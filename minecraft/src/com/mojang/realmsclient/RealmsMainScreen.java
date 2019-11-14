package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateTrialScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean overrideConfigure;
	private final RateLimiter inviteNarrationLimiter;
	private boolean dontSetConnectedToRealms;
	private static List<ResourceLocation> teaserImages = ImmutableList.of();
	private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
	private static int lastScrollYPosition = -1;
	private final RealmsScreen lastScreen;
	private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
	private long selectedServerId = -1L;
	private RealmsButton playButton;
	private RealmsButton backButton;
	private RealmsButton renewButton;
	private RealmsButton configureButton;
	private RealmsButton leaveButton;
	private String toolTip;
	private List<RealmsServer> realmsServers = Lists.<RealmsServer>newArrayList();
	private volatile int numberOfPendingInvites;
	private int animTick;
	private static volatile boolean hasParentalConsent;
	private static volatile boolean checkedParentalConsent;
	private static volatile boolean checkedClientCompatability;
	private boolean hasFetchedServers;
	private boolean popupOpenedByUser;
	private boolean justClosedPopup;
	private volatile boolean trialsAvailable;
	private volatile boolean createdTrial;
	private volatile boolean showingPopup;
	private volatile boolean hasUnreadNews;
	private volatile String newsLink;
	private int carouselIndex;
	private int carouselTick;
	private boolean hasSwitchedCarouselImage;
	private static RealmsScreen realmsGenericErrorScreen;
	private static boolean regionsPinged;
	private List<KeyCombo> keyCombos;
	private int clicks;
	private ReentrantLock connectLock = new ReentrantLock();
	private boolean expiredHover;
	private RealmsMainScreen.ShowPopupButton showPopupButton;
	private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
	private RealmsMainScreen.NewsButton newsButton;
	private RealmsButton createTrialButton;
	private RealmsButton buyARealmButton;
	private RealmsButton closeButton;

	public RealmsMainScreen(RealmsScreen realmsScreen) {
		this.lastScreen = realmsScreen;
		this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
	}

	public boolean shouldShowMessageInList() {
		if (this.hasParentalConsent() && this.hasFetchedServers) {
			if (this.trialsAvailable && !this.createdTrial) {
				return true;
			} else {
				for (RealmsServer realmsServer : this.realmsServers) {
					if (realmsServer.ownerUUID.equals(Realms.getUUID())) {
						return false;
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public boolean shouldShowPopup() {
		if (!this.hasParentalConsent() || !this.hasFetchedServers) {
			return false;
		} else if (this.popupOpenedByUser) {
			return true;
		} else {
			return this.trialsAvailable && !this.createdTrial && this.realmsServers.isEmpty() ? true : this.realmsServers.isEmpty();
		}
	}

	@Override
	public void init() {
		this.keyCombos = Lists.<KeyCombo>newArrayList(
			new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> overrideConfigure = !overrideConfigure),
			new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
				if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
					this.switchToProd();
				} else {
					this.switchToStage();
				}
			}),
			new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
				if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
					this.switchToProd();
				} else {
					this.switchToLocal();
				}
			})
		);
		if (realmsGenericErrorScreen != null) {
			Realms.setScreen(realmsGenericErrorScreen);
		} else {
			this.connectLock = new ReentrantLock();
			if (checkedClientCompatability && !this.hasParentalConsent()) {
				this.checkParentalConsent();
			}

			this.checkClientCompatability();
			this.checkUnreadNews();
			if (!this.dontSetConnectedToRealms) {
				Realms.setConnectedToRealms(false);
			}

			this.setKeyboardHandlerSendRepeatsToGui(true);
			if (this.hasParentalConsent()) {
				realmsDataFetcher.forceUpdate();
			}

			this.showingPopup = false;
			this.postInit();
		}
	}

	private boolean hasParentalConsent() {
		return checkedParentalConsent && hasParentalConsent;
	}

	public void addButtons() {
		this.buttonsAdd(
			this.configureButton = new RealmsButton(1, this.width() / 2 - 190, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.configure")) {
				@Override
				public void onPress() {
					RealmsMainScreen.this.configureClicked(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
				}
			}
		);
		this.buttonsAdd(this.playButton = new RealmsButton(3, this.width() / 2 - 93, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.play")) {
			@Override
			public void onPress() {
				RealmsMainScreen.this.onPlay();
			}
		});
		this.buttonsAdd(this.backButton = new RealmsButton(2, this.width() / 2 + 4, this.height() - 32, 90, 20, getLocalizedString("gui.back")) {
			@Override
			public void onPress() {
				if (!RealmsMainScreen.this.justClosedPopup) {
					Realms.setScreen(RealmsMainScreen.this.lastScreen);
				}
			}
		});
		this.buttonsAdd(
			this.renewButton = new RealmsButton(0, this.width() / 2 + 100, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.expiredRenew")) {
				@Override
				public void onPress() {
					RealmsMainScreen.this.onRenew();
				}
			}
		);
		this.buttonsAdd(this.leaveButton = new RealmsButton(7, this.width() / 2 - 202, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.leave")) {
			@Override
			public void onPress() {
				RealmsMainScreen.this.leaveClicked(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
			}
		});
		this.buttonsAdd(this.pendingInvitesButton = new RealmsMainScreen.PendingInvitesButton());
		this.buttonsAdd(this.newsButton = new RealmsMainScreen.NewsButton());
		this.buttonsAdd(this.showPopupButton = new RealmsMainScreen.ShowPopupButton());
		this.buttonsAdd(this.closeButton = new RealmsMainScreen.CloseButton());
		this.buttonsAdd(
			this.createTrialButton = new RealmsButton(6, this.width() / 2 + 52, this.popupY0() + 137 - 20, 98, 20, getLocalizedString("mco.selectServer.trial")) {
				@Override
				public void onPress() {
					RealmsMainScreen.this.createTrial();
				}
			}
		);
		this.buttonsAdd(
			this.buyARealmButton = new RealmsButton(5, this.width() / 2 + 52, this.popupY0() + 160 - 20, 98, 20, getLocalizedString("mco.selectServer.buy")) {
				@Override
				public void onPress() {
					RealmsUtil.browseTo("https://minecraft.net/realms");
				}
			}
		);
		RealmsServer realmsServer = this.findServer(this.selectedServerId);
		this.updateButtonStates(realmsServer);
	}

	private void updateButtonStates(RealmsServer realmsServer) {
		this.playButton.active(this.shouldPlayButtonBeActive(realmsServer) && !this.shouldShowPopup());
		this.renewButton.setVisible(this.shouldRenewButtonBeActive(realmsServer));
		this.configureButton.setVisible(this.shouldConfigureButtonBeVisible(realmsServer));
		this.leaveButton.setVisible(this.shouldLeaveButtonBeVisible(realmsServer));
		boolean bl = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
		this.createTrialButton.setVisible(bl);
		this.createTrialButton.active(bl);
		this.buyARealmButton.setVisible(this.shouldShowPopup());
		this.closeButton.setVisible(this.shouldShowPopup() && this.popupOpenedByUser);
		this.renewButton.active(!this.shouldShowPopup());
		this.configureButton.active(!this.shouldShowPopup());
		this.leaveButton.active(!this.shouldShowPopup());
		this.newsButton.active(true);
		this.pendingInvitesButton.active(true);
		this.backButton.active(true);
		this.showPopupButton.active(!this.shouldShowPopup());
	}

	private boolean shouldShowPopupButton() {
		return (!this.shouldShowPopup() || this.popupOpenedByUser) && this.hasParentalConsent() && this.hasFetchedServers;
	}

	private boolean shouldPlayButtonBeActive(RealmsServer realmsServer) {
		return realmsServer != null && !realmsServer.expired && realmsServer.state == RealmsServer.State.OPEN;
	}

	private boolean shouldRenewButtonBeActive(RealmsServer realmsServer) {
		return realmsServer != null && realmsServer.expired && this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldConfigureButtonBeVisible(RealmsServer realmsServer) {
		return realmsServer != null && this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldLeaveButtonBeVisible(RealmsServer realmsServer) {
		return realmsServer != null && !this.isSelfOwnedServer(realmsServer);
	}

	public void postInit() {
		if (this.hasParentalConsent() && this.hasFetchedServers) {
			this.addButtons();
		}

		this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
		if (lastScrollYPosition != -1) {
			this.realmSelectionList.scroll(lastScrollYPosition);
		}

		this.addWidget(this.realmSelectionList);
		this.focusOn(this.realmSelectionList);
	}

	@Override
	public void tick() {
		this.tickButtons();
		this.justClosedPopup = false;
		this.animTick++;
		this.clicks--;
		if (this.clicks < 0) {
			this.clicks = 0;
		}

		if (this.hasParentalConsent()) {
			realmsDataFetcher.init();
			if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
				List<RealmsServer> list = realmsDataFetcher.getServers();
				this.realmSelectionList.clear();
				boolean bl = !this.hasFetchedServers;
				if (bl) {
					this.hasFetchedServers = true;
				}

				if (list != null) {
					boolean bl2 = false;

					for (RealmsServer realmsServer : list) {
						if (this.isSelfOwnedNonExpiredServer(realmsServer)) {
							bl2 = true;
						}
					}

					this.realmsServers = list;
					if (this.shouldShowMessageInList()) {
						this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListTrialEntry());
					}

					for (RealmsServer realmsServerx : this.realmsServers) {
						this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListEntry(realmsServerx));
					}

					if (!regionsPinged && bl2) {
						regionsPinged = true;
						this.pingRegions();
					}
				}

				if (bl) {
					this.addButtons();
				}
			}

			if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
				this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
				if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
					Realms.narrateNow(getLocalizedString("mco.configure.world.invite.narration", new Object[]{this.numberOfPendingInvites}));
				}
			}

			if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
				boolean bl3 = realmsDataFetcher.isTrialAvailable();
				if (bl3 != this.trialsAvailable && this.shouldShowPopup()) {
					this.trialsAvailable = bl3;
					this.showingPopup = false;
				} else {
					this.trialsAvailable = bl3;
				}
			}

			if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
				RealmsServerPlayerLists realmsServerPlayerLists = realmsDataFetcher.getLivestats();

				for (RealmsServerPlayerList realmsServerPlayerList : realmsServerPlayerLists.servers) {
					for (RealmsServer realmsServerx : this.realmsServers) {
						if (realmsServerx.id == realmsServerPlayerList.serverId) {
							realmsServerx.updateServerPing(realmsServerPlayerList);
							break;
						}
					}
				}
			}

			if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
				this.hasUnreadNews = realmsDataFetcher.hasUnreadNews();
				this.newsLink = realmsDataFetcher.newsLink();
			}

			realmsDataFetcher.markClean();
			if (this.shouldShowPopup()) {
				this.carouselTick++;
			}

			if (this.showPopupButton != null) {
				this.showPopupButton.setVisible(this.shouldShowPopupButton());
			}
		}
	}

	private void browseURL(String string) {
		Realms.setClipboard(string);
		RealmsUtil.browseTo(string);
	}

	private void pingRegions() {
		new Thread(() -> {
			List<RegionPingResult> list = Ping.pingAllRegions();
			RealmsClient realmsClient = RealmsClient.createRealmsClient();
			PingResult pingResult = new PingResult();
			pingResult.pingResults = list;
			pingResult.worldIds = this.getOwnedNonExpiredWorldIds();

			try {
				realmsClient.sendPingResults(pingResult);
			} catch (Throwable var5) {
				LOGGER.warn("Could not send ping result to Realms: ", var5);
			}
		}).start();
	}

	private List<Long> getOwnedNonExpiredWorldIds() {
		List<Long> list = Lists.<Long>newArrayList();

		for (RealmsServer realmsServer : this.realmsServers) {
			if (this.isSelfOwnedNonExpiredServer(realmsServer)) {
				list.add(realmsServer.id);
			}
		}

		return list;
	}

	@Override
	public void removed() {
		this.setKeyboardHandlerSendRepeatsToGui(false);
		this.stopRealmsFetcher();
	}

	public void setCreatedTrial(boolean bl) {
		this.createdTrial = bl;
	}

	private void onPlay() {
		RealmsServer realmsServer = this.findServer(this.selectedServerId);
		if (realmsServer != null) {
			this.play(realmsServer, this);
		}
	}

	private void onRenew() {
		RealmsServer realmsServer = this.findServer(this.selectedServerId);
		if (realmsServer != null) {
			String string = "https://account.mojang.com/buy/realms?sid="
				+ realmsServer.remoteSubscriptionId
				+ "&pid="
				+ Realms.getUUID()
				+ "&ref="
				+ (realmsServer.expiredTrial ? "expiredTrial" : "expiredRealm");
			this.browseURL(string);
		}
	}

	private void createTrial() {
		if (this.trialsAvailable && !this.createdTrial) {
			Realms.setScreen(new RealmsCreateTrialScreen(this));
		}
	}

	private void checkClientCompatability() {
		if (!checkedClientCompatability) {
			checkedClientCompatability = true;
			(new Thread("MCO Compatability Checker #1") {
					public void run() {
						RealmsClient realmsClient = RealmsClient.createRealmsClient();

						try {
							RealmsClient.CompatibleVersionResponse compatibleVersionResponse = realmsClient.clientCompatible();
							if (compatibleVersionResponse.equals(RealmsClient.CompatibleVersionResponse.OUTDATED)) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
								Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
							} else if (compatibleVersionResponse.equals(RealmsClient.CompatibleVersionResponse.OTHER)) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
								Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
							} else {
								RealmsMainScreen.this.checkParentalConsent();
							}
						} catch (RealmsServiceException var3) {
							RealmsMainScreen.checkedClientCompatability = false;
							RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var3.toString());
							if (var3.httpResultCode == 401) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(
									RealmsScreen.getLocalizedString("mco.error.invalid.session.title"),
									RealmsScreen.getLocalizedString("mco.error.invalid.session.message"),
									RealmsMainScreen.this.lastScreen
								);
								Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
							} else {
								Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
							}
						} catch (IOException var4) {
							RealmsMainScreen.checkedClientCompatability = false;
							RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
							Realms.setScreen(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
						}
					}
				})
				.start();
		}
	}

	private void checkUnreadNews() {
	}

	private void checkParentalConsent() {
		(new Thread("MCO Compatability Checker #1") {
			public void run() {
				RealmsClient realmsClient = RealmsClient.createRealmsClient();

				try {
					Boolean boolean_ = realmsClient.mcoEnabled();
					if (boolean_) {
						RealmsMainScreen.LOGGER.info("Realms is available for this user");
						RealmsMainScreen.hasParentalConsent = true;
					} else {
						RealmsMainScreen.LOGGER.info("Realms is not available for this user");
						RealmsMainScreen.hasParentalConsent = false;
						Realms.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
					}

					RealmsMainScreen.checkedParentalConsent = true;
				} catch (RealmsServiceException var3) {
					RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var3.toString());
					Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
				} catch (IOException var4) {
					RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
					Realms.setScreen(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
				}
			}
		}).start();
	}

	private void switchToStage() {
		if (!RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
			(new Thread("MCO Stage Availability Checker #1") {
				public void run() {
					RealmsClient realmsClient = RealmsClient.createRealmsClient();

					try {
						Boolean boolean_ = realmsClient.stageAvailable();
						if (boolean_) {
							RealmsClient.switchToStage();
							RealmsMainScreen.LOGGER.info("Switched to stage");
							RealmsMainScreen.realmsDataFetcher.forceUpdate();
						}
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
					} catch (IOException var4) {
						RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
					}
				}
			}).start();
		}
	}

	private void switchToLocal() {
		if (!RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
			(new Thread("MCO Local Availability Checker #1") {
				public void run() {
					RealmsClient realmsClient = RealmsClient.createRealmsClient();

					try {
						Boolean boolean_ = realmsClient.stageAvailable();
						if (boolean_) {
							RealmsClient.switchToLocal();
							RealmsMainScreen.LOGGER.info("Switched to local");
							RealmsMainScreen.realmsDataFetcher.forceUpdate();
						}
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
					} catch (IOException var4) {
						RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
					}
				}
			}).start();
		}
	}

	private void switchToProd() {
		RealmsClient.switchToProd();
		realmsDataFetcher.forceUpdate();
	}

	private void stopRealmsFetcher() {
		realmsDataFetcher.stop();
	}

	private void configureClicked(RealmsServer realmsServer) {
		if (Realms.getUUID().equals(realmsServer.ownerUUID) || overrideConfigure) {
			this.saveListScrollPosition();
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.execute(() -> minecraft.setScreen(new RealmsConfigureWorldScreen(this, realmsServer.id).getProxy()));
		}
	}

	private void leaveClicked(RealmsServer realmsServer) {
		if (!Realms.getUUID().equals(realmsServer.ownerUUID)) {
			this.saveListScrollPosition();
			String string = getLocalizedString("mco.configure.world.leave.question.line1");
			String string2 = getLocalizedString("mco.configure.world.leave.question.line2");
			Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, string, string2, true, 4));
		}
	}

	private void saveListScrollPosition() {
		lastScrollYPosition = this.realmSelectionList.getScroll();
	}

	private RealmsServer findServer(long l) {
		for (RealmsServer realmsServer : this.realmsServers) {
			if (realmsServer.id == l) {
				return realmsServer;
			}
		}

		return null;
	}

	@Override
	public void confirmResult(boolean bl, int i) {
		if (i == 4) {
			if (bl) {
				(new Thread("Realms-leave-server") {
					public void run() {
						try {
							RealmsServer realmsServer = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
							if (realmsServer != null) {
								RealmsClient realmsClient = RealmsClient.createRealmsClient();
								realmsClient.uninviteMyselfFrom(realmsServer.id);
								RealmsMainScreen.realmsDataFetcher.removeItem(realmsServer);
								RealmsMainScreen.this.realmsServers.remove(realmsServer);
								RealmsMainScreen.this.selectedServerId = -1L;
								RealmsMainScreen.this.playButton.active(false);
							}
						} catch (RealmsServiceException var3) {
							RealmsMainScreen.LOGGER.error("Couldn't configure world");
							Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this));
						}
					}
				}).start();
			}

			Realms.setScreen(this);
		}
	}

	public void removeSelection() {
		this.selectedServerId = -1L;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		switch (i) {
			case 256:
				this.keyCombos.forEach(KeyCombo::reset);
				this.onClosePopup();
				return true;
			default:
				return super.keyPressed(i, j, k);
		}
	}

	private void onClosePopup() {
		if (this.shouldShowPopup() && this.popupOpenedByUser) {
			this.popupOpenedByUser = false;
		} else {
			Realms.setScreen(this.lastScreen);
		}
	}

	@Override
	public boolean charTyped(char c, int i) {
		this.keyCombos.forEach(keyCombo -> keyCombo.keyPressed(c));
		return true;
	}

	@Override
	public void render(int i, int j, float f) {
		this.expiredHover = false;
		this.toolTip = null;
		this.renderBackground();
		this.realmSelectionList.render(i, j, f);
		this.drawRealmsLogo(this.width() / 2 - 50, 7);
		if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
			this.renderStage();
		}

		if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
			this.renderLocal();
		}

		if (this.shouldShowPopup()) {
			this.drawPopup(i, j);
		} else {
			if (this.showingPopup) {
				this.updateButtonStates(null);
				if (!this.hasWidget(this.realmSelectionList)) {
					this.addWidget(this.realmSelectionList);
				}

				RealmsServer realmsServer = this.findServer(this.selectedServerId);
				this.playButton.active(this.shouldPlayButtonBeActive(realmsServer));
			}

			this.showingPopup = false;
		}

		super.render(i, j, f);
		if (this.toolTip != null) {
			this.renderMousehoverTooltip(this.toolTip, i, j);
		}

		if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
			RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.pushMatrix();
			int k = 8;
			int l = 8;
			int m = 0;
			if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
				m = 8;
			}

			RealmsScreen.blit(
				this.createTrialButton.x() + this.createTrialButton.getWidth() - 8 - 4,
				this.createTrialButton.y() + this.createTrialButton.getHeight() / 2 - 4,
				0.0F,
				(float)m,
				8,
				8,
				8,
				16
			);
			RenderSystem.popMatrix();
		}
	}

	private void drawRealmsLogo(int i, int j) {
		RealmsScreen.bind("realms:textures/gui/title/realms.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(0.5F, 0.5F, 0.5F);
		RealmsScreen.blit(i * 2, j * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
		RenderSystem.popMatrix();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.isOutsidePopup(d, e) && this.popupOpenedByUser) {
			this.popupOpenedByUser = false;
			this.justClosedPopup = true;
			return true;
		} else {
			return super.mouseClicked(d, e, i);
		}
	}

	private boolean isOutsidePopup(double d, double e) {
		int i = this.popupX0();
		int j = this.popupY0();
		return d < (double)(i - 5) || d > (double)(i + 315) || e < (double)(j - 5) || e > (double)(j + 171);
	}

	private void drawPopup(int i, int j) {
		int k = this.popupX0();
		int l = this.popupY0();
		String string = getLocalizedString("mco.selectServer.popup");
		List<String> list = this.fontSplit(string, 100);
		if (!this.showingPopup) {
			this.carouselIndex = 0;
			this.carouselTick = 0;
			this.hasSwitchedCarouselImage = true;
			this.updateButtonStates(null);
			if (this.hasWidget(this.realmSelectionList)) {
				this.removeWidget(this.realmSelectionList);
			}

			Realms.narrateNow(string);
		}

		if (this.hasFetchedServers) {
			this.showingPopup = true;
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
		RenderSystem.enableBlend();
		RealmsScreen.bind("realms:textures/gui/realms/darken.png");
		RenderSystem.pushMatrix();
		int m = 0;
		int n = 32;
		RealmsScreen.blit(0, 32, 0.0F, 0.0F, this.width(), this.height() - 40 - 32, 310, 166);
		RenderSystem.popMatrix();
		RenderSystem.disableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RealmsScreen.bind("realms:textures/gui/realms/popup.png");
		RenderSystem.pushMatrix();
		RealmsScreen.blit(k, l, 0.0F, 0.0F, 310, 166, 310, 166);
		RenderSystem.popMatrix();
		if (!teaserImages.isEmpty()) {
			RealmsScreen.bind(((ResourceLocation)teaserImages.get(this.carouselIndex)).toString());
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.pushMatrix();
			RealmsScreen.blit(k + 7, l + 7, 0.0F, 0.0F, 195, 152, 195, 152);
			RenderSystem.popMatrix();
			if (this.carouselTick % 95 < 5) {
				if (!this.hasSwitchedCarouselImage) {
					this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
					this.hasSwitchedCarouselImage = true;
				}
			} else {
				this.hasSwitchedCarouselImage = false;
			}
		}

		int o = 0;

		for (String string2 : list) {
			int var10002 = this.width() / 2 + 52;
			o++;
			this.drawString(string2, var10002, l + 10 * o - 3, 8421504, false);
		}
	}

	private int popupX0() {
		return (this.width() - 310) / 2;
	}

	private int popupY0() {
		return this.height() / 2 - 80;
	}

	private void drawInvitationPendingIcon(int i, int j, int k, int l, boolean bl, boolean bl2) {
		int m = this.numberOfPendingInvites;
		boolean bl3 = this.inPendingInvitationArea((double)i, (double)j);
		boolean bl4 = bl2 && bl;
		if (bl4) {
			float f = 0.25F + (1.0F + RealmsMth.sin((float)this.animTick * 0.5F)) * 0.25F;
			int n = 0xFF000000 | (int)(f * 64.0F) << 16 | (int)(f * 64.0F) << 8 | (int)(f * 64.0F) << 0;
			this.fillGradient(k - 2, l - 2, k + 18, l + 18, n, n);
			n = 0xFF000000 | (int)(f * 255.0F) << 16 | (int)(f * 255.0F) << 8 | (int)(f * 255.0F) << 0;
			this.fillGradient(k - 2, l - 2, k + 18, l - 1, n, n);
			this.fillGradient(k - 2, l - 2, k - 1, l + 18, n, n);
			this.fillGradient(k + 17, l - 2, k + 18, l + 18, n, n);
			this.fillGradient(k - 2, l + 17, k + 18, l + 18, n, n);
		}

		RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		boolean bl5 = bl2 && bl;
		RealmsScreen.blit(k, l - 6, bl5 ? 16.0F : 0.0F, 0.0F, 15, 25, 31, 25);
		RenderSystem.popMatrix();
		boolean bl6 = bl2 && m != 0;
		if (bl6) {
			int o = (Math.min(m, 6) - 1) * 8;
			int p = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
			RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.pushMatrix();
			RealmsScreen.blit(k + 4, l + 4 + p, (float)o, bl3 ? 8.0F : 0.0F, 8, 8, 48, 16);
			RenderSystem.popMatrix();
		}

		int o = i + 12;
		boolean bl7 = bl2 && bl3;
		if (bl7) {
			String string = getLocalizedString(m == 0 ? "mco.invites.nopending" : "mco.invites.pending");
			int q = this.fontWidth(string);
			this.fillGradient(o - 3, j - 3, o + q + 3, j + 8 + 3, -1073741824, -1073741824);
			this.fontDrawShadow(string, o, j, -1);
		}
	}

	private boolean inPendingInvitationArea(double d, double e) {
		int i = this.width() / 2 + 50;
		int j = this.width() / 2 + 66;
		int k = 11;
		int l = 23;
		if (this.numberOfPendingInvites != 0) {
			i -= 3;
			j += 3;
			k -= 5;
			l += 5;
		}

		return (double)i <= d && d <= (double)j && (double)k <= e && e <= (double)l;
	}

	public void play(RealmsServer realmsServer, RealmsScreen realmsScreen) {
		if (realmsServer != null) {
			try {
				if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
					return;
				}

				if (this.connectLock.getHoldCount() > 1) {
					return;
				}
			} catch (InterruptedException var4) {
				return;
			}

			this.dontSetConnectedToRealms = true;
			this.connectToServer(realmsServer, realmsScreen);
		}
	}

	private void connectToServer(RealmsServer realmsServer, RealmsScreen realmsScreen) {
		RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(
			realmsScreen, new RealmsTasks.RealmsGetServerDetailsTask(this, realmsScreen, realmsServer, this.connectLock)
		);
		realmsLongRunningMcoTaskScreen.start();
		Realms.setScreen(realmsLongRunningMcoTaskScreen);
	}

	private boolean isSelfOwnedServer(RealmsServer realmsServer) {
		return realmsServer.ownerUUID != null && realmsServer.ownerUUID.equals(Realms.getUUID());
	}

	private boolean isSelfOwnedNonExpiredServer(RealmsServer realmsServer) {
		return realmsServer.ownerUUID != null && realmsServer.ownerUUID.equals(Realms.getUUID()) && !realmsServer.expired;
	}

	private void drawExpired(int i, int j, int k, int l) {
		RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RealmsScreen.blit(i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		RenderSystem.popMatrix();
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height() - 40 && l > 32 && !this.shouldShowPopup()) {
			this.toolTip = getLocalizedString("mco.selectServer.expired");
		}
	}

	private void drawExpiring(int i, int j, int k, int l, int m) {
		RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		if (this.animTick % 20 < 10) {
			RealmsScreen.blit(i, j, 0.0F, 0.0F, 10, 28, 20, 28);
		} else {
			RealmsScreen.blit(i, j, 10.0F, 0.0F, 10, 28, 20, 28);
		}

		RenderSystem.popMatrix();
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height() - 40 && l > 32 && !this.shouldShowPopup()) {
			if (m <= 0) {
				this.toolTip = getLocalizedString("mco.selectServer.expires.soon");
			} else if (m == 1) {
				this.toolTip = getLocalizedString("mco.selectServer.expires.day");
			} else {
				this.toolTip = getLocalizedString("mco.selectServer.expires.days", new Object[]{m});
			}
		}
	}

	private void drawOpen(int i, int j, int k, int l) {
		RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RealmsScreen.blit(i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		RenderSystem.popMatrix();
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height() - 40 && l > 32 && !this.shouldShowPopup()) {
			this.toolTip = getLocalizedString("mco.selectServer.open");
		}
	}

	private void drawClose(int i, int j, int k, int l) {
		RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RealmsScreen.blit(i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		RenderSystem.popMatrix();
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height() - 40 && l > 32 && !this.shouldShowPopup()) {
			this.toolTip = getLocalizedString("mco.selectServer.closed");
		}
	}

	private void drawLeave(int i, int j, int k, int l) {
		boolean bl = false;
		if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height() - 40 && l > 32 && !this.shouldShowPopup()) {
			bl = true;
		}

		RealmsScreen.bind("realms:textures/gui/realms/leave_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RealmsScreen.blit(i, j, bl ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
		RenderSystem.popMatrix();
		if (bl) {
			this.toolTip = getLocalizedString("mco.selectServer.leave");
		}
	}

	private void drawConfigure(int i, int j, int k, int l) {
		boolean bl = false;
		if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height() - 40 && l > 32 && !this.shouldShowPopup()) {
			bl = true;
		}

		RealmsScreen.bind("realms:textures/gui/realms/configure_icon.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RealmsScreen.blit(i, j, bl ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
		RenderSystem.popMatrix();
		if (bl) {
			this.toolTip = getLocalizedString("mco.selectServer.configure");
		}
	}

	protected void renderMousehoverTooltip(String string, int i, int j) {
		if (string != null) {
			int k = 0;
			int l = 0;

			for (String string2 : string.split("\n")) {
				int m = this.fontWidth(string2);
				if (m > l) {
					l = m;
				}
			}

			int n = i - l - 5;
			int o = j;
			if (n < 0) {
				n = i + 12;
			}

			for (String string3 : string.split("\n")) {
				this.fillGradient(n - 3, o - (k == 0 ? 3 : 0) + k, n + l + 3, o + 8 + 3 + k, -1073741824, -1073741824);
				this.fontDrawShadow(string3, n, o + k, 16777215);
				k += 10;
			}
		}
	}

	private void renderMoreInfo(int i, int j, int k, int l, boolean bl) {
		boolean bl2 = false;
		if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
			bl2 = true;
		}

		RealmsScreen.bind("realms:textures/gui/realms/questionmark.png");
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RealmsScreen.blit(k, l, bl ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
		RenderSystem.popMatrix();
		if (bl2) {
			this.toolTip = getLocalizedString("mco.selectServer.info");
		}
	}

	private void renderNews(int i, int j, boolean bl, int k, int l, boolean bl2, boolean bl3) {
		boolean bl4 = false;
		if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
			bl4 = true;
		}

		RealmsScreen.bind("realms:textures/gui/realms/news_icon.png");
		if (bl3) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
		}

		RenderSystem.pushMatrix();
		boolean bl5 = bl3 && bl2;
		RealmsScreen.blit(k, l, bl5 ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
		RenderSystem.popMatrix();
		if (bl4 && bl3) {
			this.toolTip = getLocalizedString("mco.news");
		}

		if (bl && bl3) {
			int m = bl4 ? 0 : (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
			RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.pushMatrix();
			RealmsScreen.blit(k + 10, l + 2 + m, 40.0F, 0.0F, 8, 8, 48, 16);
			RenderSystem.popMatrix();
		}
	}

	private void renderLocal() {
		String string = "LOCAL!";
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
		RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.scalef(1.5F, 1.5F, 1.5F);
		this.drawString("LOCAL!", 0, 0, 8388479);
		RenderSystem.popMatrix();
	}

	private void renderStage() {
		String string = "STAGE!";
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
		RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.scalef(1.5F, 1.5F, 1.5F);
		this.drawString("STAGE!", 0, 0, -256);
		RenderSystem.popMatrix();
	}

	public RealmsMainScreen newScreen() {
		return new RealmsMainScreen(this.lastScreen);
	}

	public void closePopup() {
		if (this.shouldShowPopup() && this.popupOpenedByUser) {
			this.popupOpenedByUser = false;
		}
	}

	public static void updateTeaserImages(ResourceManager resourceManager) {
		Collection<ResourceLocation> collection = resourceManager.listResources("textures/gui/images", string -> string.endsWith(".png"));
		teaserImages = (List<ResourceLocation>)collection.stream()
			.filter(resourceLocation -> resourceLocation.getNamespace().equals("realms"))
			.collect(ImmutableList.toImmutableList());
	}

	@Environment(EnvType.CLIENT)
	class CloseButton extends RealmsButton {
		public CloseButton() {
			super(11, RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, 12, 12, RealmsScreen.getLocalizedString("mco.selectServer.close"));
		}

		@Override
		public void tick() {
			super.tick();
		}

		@Override
		public void render(int i, int j, float f) {
			super.render(i, j, f);
		}

		@Override
		public void renderButton(int i, int j, float f) {
			RealmsScreen.bind("realms:textures/gui/realms/cross_icon.png");
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.pushMatrix();
			RealmsScreen.blit(this.x(), this.y(), 0.0F, this.getProxy().isHovered() ? 12.0F : 0.0F, 12, 12, 12, 24);
			RenderSystem.popMatrix();
			if (this.getProxy().isMouseOver((double)i, (double)j)) {
				RealmsMainScreen.this.toolTip = this.getProxy().getMessage();
			}
		}

		@Override
		public void onPress() {
			RealmsMainScreen.this.onClosePopup();
		}
	}

	@Environment(EnvType.CLIENT)
	class NewsButton extends RealmsButton {
		public NewsButton() {
			super(9, RealmsMainScreen.this.width() - 62, 6, 20, 20, "");
		}

		@Override
		public void tick() {
			this.setMessage(Realms.getLocalizedString("mco.news"));
		}

		@Override
		public void render(int i, int j, float f) {
			super.render(i, j, f);
		}

		@Override
		public void onPress() {
			if (RealmsMainScreen.this.newsLink != null) {
				RealmsUtil.browseTo(RealmsMainScreen.this.newsLink);
				if (RealmsMainScreen.this.hasUnreadNews) {
					RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
					realmsPersistenceData.hasUnreadNews = false;
					RealmsMainScreen.this.hasUnreadNews = false;
					RealmsPersistence.writeFile(realmsPersistenceData);
				}
			}
		}

		@Override
		public void renderButton(int i, int j, float f) {
			RealmsMainScreen.this.renderNews(i, j, RealmsMainScreen.this.hasUnreadNews, this.x(), this.y(), this.getProxy().isHovered(), this.active());
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInvitesButton extends RealmsButton {
		public PendingInvitesButton() {
			super(8, RealmsMainScreen.this.width() / 2 + 47, 6, 22, 22, "");
		}

		@Override
		public void tick() {
			this.setMessage(Realms.getLocalizedString(RealmsMainScreen.this.numberOfPendingInvites == 0 ? "mco.invites.nopending" : "mco.invites.pending"));
		}

		@Override
		public void render(int i, int j, float f) {
			super.render(i, j, f);
		}

		@Override
		public void onPress() {
			RealmsPendingInvitesScreen realmsPendingInvitesScreen = new RealmsPendingInvitesScreen(RealmsMainScreen.this.lastScreen);
			Realms.setScreen(realmsPendingInvitesScreen);
		}

		@Override
		public void renderButton(int i, int j, float f) {
			RealmsMainScreen.this.drawInvitationPendingIcon(i, j, this.x(), this.y(), this.getProxy().isHovered(), this.active());
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionList extends RealmsObjectSelectionList {
		public RealmSelectionList() {
			super(RealmsMainScreen.this.width(), RealmsMainScreen.this.height(), 32, RealmsMainScreen.this.height() - 40, 36);
		}

		@Override
		public boolean isFocused() {
			return RealmsMainScreen.this.isFocused(this);
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (i != 257 && i != 32 && i != 335) {
				return false;
			} else {
				RealmListEntry realmListEntry = this.getSelected();
				return realmListEntry == null ? super.keyPressed(i, j, k) : realmListEntry.mouseClicked(0.0, 0.0, 0);
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (i == 0 && d < (double)this.getScrollbarPosition() && e >= (double)this.y0() && e <= (double)this.y1()) {
				int j = RealmsMainScreen.this.realmSelectionList.getRowLeft();
				int k = this.getScrollbarPosition();
				int l = (int)Math.floor(e - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
				int m = l / this.itemHeight();
				if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
					this.itemClicked(l, m, d, e, this.width());
					RealmsMainScreen.this.clicks = RealmsMainScreen.this.clicks + 7;
					this.selectItem(m);
				}

				return true;
			} else {
				return super.mouseClicked(d, e, i);
			}
		}

		@Override
		public void selectItem(int i) {
			this.setSelected(i);
			if (i != -1) {
				RealmsServer realmsServer;
				if (RealmsMainScreen.this.shouldShowMessageInList()) {
					if (i == 0) {
						Realms.narrateNow(RealmsScreen.getLocalizedString("mco.trial.message.line1"), RealmsScreen.getLocalizedString("mco.trial.message.line2"));
						realmsServer = null;
					} else {
						realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i - 1);
					}
				} else {
					realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i);
				}

				RealmsMainScreen.this.updateButtonStates(realmsServer);
				if (realmsServer == null) {
					RealmsMainScreen.this.selectedServerId = -1L;
				} else if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
					Realms.narrateNow(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized") + RealmsScreen.getLocalizedString("mco.gui.button"));
					RealmsMainScreen.this.selectedServerId = -1L;
				} else {
					RealmsMainScreen.this.selectedServerId = realmsServer.id;
					if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active()) {
						RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
					}

					Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", realmsServer.name));
				}
			}
		}

		@Override
		public void itemClicked(int i, int j, double d, double e, int k) {
			if (RealmsMainScreen.this.shouldShowMessageInList()) {
				if (j == 0) {
					RealmsMainScreen.this.popupOpenedByUser = true;
					return;
				}

				j--;
			}

			if (j < RealmsMainScreen.this.realmsServers.size()) {
				RealmsServer realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(j);
				if (realmsServer != null) {
					if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
						RealmsMainScreen.this.selectedServerId = -1L;
						Realms.setScreen(new RealmsCreateRealmScreen(realmsServer, RealmsMainScreen.this));
					} else {
						RealmsMainScreen.this.selectedServerId = realmsServer.id;
					}

					if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.configure"))) {
						RealmsMainScreen.this.selectedServerId = realmsServer.id;
						RealmsMainScreen.this.configureClicked(realmsServer);
					} else if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.leave"))) {
						RealmsMainScreen.this.selectedServerId = realmsServer.id;
						RealmsMainScreen.this.leaveClicked(realmsServer);
					} else if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.expired && RealmsMainScreen.this.expiredHover) {
						RealmsMainScreen.this.onRenew();
					}
				}
			}
		}

		@Override
		public int getMaxPosition() {
			return this.getItemCount() * 36;
		}

		@Override
		public int getRowWidth() {
			return 300;
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionListEntry extends RealmListEntry {
		final RealmsServer mServerData;

		public RealmSelectionListEntry(RealmsServer realmsServer) {
			this.mServerData = realmsServer;
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderMcoServerItem(this.mServerData, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.mServerData.state == RealmsServer.State.UNINITIALIZED) {
				RealmsMainScreen.this.selectedServerId = -1L;
				Realms.setScreen(new RealmsCreateRealmScreen(this.mServerData, RealmsMainScreen.this));
			} else {
				RealmsMainScreen.this.selectedServerId = this.mServerData.id;
			}

			return true;
		}

		private void renderMcoServerItem(RealmsServer realmsServer, int i, int j, int k, int l) {
			this.renderLegacy(realmsServer, i + 36, j, k, l);
		}

		private void renderLegacy(RealmsServer realmsServer, int i, int j, int k, int l) {
			if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
				RealmsScreen.bind("realms:textures/gui/realms/world_icon.png");
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableAlphaTest();
				RenderSystem.pushMatrix();
				RealmsScreen.blit(i + 10, j + 6, 0.0F, 0.0F, 40, 20, 40, 20);
				RenderSystem.popMatrix();
				float f = 0.5F + (1.0F + RealmsMth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
				int m = 0xFF000000 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
				RealmsMainScreen.this.drawCenteredString(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"), i + 10 + 40 + 75, j + 12, m);
			} else {
				int n = 225;
				int m = 2;
				if (realmsServer.expired) {
					RealmsMainScreen.this.drawExpired(i + 225 - 14, j + 2, k, l);
				} else if (realmsServer.state == RealmsServer.State.CLOSED) {
					RealmsMainScreen.this.drawClose(i + 225 - 14, j + 2, k, l);
				} else if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.daysLeft < 7) {
					RealmsMainScreen.this.drawExpiring(i + 225 - 14, j + 2, k, l, realmsServer.daysLeft);
				} else if (realmsServer.state == RealmsServer.State.OPEN) {
					RealmsMainScreen.this.drawOpen(i + 225 - 14, j + 2, k, l);
				}

				if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && !RealmsMainScreen.overrideConfigure) {
					RealmsMainScreen.this.drawLeave(i + 225, j + 2, k, l);
				} else {
					RealmsMainScreen.this.drawConfigure(i + 225, j + 2, k, l);
				}

				if (!"0".equals(realmsServer.serverPing.nrOfPlayers)) {
					String string = ChatFormatting.GRAY + "" + realmsServer.serverPing.nrOfPlayers;
					RealmsMainScreen.this.drawString(string, i + 207 - RealmsMainScreen.this.fontWidth(string), j + 3, 8421504);
					if (k >= i + 207 - RealmsMainScreen.this.fontWidth(string)
						&& k <= i + 207
						&& l >= j + 1
						&& l <= j + 10
						&& l < RealmsMainScreen.this.height() - 40
						&& l > 32
						&& !RealmsMainScreen.this.shouldShowPopup()) {
						RealmsMainScreen.this.toolTip = realmsServer.serverPing.playerList;
					}
				}

				if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.expired) {
					boolean bl = false;
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.enableBlend();
					RealmsScreen.bind("minecraft:textures/gui/widgets.png");
					RenderSystem.pushMatrix();
					RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					String string2 = RealmsScreen.getLocalizedString("mco.selectServer.expiredList");
					String string3 = RealmsScreen.getLocalizedString("mco.selectServer.expiredRenew");
					if (realmsServer.expiredTrial) {
						string2 = RealmsScreen.getLocalizedString("mco.selectServer.expiredTrial");
						string3 = RealmsScreen.getLocalizedString("mco.selectServer.expiredSubscribe");
					}

					int o = RealmsMainScreen.this.fontWidth(string3) + 17;
					int p = 16;
					int q = i + RealmsMainScreen.this.fontWidth(string2) + 8;
					int r = j + 13;
					if (k >= q && k < q + o && l > r && l <= r + 16 & l < RealmsMainScreen.this.height() - 40 && l > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
						bl = true;
						RealmsMainScreen.this.expiredHover = true;
					}

					int s = bl ? 2 : 1;
					RealmsScreen.blit(q, r, 0.0F, (float)(46 + s * 20), o / 2, 8, 256, 256);
					RealmsScreen.blit(q + o / 2, r, (float)(200 - o / 2), (float)(46 + s * 20), o / 2, 8, 256, 256);
					RealmsScreen.blit(q, r + 8, 0.0F, (float)(46 + s * 20 + 12), o / 2, 8, 256, 256);
					RealmsScreen.blit(q + o / 2, r + 8, (float)(200 - o / 2), (float)(46 + s * 20 + 12), o / 2, 8, 256, 256);
					RenderSystem.popMatrix();
					RenderSystem.disableBlend();
					int t = j + 11 + 5;
					int u = bl ? 16777120 : 16777215;
					RealmsMainScreen.this.drawString(string2, i + 2, t + 1, 15553363);
					RealmsMainScreen.this.drawCenteredString(string3, q + o / 2, t + 1, u);
				} else {
					if (realmsServer.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
						int v = 13413468;
						String string2x = RealmsScreen.getLocalizedString("mco.selectServer.minigame") + " ";
						int w = RealmsMainScreen.this.fontWidth(string2x);
						RealmsMainScreen.this.drawString(string2x, i + 2, j + 12, 13413468);
						RealmsMainScreen.this.drawString(realmsServer.getMinigameName(), i + 2 + w, j + 12, 8421504);
					} else {
						RealmsMainScreen.this.drawString(realmsServer.getDescription(), i + 2, j + 12, 8421504);
					}

					if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer)) {
						RealmsMainScreen.this.drawString(realmsServer.owner, i + 2, j + 12 + 11, 8421504);
					}
				}

				RealmsMainScreen.this.drawString(realmsServer.getName(), i + 2, j + 1, 16777215);
				RealmsTextureManager.withBoundFace(realmsServer.ownerUUID, () -> {
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					RealmsScreen.blit(i - 36, j, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
					RealmsScreen.blit(i - 36, j, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
				});
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionListTrialEntry extends RealmListEntry {
		public RealmSelectionListTrialEntry() {
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderTrialItem(i, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsMainScreen.this.popupOpenedByUser = true;
			return true;
		}

		private void renderTrialItem(int i, int j, int k, int l, int m) {
			int n = k + 8;
			int o = 0;
			String string = RealmsScreen.getLocalizedString("mco.trial.message.line1") + "\\n" + RealmsScreen.getLocalizedString("mco.trial.message.line2");
			boolean bl = false;
			if (j <= l && l <= RealmsMainScreen.this.realmSelectionList.getScroll() && k <= m && m <= k + 32) {
				bl = true;
			}

			int p = 8388479;
			if (bl && !RealmsMainScreen.this.shouldShowPopup()) {
				p = 6077788;
			}

			for (String string2 : string.split("\\\\n")) {
				RealmsMainScreen.this.drawCenteredString(string2, RealmsMainScreen.this.width() / 2, n + o, p);
				o += 10;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ShowPopupButton extends RealmsButton {
		public ShowPopupButton() {
			super(10, RealmsMainScreen.this.width() - 37, 6, 20, 20, RealmsScreen.getLocalizedString("mco.selectServer.info"));
		}

		@Override
		public void tick() {
			super.tick();
		}

		@Override
		public void render(int i, int j, float f) {
			super.render(i, j, f);
		}

		@Override
		public void renderButton(int i, int j, float f) {
			RealmsMainScreen.this.renderMoreInfo(i, j, this.x(), this.y(), this.getProxy().isHovered());
		}

		@Override
		public void onPress() {
			RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser;
		}
	}
}
