package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
	private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
	private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
	private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
	private static final ResourceLocation LEAVE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/leave_icon.png");
	private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
	private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
	private static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
	private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
	private static final ResourceLocation CONFIGURE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/configure_icon.png");
	private static final ResourceLocation QUESTIONMARK_LOCATION = new ResourceLocation("realms", "textures/gui/realms/questionmark.png");
	private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
	private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
	private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
	private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
	private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
	private static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("minecraft", "textures/gui/widgets.png");
	private static List<ResourceLocation> teaserImages = ImmutableList.of();
	private static final RealmsDataFetcher REALMS_DATA_FETCHER = new RealmsDataFetcher();
	private static boolean overrideConfigure;
	private static int lastScrollYPosition = -1;
	private static volatile boolean hasParentalConsent;
	private static volatile boolean checkedParentalConsent;
	private static volatile boolean checkedClientCompatability;
	private static Screen realmsGenericErrorScreen;
	private static boolean regionsPinged;
	private final RateLimiter inviteNarrationLimiter;
	private boolean dontSetConnectedToRealms;
	private final Screen lastScreen;
	private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
	private long selectedServerId = -1L;
	private Button playButton;
	private Button backButton;
	private Button renewButton;
	private Button configureButton;
	private Button leaveButton;
	private List<Component> toolTip;
	private List<RealmsServer> realmsServers = Lists.<RealmsServer>newArrayList();
	private volatile int numberOfPendingInvites;
	private int animTick;
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
	private List<KeyCombo> keyCombos;
	private int clicks;
	private ReentrantLock connectLock = new ReentrantLock();
	private RealmsMainScreen.HoveredElement hoveredElement;
	private Button showPopupButton;
	private Button pendingInvitesButton;
	private Button newsButton;
	private Button createTrialButton;
	private Button buyARealmButton;
	private Button closeButton;

	public RealmsMainScreen(Screen screen) {
		this.lastScreen = screen;
		this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
	}

	private boolean shouldShowMessageInList() {
		if (hasParentalConsent() && this.hasFetchedServers) {
			if (this.trialsAvailable && !this.createdTrial) {
				return true;
			} else {
				for (RealmsServer realmsServer : this.realmsServers) {
					if (realmsServer.ownerUUID.equals(this.minecraft.getUser().getUuid())) {
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
		if (!hasParentalConsent() || !this.hasFetchedServers) {
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
				if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
					this.switchToProd();
				} else {
					this.switchToStage();
				}
			}),
			new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
				if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
					this.switchToProd();
				} else {
					this.switchToLocal();
				}
			})
		);
		if (realmsGenericErrorScreen != null) {
			this.minecraft.setScreen(realmsGenericErrorScreen);
		} else {
			this.connectLock = new ReentrantLock();
			if (checkedClientCompatability && !hasParentalConsent()) {
				this.checkParentalConsent();
			}

			this.checkClientCompatability();
			this.checkUnreadNews();
			if (!this.dontSetConnectedToRealms) {
				this.minecraft.setConnectedToRealms(false);
			}

			this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
			if (hasParentalConsent()) {
				REALMS_DATA_FETCHER.forceUpdate();
			}

			this.showingPopup = false;
			if (hasParentalConsent() && this.hasFetchedServers) {
				this.addButtons();
			}

			this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
			if (lastScrollYPosition != -1) {
				this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
			}

			this.addWidget(this.realmSelectionList);
			this.magicalSpecialHackyFocus(this.realmSelectionList);
		}
	}

	private static boolean hasParentalConsent() {
		return checkedParentalConsent && hasParentalConsent;
	}

	public void addButtons() {
		this.configureButton = this.addButton(
			new Button(
				this.width / 2 - 190,
				this.height - 32,
				90,
				20,
				new TranslatableComponent("mco.selectServer.configure"),
				button -> this.configureClicked(this.findServer(this.selectedServerId))
			)
		);
		this.playButton = this.addButton(new Button(this.width / 2 - 93, this.height - 32, 90, 20, new TranslatableComponent("mco.selectServer.play"), button -> {
			RealmsServer realmsServerx = this.findServer(this.selectedServerId);
			if (realmsServerx != null) {
				this.play(realmsServerx, this);
			}
		}));
		this.backButton = this.addButton(new Button(this.width / 2 + 4, this.height - 32, 90, 20, CommonComponents.GUI_BACK, button -> {
			if (!this.justClosedPopup) {
				this.minecraft.setScreen(this.lastScreen);
			}
		}));
		this.renewButton = this.addButton(
			new Button(this.width / 2 + 100, this.height - 32, 90, 20, new TranslatableComponent("mco.selectServer.expiredRenew"), button -> this.onRenew())
		);
		this.leaveButton = this.addButton(
			new Button(
				this.width / 2 - 202,
				this.height - 32,
				90,
				20,
				new TranslatableComponent("mco.selectServer.leave"),
				button -> this.leaveClicked(this.findServer(this.selectedServerId))
			)
		);
		this.pendingInvitesButton = this.addButton(new RealmsMainScreen.PendingInvitesButton());
		this.newsButton = this.addButton(new RealmsMainScreen.NewsButton());
		this.showPopupButton = this.addButton(new RealmsMainScreen.ShowPopupButton());
		this.closeButton = this.addButton(new RealmsMainScreen.CloseButton());
		this.createTrialButton = this.addButton(
			new Button(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20, new TranslatableComponent("mco.selectServer.trial"), button -> {
				if (this.trialsAvailable && !this.createdTrial) {
					Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
					this.minecraft.setScreen(this.lastScreen);
				}
			})
		);
		this.buyARealmButton = this.addButton(
			new Button(
				this.width / 2 + 52,
				this.popupY0() + 160 - 20,
				98,
				20,
				new TranslatableComponent("mco.selectServer.buy"),
				button -> Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms")
			)
		);
		RealmsServer realmsServer = this.findServer(this.selectedServerId);
		this.updateButtonStates(realmsServer);
	}

	private void updateButtonStates(@Nullable RealmsServer realmsServer) {
		this.playButton.active = this.shouldPlayButtonBeActive(realmsServer) && !this.shouldShowPopup();
		this.renewButton.visible = this.shouldRenewButtonBeActive(realmsServer);
		this.configureButton.visible = this.shouldConfigureButtonBeVisible(realmsServer);
		this.leaveButton.visible = this.shouldLeaveButtonBeVisible(realmsServer);
		boolean bl = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
		this.createTrialButton.visible = bl;
		this.createTrialButton.active = bl;
		this.buyARealmButton.visible = this.shouldShowPopup();
		this.closeButton.visible = this.shouldShowPopup() && this.popupOpenedByUser;
		this.renewButton.active = !this.shouldShowPopup();
		this.configureButton.active = !this.shouldShowPopup();
		this.leaveButton.active = !this.shouldShowPopup();
		this.newsButton.active = true;
		this.pendingInvitesButton.active = true;
		this.backButton.active = true;
		this.showPopupButton.active = !this.shouldShowPopup();
	}

	private boolean shouldShowPopupButton() {
		return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
	}

	private boolean shouldPlayButtonBeActive(@Nullable RealmsServer realmsServer) {
		return realmsServer != null && !realmsServer.expired && realmsServer.state == RealmsServer.State.OPEN;
	}

	private boolean shouldRenewButtonBeActive(@Nullable RealmsServer realmsServer) {
		return realmsServer != null && realmsServer.expired && this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldConfigureButtonBeVisible(@Nullable RealmsServer realmsServer) {
		return realmsServer != null && this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldLeaveButtonBeVisible(@Nullable RealmsServer realmsServer) {
		return realmsServer != null && !this.isSelfOwnedServer(realmsServer);
	}

	@Override
	public void tick() {
		super.tick();
		this.justClosedPopup = false;
		this.animTick++;
		this.clicks--;
		if (this.clicks < 0) {
			this.clicks = 0;
		}

		if (hasParentalConsent()) {
			REALMS_DATA_FETCHER.init();
			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
				List<RealmsServer> list = REALMS_DATA_FETCHER.getServers();
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
						this.realmSelectionList.addMessageEntry(new RealmsMainScreen.TrialEntry());
					}

					for (RealmsServer realmsServerx : this.realmsServers) {
						this.realmSelectionList.addEntry(new RealmsMainScreen.ServerEntry(realmsServerx));
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

			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
				this.numberOfPendingInvites = REALMS_DATA_FETCHER.getPendingInvitesCount();
				if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
					NarrationHelper.now(I18n.get("mco.configure.world.invite.narration", this.numberOfPendingInvites));
				}
			}

			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
				boolean bl3 = REALMS_DATA_FETCHER.isTrialAvailable();
				if (bl3 != this.trialsAvailable && this.shouldShowPopup()) {
					this.trialsAvailable = bl3;
					this.showingPopup = false;
				} else {
					this.trialsAvailable = bl3;
				}
			}

			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
				RealmsServerPlayerLists realmsServerPlayerLists = REALMS_DATA_FETCHER.getLivestats();

				for (RealmsServerPlayerList realmsServerPlayerList : realmsServerPlayerLists.servers) {
					for (RealmsServer realmsServerx : this.realmsServers) {
						if (realmsServerx.id == realmsServerPlayerList.serverId) {
							realmsServerx.updateServerPing(realmsServerPlayerList);
							break;
						}
					}
				}
			}

			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
				this.hasUnreadNews = REALMS_DATA_FETCHER.hasUnreadNews();
				this.newsLink = REALMS_DATA_FETCHER.newsLink();
			}

			REALMS_DATA_FETCHER.markClean();
			if (this.shouldShowPopup()) {
				this.carouselTick++;
			}

			if (this.showPopupButton != null) {
				this.showPopupButton.visible = this.shouldShowPopupButton();
			}
		}
	}

	private void pingRegions() {
		new Thread(() -> {
			List<RegionPingResult> list = Ping.pingAllRegions();
			RealmsClient realmsClient = RealmsClient.create();
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
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		this.stopRealmsFetcher();
	}

	private void onRenew() {
		RealmsServer realmsServer = this.findServer(this.selectedServerId);
		if (realmsServer != null) {
			String string = "https://aka.ms/ExtendJavaRealms?subscriptionId="
				+ realmsServer.remoteSubscriptionId
				+ "&profileId="
				+ this.minecraft.getUser().getUuid()
				+ "&ref="
				+ (realmsServer.expiredTrial ? "expiredTrial" : "expiredRealm");
			this.minecraft.keyboardHandler.setClipboard(string);
			Util.getPlatform().openUri(string);
		}
	}

	private void checkClientCompatability() {
		if (!checkedClientCompatability) {
			checkedClientCompatability = true;
			(new Thread("MCO Compatability Checker #1") {
					public void run() {
						RealmsClient realmsClient = RealmsClient.create();

						try {
							RealmsClient.CompatibleVersionResponse compatibleVersionResponse = realmsClient.clientCompatible();
							if (compatibleVersionResponse == RealmsClient.CompatibleVersionResponse.OUTDATED) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
								RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
								return;
							}

							if (compatibleVersionResponse == RealmsClient.CompatibleVersionResponse.OTHER) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
								RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
								return;
							}

							RealmsMainScreen.this.checkParentalConsent();
						} catch (RealmsServiceException var3) {
							RealmsMainScreen.checkedClientCompatability = false;
							RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)var3);
							if (var3.httpResultCode == 401) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(
									new TranslatableComponent("mco.error.invalid.session.title"),
									new TranslatableComponent("mco.error.invalid.session.message"),
									RealmsMainScreen.this.lastScreen
								);
								RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
							} else {
								RealmsMainScreen.this.minecraft
									.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen)));
							}
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
					RealmsClient realmsClient = RealmsClient.create();

					try {
						Boolean boolean_ = realmsClient.mcoEnabled();
						if (boolean_) {
							RealmsMainScreen.LOGGER.info("Realms is available for this user");
							RealmsMainScreen.hasParentalConsent = true;
						} else {
							RealmsMainScreen.LOGGER.info("Realms is not available for this user");
							RealmsMainScreen.hasParentalConsent = false;
							RealmsMainScreen.this.minecraft
								.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen)));
						}

						RealmsMainScreen.checkedParentalConsent = true;
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)var3);
						RealmsMainScreen.this.minecraft
							.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen)));
					}
				}
			})
			.start();
	}

	private void switchToStage() {
		if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
			(new Thread("MCO Stage Availability Checker #1") {
				public void run() {
					RealmsClient realmsClient = RealmsClient.create();

					try {
						Boolean boolean_ = realmsClient.stageAvailable();
						if (boolean_) {
							RealmsClient.switchToStage();
							RealmsMainScreen.LOGGER.info("Switched to stage");
							RealmsMainScreen.REALMS_DATA_FETCHER.forceUpdate();
						}
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
					}
				}
			}).start();
		}
	}

	private void switchToLocal() {
		if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
			(new Thread("MCO Local Availability Checker #1") {
				public void run() {
					RealmsClient realmsClient = RealmsClient.create();

					try {
						Boolean boolean_ = realmsClient.stageAvailable();
						if (boolean_) {
							RealmsClient.switchToLocal();
							RealmsMainScreen.LOGGER.info("Switched to local");
							RealmsMainScreen.REALMS_DATA_FETCHER.forceUpdate();
						}
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
					}
				}
			}).start();
		}
	}

	private void switchToProd() {
		RealmsClient.switchToProd();
		REALMS_DATA_FETCHER.forceUpdate();
	}

	private void stopRealmsFetcher() {
		REALMS_DATA_FETCHER.stop();
	}

	private void configureClicked(RealmsServer realmsServer) {
		if (this.minecraft.getUser().getUuid().equals(realmsServer.ownerUUID) || overrideConfigure) {
			this.saveListScrollPosition();
			this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, realmsServer.id));
		}
	}

	private void leaveClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && !this.minecraft.getUser().getUuid().equals(realmsServer.ownerUUID)) {
			this.saveListScrollPosition();
			Component component = new TranslatableComponent("mco.configure.world.leave.question.line1");
			Component component2 = new TranslatableComponent("mco.configure.world.leave.question.line2");
			this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::leaveServer, RealmsLongConfirmationScreen.Type.Info, component, component2, true));
		}
	}

	private void saveListScrollPosition() {
		lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
	}

	@Nullable
	private RealmsServer findServer(long l) {
		for (RealmsServer realmsServer : this.realmsServers) {
			if (realmsServer.id == l) {
				return realmsServer;
			}
		}

		return null;
	}

	private void leaveServer(boolean bl) {
		if (bl) {
			(new Thread("Realms-leave-server") {
					public void run() {
						try {
							RealmsServer realmsServer = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
							if (realmsServer != null) {
								RealmsClient realmsClient = RealmsClient.create();
								realmsClient.uninviteMyselfFrom(realmsServer.id);
								RealmsMainScreen.REALMS_DATA_FETCHER.removeItem(realmsServer);
								RealmsMainScreen.this.realmsServers.remove(realmsServer);
								RealmsMainScreen.this.realmSelectionList
									.children()
									.removeIf(
										entry -> entry instanceof RealmsMainScreen.ServerEntry
												&& ((RealmsMainScreen.ServerEntry)entry).serverData.id == RealmsMainScreen.this.selectedServerId
									);
								RealmsMainScreen.this.realmSelectionList.setSelected(null);
								RealmsMainScreen.this.updateButtonStates(null);
								RealmsMainScreen.this.selectedServerId = -1L;
								RealmsMainScreen.this.playButton.active = false;
							}
						} catch (RealmsServiceException var3) {
							RealmsMainScreen.LOGGER.error("Couldn't configure world");
							RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this)));
						}
					}
				})
				.start();
		}

		this.minecraft.setScreen(this);
	}

	public void removeSelection() {
		this.selectedServerId = -1L;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.keyCombos.forEach(KeyCombo::reset);
			this.onClosePopup();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private void onClosePopup() {
		if (this.shouldShowPopup() && this.popupOpenedByUser) {
			this.popupOpenedByUser = false;
		} else {
			this.minecraft.setScreen(this.lastScreen);
		}
	}

	@Override
	public boolean charTyped(char c, int i) {
		this.keyCombos.forEach(keyCombo -> keyCombo.keyPressed(c));
		return true;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.hoveredElement = RealmsMainScreen.HoveredElement.NONE;
		this.toolTip = null;
		this.renderBackground(poseStack);
		this.realmSelectionList.render(poseStack, i, j, f);
		this.drawRealmsLogo(poseStack, this.width / 2 - 50, 7);
		if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
			this.renderStage(poseStack);
		}

		if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
			this.renderLocal(poseStack);
		}

		if (this.shouldShowPopup()) {
			this.drawPopup(poseStack, i, j);
		} else {
			if (this.showingPopup) {
				this.updateButtonStates(null);
				if (!this.children.contains(this.realmSelectionList)) {
					this.children.add(this.realmSelectionList);
				}

				RealmsServer realmsServer = this.findServer(this.selectedServerId);
				this.playButton.active = this.shouldPlayButtonBeActive(realmsServer);
			}

			this.showingPopup = false;
		}

		super.render(poseStack, i, j, f);
		if (this.toolTip != null) {
			this.renderMousehoverTooltip(poseStack, this.toolTip, i, j);
		}

		if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
			this.minecraft.getTextureManager().bind(TRIAL_ICON_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			int k = 8;
			int l = 8;
			int m = 0;
			if ((Util.getMillis() / 800L & 1L) == 1L) {
				m = 8;
			}

			GuiComponent.blit(
				poseStack,
				this.createTrialButton.x + this.createTrialButton.getWidth() - 8 - 4,
				this.createTrialButton.y + this.createTrialButton.getHeight() / 2 - 4,
				0.0F,
				(float)m,
				8,
				8,
				8,
				16
			);
		}
	}

	private void drawRealmsLogo(PoseStack poseStack, int i, int j) {
		this.minecraft.getTextureManager().bind(LOGO_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(0.5F, 0.5F, 0.5F);
		GuiComponent.blit(poseStack, i * 2, j * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
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

	private void drawPopup(PoseStack poseStack, int i, int j) {
		int k = this.popupX0();
		int l = this.popupY0();
		Component component = new TranslatableComponent("mco.selectServer.popup");
		List<FormattedText> list = this.font.split(component, 100);
		if (!this.showingPopup) {
			this.carouselIndex = 0;
			this.carouselTick = 0;
			this.hasSwitchedCarouselImage = true;
			this.updateButtonStates(null);
			if (this.children.contains(this.realmSelectionList)) {
				GuiEventListener guiEventListener = this.realmSelectionList;
				if (!this.children.remove(guiEventListener)) {
					LOGGER.error("Unable to remove widget: " + guiEventListener);
				}
			}

			NarrationHelper.now(component.getString());
		}

		if (this.hasFetchedServers) {
			this.showingPopup = true;
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
		RenderSystem.enableBlend();
		this.minecraft.getTextureManager().bind(DARKEN_LOCATION);
		int m = 0;
		int n = 32;
		GuiComponent.blit(poseStack, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
		RenderSystem.disableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(POPUP_LOCATION);
		GuiComponent.blit(poseStack, k, l, 0.0F, 0.0F, 310, 166, 310, 166);
		if (!teaserImages.isEmpty()) {
			this.minecraft.getTextureManager().bind((ResourceLocation)teaserImages.get(this.carouselIndex));
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, k + 7, l + 7, 0.0F, 0.0F, 195, 152, 195, 152);
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

		for (FormattedText formattedText : list) {
			o++;
			int p = l + 10 * o - 3;
			this.font.draw(poseStack, formattedText, (float)(this.width / 2 + 52), (float)p, 5000268);
		}
	}

	private int popupX0() {
		return (this.width - 310) / 2;
	}

	private int popupY0() {
		return this.height / 2 - 80;
	}

	private void drawInvitationPendingIcon(PoseStack poseStack, int i, int j, int k, int l, boolean bl, boolean bl2) {
		int m = this.numberOfPendingInvites;
		boolean bl3 = this.inPendingInvitationArea((double)i, (double)j);
		boolean bl4 = bl2 && bl;
		if (bl4) {
			float f = 0.25F + (1.0F + Mth.sin((float)this.animTick * 0.5F)) * 0.25F;
			int n = 0xFF000000 | (int)(f * 64.0F) << 16 | (int)(f * 64.0F) << 8 | (int)(f * 64.0F) << 0;
			this.fillGradient(poseStack, k - 2, l - 2, k + 18, l + 18, n, n);
			n = 0xFF000000 | (int)(f * 255.0F) << 16 | (int)(f * 255.0F) << 8 | (int)(f * 255.0F) << 0;
			this.fillGradient(poseStack, k - 2, l - 2, k + 18, l - 1, n, n);
			this.fillGradient(poseStack, k - 2, l - 2, k - 1, l + 18, n, n);
			this.fillGradient(poseStack, k + 17, l - 2, k + 18, l + 18, n, n);
			this.fillGradient(poseStack, k - 2, l + 17, k + 18, l + 18, n, n);
		}

		this.minecraft.getTextureManager().bind(INVITE_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean bl5 = bl2 && bl;
		float g = bl5 ? 16.0F : 0.0F;
		GuiComponent.blit(poseStack, k, l - 6, g, 0.0F, 15, 25, 31, 25);
		boolean bl6 = bl2 && m != 0;
		if (bl6) {
			int o = (Math.min(m, 6) - 1) * 8;
			int p = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
			this.minecraft.getTextureManager().bind(INVITATION_ICONS_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			float h = bl3 ? 8.0F : 0.0F;
			GuiComponent.blit(poseStack, k + 4, l + 4 + p, (float)o, h, 8, 8, 48, 16);
		}

		int o = i + 12;
		boolean bl7 = bl2 && bl3;
		if (bl7) {
			String string = m == 0 ? "mco.invites.nopending" : "mco.invites.pending";
			String string2 = I18n.get(string);
			int q = this.font.width(string2);
			this.fillGradient(poseStack, o - 3, j - 3, o + q + 3, j + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(poseStack, string2, (float)o, (float)j, -1);
		}
	}

	private boolean inPendingInvitationArea(double d, double e) {
		int i = this.width / 2 + 50;
		int j = this.width / 2 + 66;
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

	public void play(RealmsServer realmsServer, Screen screen) {
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
			this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(screen, new GetServerDetailsTask(this, screen, realmsServer, this.connectLock)));
		}
	}

	private boolean isSelfOwnedServer(RealmsServer realmsServer) {
		return realmsServer.ownerUUID != null && realmsServer.ownerUUID.equals(this.minecraft.getUser().getUuid());
	}

	private boolean isSelfOwnedNonExpiredServer(RealmsServer realmsServer) {
		return this.isSelfOwnedServer(realmsServer) && !realmsServer.expired;
	}

	private void drawExpired(PoseStack poseStack, int i, int j, int k, int l) {
		this.minecraft.getTextureManager().bind(EXPIRED_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltip(new TranslatableComponent("mco.selectServer.expired"));
		}
	}

	private void drawExpiring(PoseStack poseStack, int i, int j, int k, int l, int m) {
		this.minecraft.getTextureManager().bind(EXPIRES_SOON_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.animTick % 20 < 10) {
			GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 20, 28);
		} else {
			GuiComponent.blit(poseStack, i, j, 10.0F, 0.0F, 10, 28, 20, 28);
		}

		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			if (m <= 0) {
				this.setTooltip(new TranslatableComponent("mco.selectServer.expires.soon"));
			} else if (m == 1) {
				this.setTooltip(new TranslatableComponent("mco.selectServer.expires.day"));
			} else {
				this.setTooltip(new TranslatableComponent("mco.selectServer.expires.days", m));
			}
		}
	}

	private void drawOpen(PoseStack poseStack, int i, int j, int k, int l) {
		this.minecraft.getTextureManager().bind(ON_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltip(new TranslatableComponent("mco.selectServer.open"));
		}
	}

	private void drawClose(PoseStack poseStack, int i, int j, int k, int l) {
		this.minecraft.getTextureManager().bind(OFF_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltip(new TranslatableComponent("mco.selectServer.closed"));
		}
	}

	private void drawLeave(PoseStack poseStack, int i, int j, int k, int l) {
		boolean bl = false;
		if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			bl = true;
		}

		this.minecraft.getTextureManager().bind(LEAVE_ICON_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = bl ? 28.0F : 0.0F;
		GuiComponent.blit(poseStack, i, j, f, 0.0F, 28, 28, 56, 28);
		if (bl) {
			this.setTooltip(new TranslatableComponent("mco.selectServer.leave"));
			this.hoveredElement = RealmsMainScreen.HoveredElement.LEAVE;
		}
	}

	private void drawConfigure(PoseStack poseStack, int i, int j, int k, int l) {
		boolean bl = false;
		if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			bl = true;
		}

		this.minecraft.getTextureManager().bind(CONFIGURE_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = bl ? 28.0F : 0.0F;
		GuiComponent.blit(poseStack, i, j, f, 0.0F, 28, 28, 56, 28);
		if (bl) {
			this.setTooltip(new TranslatableComponent("mco.selectServer.configure"));
			this.hoveredElement = RealmsMainScreen.HoveredElement.CONFIGURE;
		}
	}

	protected void renderMousehoverTooltip(PoseStack poseStack, List<Component> list, int i, int j) {
		if (!list.isEmpty()) {
			int k = 0;
			int l = 0;

			for (Component component : list) {
				int m = this.font.width(component);
				if (m > l) {
					l = m;
				}
			}

			int n = i - l - 5;
			int o = j;
			if (n < 0) {
				n = i + 12;
			}

			for (Component component2 : list) {
				int p = o - (k == 0 ? 3 : 0) + k;
				this.fillGradient(poseStack, n - 3, p, n + l + 3, o + 8 + 3 + k, -1073741824, -1073741824);
				this.font.drawShadow(poseStack, component2, (float)n, (float)(o + k), 16777215);
				k += 10;
			}
		}
	}

	private void renderMoreInfo(PoseStack poseStack, int i, int j, int k, int l, boolean bl) {
		boolean bl2 = false;
		if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
			bl2 = true;
		}

		this.minecraft.getTextureManager().bind(QUESTIONMARK_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = bl ? 20.0F : 0.0F;
		GuiComponent.blit(poseStack, k, l, f, 0.0F, 20, 20, 40, 20);
		if (bl2) {
			this.setTooltip(new TranslatableComponent("mco.selectServer.info"));
		}
	}

	private void renderNews(PoseStack poseStack, int i, int j, boolean bl, int k, int l, boolean bl2, boolean bl3) {
		boolean bl4 = false;
		if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
			bl4 = true;
		}

		this.minecraft.getTextureManager().bind(NEWS_LOCATION);
		if (bl3) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
		}

		boolean bl5 = bl3 && bl2;
		float f = bl5 ? 20.0F : 0.0F;
		GuiComponent.blit(poseStack, k, l, f, 0.0F, 20, 20, 40, 20);
		if (bl4 && bl3) {
			this.setTooltip(new TranslatableComponent("mco.news"));
		}

		if (bl && bl3) {
			int m = bl4 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
			this.minecraft.getTextureManager().bind(INVITATION_ICONS_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, k + 10, l + 2 + m, 40.0F, 0.0F, 8, 8, 48, 16);
		}
	}

	private void renderLocal(PoseStack poseStack) {
		String string = "LOCAL!";
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
		RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.scalef(1.5F, 1.5F, 1.5F);
		this.font.draw(poseStack, "LOCAL!", 0.0F, 0.0F, 8388479);
		RenderSystem.popMatrix();
	}

	private void renderStage(PoseStack poseStack) {
		String string = "STAGE!";
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
		RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.scalef(1.5F, 1.5F, 1.5F);
		this.font.draw(poseStack, "STAGE!", 0.0F, 0.0F, -256);
		RenderSystem.popMatrix();
	}

	public RealmsMainScreen newScreen() {
		RealmsMainScreen realmsMainScreen = new RealmsMainScreen(this.lastScreen);
		realmsMainScreen.init(this.minecraft, this.width, this.height);
		return realmsMainScreen;
	}

	public static void updateTeaserImages(ResourceManager resourceManager) {
		Collection<ResourceLocation> collection = resourceManager.listResources("textures/gui/images", string -> string.endsWith(".png"));
		teaserImages = (List<ResourceLocation>)collection.stream()
			.filter(resourceLocation -> resourceLocation.getNamespace().equals("realms"))
			.collect(ImmutableList.toImmutableList());
	}

	private void setTooltip(Component... components) {
		this.toolTip = Arrays.asList(components);
	}

	private void pendingButtonPress(Button button) {
		this.minecraft.setScreen(new RealmsPendingInvitesScreen(this.lastScreen));
	}

	@Environment(EnvType.CLIENT)
	class CloseButton extends Button {
		public CloseButton() {
			super(
				RealmsMainScreen.this.popupX0() + 4,
				RealmsMainScreen.this.popupY0() + 4,
				12,
				12,
				new TranslatableComponent("mco.selectServer.close"),
				button -> RealmsMainScreen.this.onClosePopup()
			);
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.CROSS_ICON_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			float g = this.isHovered() ? 12.0F : 0.0F;
			blit(poseStack, this.x, this.y, 0.0F, g, 12, 12, 12, 24);
			if (this.isMouseOver((double)i, (double)j)) {
				RealmsMainScreen.this.setTooltip(this.getMessage());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
		private Entry() {
		}
	}

	@Environment(EnvType.CLIENT)
	static enum HoveredElement {
		NONE,
		EXPIRED,
		LEAVE,
		CONFIGURE;
	}

	@Environment(EnvType.CLIENT)
	class NewsButton extends Button {
		public NewsButton() {
			super(RealmsMainScreen.this.width - 62, 6, 20, 20, TextComponent.EMPTY, button -> {
				if (RealmsMainScreen.this.newsLink != null) {
					Util.getPlatform().openUri(RealmsMainScreen.this.newsLink);
					if (RealmsMainScreen.this.hasUnreadNews) {
						RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
						realmsPersistenceData.hasUnreadNews = false;
						RealmsMainScreen.this.hasUnreadNews = false;
						RealmsPersistence.writeFile(realmsPersistenceData);
					}
				}
			});
			this.setMessage(new TranslatableComponent("mco.news"));
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RealmsMainScreen.this.renderNews(poseStack, i, j, RealmsMainScreen.this.hasUnreadNews, this.x, this.y, this.isHovered(), this.active);
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInvitesButton extends Button implements TickableWidget {
		public PendingInvitesButton() {
			super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, TextComponent.EMPTY, button -> RealmsMainScreen.this.pendingButtonPress(button));
		}

		@Override
		public void tick() {
			this.setMessage(new TranslatableComponent(RealmsMainScreen.this.numberOfPendingInvites == 0 ? "mco.invites.nopending" : "mco.invites.pending"));
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RealmsMainScreen.this.drawInvitationPendingIcon(poseStack, i, j, this.x, this.y, this.isHovered(), this.active);
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
		private boolean showingMessage;

		public RealmSelectionList() {
			super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 40, 36);
		}

		@Override
		public void clear() {
			super.clear();
			this.showingMessage = false;
		}

		public int addMessageEntry(RealmsMainScreen.Entry entry) {
			this.showingMessage = true;
			return this.addEntry(entry);
		}

		@Override
		public boolean isFocused() {
			return RealmsMainScreen.this.getFocused() == this;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (i != 257 && i != 32 && i != 335) {
				return super.keyPressed(i, j, k);
			} else {
				ObjectSelectionList.Entry entry = this.getSelected();
				return entry == null ? super.keyPressed(i, j, k) : entry.mouseClicked(0.0, 0.0, 0);
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (i == 0 && d < (double)this.getScrollbarPosition() && e >= (double)this.y0 && e <= (double)this.y1) {
				int j = RealmsMainScreen.this.realmSelectionList.getRowLeft();
				int k = this.getScrollbarPosition();
				int l = (int)Math.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
				int m = l / this.itemHeight;
				if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
					this.itemClicked(l, m, d, e, this.width);
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
			this.setSelectedItem(i);
			if (i != -1) {
				RealmsServer realmsServer;
				if (this.showingMessage) {
					if (i == 0) {
						NarrationHelper.now(I18n.get("mco.trial.message.line1"), I18n.get("mco.trial.message.line2"));
						realmsServer = null;
					} else {
						if (i - 1 >= RealmsMainScreen.this.realmsServers.size()) {
							RealmsMainScreen.this.selectedServerId = -1L;
							return;
						}

						realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i - 1);
					}
				} else {
					if (i >= RealmsMainScreen.this.realmsServers.size()) {
						RealmsMainScreen.this.selectedServerId = -1L;
						return;
					}

					realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i);
				}

				RealmsMainScreen.this.updateButtonStates(realmsServer);
				if (realmsServer == null) {
					RealmsMainScreen.this.selectedServerId = -1L;
				} else if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
					NarrationHelper.now(I18n.get("mco.selectServer.uninitialized") + I18n.get("mco.gui.button"));
					RealmsMainScreen.this.selectedServerId = -1L;
				} else {
					RealmsMainScreen.this.selectedServerId = realmsServer.id;
					if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active) {
						RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
					}

					NarrationHelper.now(I18n.get("narrator.select", realmsServer.name));
				}
			}
		}

		public void setSelected(@Nullable RealmsMainScreen.Entry entry) {
			super.setSelected(entry);
			int i = this.children().indexOf(entry);
			if (!this.showingMessage || i > 0) {
				RealmsServer realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i - (this.showingMessage ? 1 : 0));
				RealmsMainScreen.this.selectedServerId = realmsServer.id;
				RealmsMainScreen.this.updateButtonStates(realmsServer);
			}
		}

		@Override
		public void itemClicked(int i, int j, double d, double e, int k) {
			if (this.showingMessage) {
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
						Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(realmsServer, RealmsMainScreen.this));
					} else {
						RealmsMainScreen.this.selectedServerId = realmsServer.id;
					}

					if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.CONFIGURE) {
						RealmsMainScreen.this.selectedServerId = realmsServer.id;
						RealmsMainScreen.this.configureClicked(realmsServer);
					} else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.LEAVE) {
						RealmsMainScreen.this.selectedServerId = realmsServer.id;
						RealmsMainScreen.this.leaveClicked(realmsServer);
					} else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.EXPIRED) {
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
	class ServerEntry extends RealmsMainScreen.Entry {
		private final RealmsServer serverData;

		public ServerEntry(RealmsServer realmsServer) {
			this.serverData = realmsServer;
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderMcoServerItem(this.serverData, poseStack, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
				RealmsMainScreen.this.selectedServerId = -1L;
				RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
			} else {
				RealmsMainScreen.this.selectedServerId = this.serverData.id;
			}

			return true;
		}

		private void renderMcoServerItem(RealmsServer realmsServer, PoseStack poseStack, int i, int j, int k, int l) {
			this.renderLegacy(realmsServer, poseStack, i + 36, j, k, l);
		}

		private void renderLegacy(RealmsServer realmsServer, PoseStack poseStack, int i, int j, int k, int l) {
			if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
				RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.WORLDICON_LOCATION);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableAlphaTest();
				GuiComponent.blit(poseStack, i + 10, j + 6, 0.0F, 0.0F, 40, 20, 40, 20);
				float f = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
				int m = 0xFF000000 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
				RealmsMainScreen.this.drawCenteredString(poseStack, RealmsMainScreen.this.font, I18n.get("mco.selectServer.uninitialized"), i + 10 + 40 + 75, j + 12, m);
			} else {
				int n = 225;
				int m = 2;
				if (realmsServer.expired) {
					RealmsMainScreen.this.drawExpired(poseStack, i + 225 - 14, j + 2, k, l);
				} else if (realmsServer.state == RealmsServer.State.CLOSED) {
					RealmsMainScreen.this.drawClose(poseStack, i + 225 - 14, j + 2, k, l);
				} else if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.daysLeft < 7) {
					RealmsMainScreen.this.drawExpiring(poseStack, i + 225 - 14, j + 2, k, l, realmsServer.daysLeft);
				} else if (realmsServer.state == RealmsServer.State.OPEN) {
					RealmsMainScreen.this.drawOpen(poseStack, i + 225 - 14, j + 2, k, l);
				}

				if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && !RealmsMainScreen.overrideConfigure) {
					RealmsMainScreen.this.drawLeave(poseStack, i + 225, j + 2, k, l);
				} else {
					RealmsMainScreen.this.drawConfigure(poseStack, i + 225, j + 2, k, l);
				}

				if (!"0".equals(realmsServer.serverPing.nrOfPlayers)) {
					String string = ChatFormatting.GRAY + "" + realmsServer.serverPing.nrOfPlayers;
					RealmsMainScreen.this.font.draw(poseStack, string, (float)(i + 207 - RealmsMainScreen.this.font.width(string)), (float)(j + 3), 8421504);
					if (k >= i + 207 - RealmsMainScreen.this.font.width(string)
						&& k <= i + 207
						&& l >= j + 1
						&& l <= j + 10
						&& l < RealmsMainScreen.this.height - 40
						&& l > 32
						&& !RealmsMainScreen.this.shouldShowPopup()) {
						RealmsMainScreen.this.setTooltip(new TextComponent(realmsServer.serverPing.playerList));
					}
				}

				if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.expired) {
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.enableBlend();
					RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.BUTTON_LOCATION);
					RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					String string = I18n.get("mco.selectServer.expiredList");
					String string2 = I18n.get("mco.selectServer.expiredRenew");
					if (realmsServer.expiredTrial) {
						string = I18n.get("mco.selectServer.expiredTrial");
						string2 = I18n.get("mco.selectServer.expiredSubscribe");
					}

					int o = RealmsMainScreen.this.font.width(string2) + 17;
					int p = 16;
					int q = i + RealmsMainScreen.this.font.width(string) + 8;
					int r = j + 13;
					boolean bl = false;
					if (k >= q && k < q + o && l > r && l <= r + 16 & l < RealmsMainScreen.this.height - 40 && l > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
						bl = true;
						RealmsMainScreen.this.hoveredElement = RealmsMainScreen.HoveredElement.EXPIRED;
					}

					int s = bl ? 2 : 1;
					GuiComponent.blit(poseStack, q, r, 0.0F, (float)(46 + s * 20), o / 2, 8, 256, 256);
					GuiComponent.blit(poseStack, q + o / 2, r, (float)(200 - o / 2), (float)(46 + s * 20), o / 2, 8, 256, 256);
					GuiComponent.blit(poseStack, q, r + 8, 0.0F, (float)(46 + s * 20 + 12), o / 2, 8, 256, 256);
					GuiComponent.blit(poseStack, q + o / 2, r + 8, (float)(200 - o / 2), (float)(46 + s * 20 + 12), o / 2, 8, 256, 256);
					RenderSystem.disableBlend();
					int t = j + 11 + 5;
					int u = bl ? 16777120 : 16777215;
					RealmsMainScreen.this.font.draw(poseStack, string, (float)(i + 2), (float)(t + 1), 15553363);
					RealmsMainScreen.this.drawCenteredString(poseStack, RealmsMainScreen.this.font, string2, q + o / 2, t + 1, u);
				} else {
					if (realmsServer.worldType == RealmsServer.WorldType.MINIGAME) {
						int v = 13413468;
						String string2x = I18n.get("mco.selectServer.minigame") + " ";
						int o = RealmsMainScreen.this.font.width(string2x);
						RealmsMainScreen.this.font.draw(poseStack, string2x, (float)(i + 2), (float)(j + 12), 13413468);
						RealmsMainScreen.this.font.draw(poseStack, realmsServer.getMinigameName(), (float)(i + 2 + o), (float)(j + 12), 7105644);
					} else {
						RealmsMainScreen.this.font.draw(poseStack, realmsServer.getDescription(), (float)(i + 2), (float)(j + 12), 7105644);
					}

					if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer)) {
						RealmsMainScreen.this.font.draw(poseStack, realmsServer.owner, (float)(i + 2), (float)(j + 12 + 11), 5000268);
					}
				}

				RealmsMainScreen.this.font.draw(poseStack, realmsServer.getName(), (float)(i + 2), (float)(j + 1), 16777215);
				RealmsTextureManager.withBoundFace(realmsServer.ownerUUID, () -> {
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					GuiComponent.blit(poseStack, i - 36, j, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
					GuiComponent.blit(poseStack, i - 36, j, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);
				});
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ShowPopupButton extends Button {
		public ShowPopupButton() {
			super(
				RealmsMainScreen.this.width - 37,
				6,
				20,
				20,
				new TranslatableComponent("mco.selectServer.info"),
				button -> RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser
			);
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RealmsMainScreen.this.renderMoreInfo(poseStack, i, j, this.x, this.y, this.isHovered());
		}
	}

	@Environment(EnvType.CLIENT)
	class TrialEntry extends RealmsMainScreen.Entry {
		private TrialEntry() {
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderTrialItem(poseStack, i, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsMainScreen.this.popupOpenedByUser = true;
			return true;
		}

		private void renderTrialItem(PoseStack poseStack, int i, int j, int k, int l, int m) {
			int n = k + 8;
			int o = 0;
			String string = I18n.get("mco.trial.message.line1") + "\\n" + I18n.get("mco.trial.message.line2");
			boolean bl = false;
			if (j <= l && l <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && k <= m && m <= k + 32) {
				bl = true;
			}

			int p = 8388479;
			if (bl && !RealmsMainScreen.this.shouldShowPopup()) {
				p = 6077788;
			}

			for (String string2 : string.split("\\\\n")) {
				RealmsMainScreen.this.drawCenteredString(poseStack, RealmsMainScreen.this.font, string2, RealmsMainScreen.this.width / 2, n + o, p);
				o += 10;
			}
		}
	}
}
