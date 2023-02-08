package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsNewsManager;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
	private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
	private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
	private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
	private static final ResourceLocation LEAVE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/leave_icon.png");
	private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
	private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
	static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
	private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
	private static final ResourceLocation CONFIGURE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/configure_icon.png");
	private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
	private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
	private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
	static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
	private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
	static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("minecraft", "textures/gui/widgets.png");
	static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
	static final Component PENDING_INVITES_TEXT = Component.translatable("mco.invites.pending");
	static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(
		Component.translatable("mco.trial.message.line1"), Component.translatable("mco.trial.message.line2")
	);
	static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
	static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
	static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
	static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
	static final Component SUBSCRIPTION_CREATE_TEXT = Component.translatable("mco.selectServer.expiredSubscribe");
	static final Component SELECT_MINIGAME_PREFIX = Component.translatable("mco.selectServer.minigame").append(CommonComponents.SPACE);
	private static final Component POPUP_TEXT = Component.translatable("mco.selectServer.popup");
	private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
	private static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
	private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
	private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
	private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
	private static final Component LEAVE_SERVER_TOOLTIP = Component.translatable("mco.selectServer.leave");
	private static final Component CONFIGURE_SERVER_TOOLTIP = Component.translatable("mco.selectServer.configureRealm");
	private static final Component NEWS_TOOLTIP = Component.translatable("mco.news");
	static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
	static final Component TRIAL_TEXT = CommonComponents.joinLines(TRIAL_MESSAGE_LINES);
	private static List<ResourceLocation> teaserImages = ImmutableList.of();
	@Nullable
	private DataFetcher.Subscription dataSubscription;
	private RealmsServerList serverList;
	static boolean overrideConfigure;
	private static int lastScrollYPosition = -1;
	static volatile boolean hasParentalConsent;
	static volatile boolean checkedParentalConsent;
	static volatile boolean checkedClientCompatability;
	@Nullable
	static Screen realmsGenericErrorScreen;
	private static boolean regionsPinged;
	private final RateLimiter inviteNarrationLimiter;
	private boolean dontSetConnectedToRealms;
	final Screen lastScreen;
	RealmsMainScreen.RealmSelectionList realmSelectionList;
	private boolean realmsSelectionListAdded;
	private Button playButton;
	private Button backButton;
	private Button renewButton;
	private Button configureButton;
	private Button leaveButton;
	private List<RealmsServer> realmsServers = ImmutableList.of();
	volatile int numberOfPendingInvites;
	int animTick;
	private boolean hasFetchedServers;
	boolean popupOpenedByUser;
	private boolean justClosedPopup;
	private volatile boolean trialsAvailable;
	private volatile boolean createdTrial;
	private volatile boolean showingPopup;
	volatile boolean hasUnreadNews;
	@Nullable
	volatile String newsLink;
	private int carouselIndex;
	private int carouselTick;
	private boolean hasSwitchedCarouselImage;
	private List<KeyCombo> keyCombos;
	long lastClickTime;
	private ReentrantLock connectLock = new ReentrantLock();
	private MultiLineLabel formattedPopup = MultiLineLabel.EMPTY;
	RealmsMainScreen.HoveredElement hoveredElement;
	private Button showPopupButton;
	private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
	private Button newsButton;
	private Button createTrialButton;
	private Button buyARealmButton;
	private Button closeButton;

	public RealmsMainScreen(Screen screen) {
		super(GameNarrator.NO_TITLE);
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
		} else {
			return this.popupOpenedByUser ? true : this.realmsServers.isEmpty();
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
			if (!this.dontSetConnectedToRealms) {
				this.minecraft.setConnectedToRealms(false);
			}

			this.showingPopup = false;
			this.addTopButtons();
			this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
			if (lastScrollYPosition != -1) {
				this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
			}

			this.addWidget(this.realmSelectionList);
			this.realmsSelectionListAdded = true;
			this.setInitialFocus(this.realmSelectionList);
			this.addMiddleButtons();
			this.addBottomButtons();
			this.updateButtonStates(null);
			this.formattedPopup = MultiLineLabel.create(this.font, POPUP_TEXT, 100);
			RealmsNewsManager realmsNewsManager = this.minecraft.realmsDataFetcher().newsManager;
			this.hasUnreadNews = realmsNewsManager.hasUnreadNews();
			this.newsLink = realmsNewsManager.newsLink();
			if (this.serverList == null) {
				this.serverList = new RealmsServerList(this.minecraft);
			}

			if (this.dataSubscription != null) {
				this.dataSubscription.forceUpdate();
			}
		}
	}

	private static boolean hasParentalConsent() {
		return checkedParentalConsent && hasParentalConsent;
	}

	public void addTopButtons() {
		this.pendingInvitesButton = this.addRenderableWidget(new RealmsMainScreen.PendingInvitesButton());
		this.newsButton = this.addRenderableWidget(new RealmsMainScreen.NewsButton());
		this.showPopupButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.selectServer.purchase"), button -> this.popupOpenedByUser = !this.popupOpenedByUser)
				.bounds(this.width - 90, 6, 80, 20)
				.build()
		);
	}

	public void addMiddleButtons() {
		this.createTrialButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.trial"), button -> {
			if (this.trialsAvailable && !this.createdTrial) {
				Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
				this.minecraft.setScreen(this.lastScreen);
			}
		}).bounds(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20).build());
		this.buyARealmButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.selectServer.buy"), button -> Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms"))
				.bounds(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20)
				.build()
		);
		this.closeButton = this.addRenderableWidget(new RealmsMainScreen.CloseButton());
	}

	public void addBottomButtons() {
		this.configureButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.selectServer.configure"), button -> this.configureClicked(this.getSelectedServer()))
				.bounds(this.width / 2 - 190, this.height - 32, 90, 20)
				.build()
		);
		this.leaveButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.selectServer.leave"), button -> this.leaveClicked(this.getSelectedServer()))
				.bounds(this.width / 2 - 190, this.height - 32, 90, 20)
				.build()
		);
		this.playButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.selectServer.play"), button -> this.play(this.getSelectedServer(), this))
				.bounds(this.width / 2 - 93, this.height - 32, 90, 20)
				.build()
		);
		this.backButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
			if (!this.justClosedPopup) {
				this.minecraft.setScreen(this.lastScreen);
			}
		}).bounds(this.width / 2 + 4, this.height - 32, 90, 20).build());
		this.renewButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.selectServer.expiredRenew"), button -> this.onRenew(this.getSelectedServer()))
				.bounds(this.width / 2 + 100, this.height - 32, 90, 20)
				.build()
		);
	}

	void updateButtonStates(@Nullable RealmsServer realmsServer) {
		this.backButton.active = true;
		if (hasParentalConsent() && this.hasFetchedServers) {
			this.playButton.visible = true;
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
			this.newsButton.visible = this.newsLink != null;
			this.pendingInvitesButton.active = true;
			this.pendingInvitesButton.visible = true;
			this.showPopupButton.active = !this.shouldShowPopup();
		} else {
			hideWidgets(
				new AbstractWidget[]{
					this.playButton,
					this.renewButton,
					this.configureButton,
					this.createTrialButton,
					this.buyARealmButton,
					this.closeButton,
					this.newsButton,
					this.pendingInvitesButton,
					this.showPopupButton,
					this.leaveButton
				}
			);
		}
	}

	private boolean shouldShowPopupButton() {
		return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
	}

	boolean shouldPlayButtonBeActive(@Nullable RealmsServer realmsServer) {
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
		if (this.pendingInvitesButton != null) {
			this.pendingInvitesButton.tick();
		}

		this.justClosedPopup = false;
		this.animTick++;
		boolean bl = hasParentalConsent();
		if (this.dataSubscription == null && bl) {
			this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
		} else if (this.dataSubscription != null && !bl) {
			this.dataSubscription = null;
		}

		if (this.dataSubscription != null) {
			this.dataSubscription.tick();
		}

		if (this.shouldShowPopup()) {
			this.carouselTick++;
		}

		if (this.showPopupButton != null) {
			this.showPopupButton.visible = this.shouldShowPopupButton();
			this.showPopupButton.active = this.showPopupButton.visible;
		}
	}

	private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher realmsDataFetcher) {
		DataFetcher.Subscription subscription = realmsDataFetcher.dataFetcher.createSubscription();
		subscription.subscribe(realmsDataFetcher.serverListUpdateTask, list -> {
			List<RealmsServer> list2 = this.serverList.updateServersList(list);
			RealmsServer realmsServer = this.getSelectedServer();
			RealmsMainScreen.Entry entry = null;
			this.realmSelectionList.clear();
			boolean bl = !this.hasFetchedServers;
			if (bl) {
				this.hasFetchedServers = true;
			}

			boolean bl2 = false;

			for (RealmsServer realmsServer2 : list2) {
				if (this.isSelfOwnedNonExpiredServer(realmsServer2)) {
					bl2 = true;
				}
			}

			this.realmsServers = list2;
			if (this.shouldShowMessageInList()) {
				this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
			}

			for (RealmsServer realmsServer2x : this.realmsServers) {
				RealmsMainScreen.ServerEntry serverEntry = new RealmsMainScreen.ServerEntry(realmsServer2x);
				this.realmSelectionList.addEntry(serverEntry);
				if (realmsServer != null && realmsServer.id == realmsServer2x.id) {
					entry = serverEntry;
				}
			}

			if (!regionsPinged && bl2) {
				regionsPinged = true;
				this.pingRegions();
			}

			if (bl) {
				this.updateButtonStates(null);
			} else {
				this.realmSelectionList.setSelected(entry);
			}
		});
		subscription.subscribe(realmsDataFetcher.pendingInvitesTask, integer -> {
			this.numberOfPendingInvites = integer;
			if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
				this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", this.numberOfPendingInvites));
			}
		});
		subscription.subscribe(realmsDataFetcher.trialAvailabilityTask, boolean_ -> {
			if (!this.createdTrial) {
				if (boolean_ != this.trialsAvailable && this.shouldShowPopup()) {
					this.trialsAvailable = boolean_;
					this.showingPopup = false;
				} else {
					this.trialsAvailable = boolean_;
				}
			}
		});
		subscription.subscribe(realmsDataFetcher.liveStatsTask, realmsServerPlayerLists -> {
			for (RealmsServerPlayerList realmsServerPlayerList : realmsServerPlayerLists.servers) {
				for (RealmsServer realmsServer : this.realmsServers) {
					if (realmsServer.id == realmsServerPlayerList.serverId) {
						realmsServer.updateServerPing(realmsServerPlayerList);
						break;
					}
				}
			}
		});
		subscription.subscribe(realmsDataFetcher.newsTask, realmsNews -> {
			realmsDataFetcher.newsManager.updateUnreadNews(realmsNews);
			this.hasUnreadNews = realmsDataFetcher.newsManager.hasUnreadNews();
			this.newsLink = realmsDataFetcher.newsManager.newsLink();
			this.updateButtonStates(null);
		});
		return subscription;
	}

	void refreshFetcher() {
		if (this.dataSubscription != null) {
			this.dataSubscription.reset();
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

	public void setCreatedTrial(boolean bl) {
		this.createdTrial = bl;
	}

	void onRenew(@Nullable RealmsServer realmsServer) {
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
							if (compatibleVersionResponse != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen);
								RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
								return;
							}

							RealmsMainScreen.this.checkParentalConsent();
						} catch (RealmsServiceException var3) {
							RealmsMainScreen.checkedClientCompatability = false;
							RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)var3);
							if (var3.httpResultCode == 401) {
								RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(
									Component.translatable("mco.error.invalid.session.title"),
									Component.translatable("mco.error.invalid.session.message"),
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

	void checkParentalConsent() {
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
							RealmsMainScreen.this.refreshFetcher();
						}
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
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
							RealmsMainScreen.this.refreshFetcher();
						}
					} catch (RealmsServiceException var3) {
						RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
					}
				}
			}).start();
		}
	}

	private void switchToProd() {
		RealmsClient.switchToProd();
		this.refreshFetcher();
	}

	void configureClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && (this.minecraft.getUser().getUuid().equals(realmsServer.ownerUUID) || overrideConfigure)) {
			this.saveListScrollPosition();
			this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, realmsServer.id));
		}
	}

	void leaveClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && !this.minecraft.getUser().getUuid().equals(realmsServer.ownerUUID)) {
			this.saveListScrollPosition();
			Component component = Component.translatable("mco.configure.world.leave.question.line1");
			Component component2 = Component.translatable("mco.configure.world.leave.question.line2");
			this.minecraft
				.setScreen(new RealmsLongConfirmationScreen(bl -> this.leaveServer(bl, realmsServer), RealmsLongConfirmationScreen.Type.Info, component, component2, true));
		}
	}

	private void saveListScrollPosition() {
		lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
	}

	@Nullable
	private RealmsServer getSelectedServer() {
		if (this.realmSelectionList == null) {
			return null;
		} else {
			RealmsMainScreen.Entry entry = this.realmSelectionList.getSelected();
			return entry != null ? entry.getServer() : null;
		}
	}

	private void leaveServer(boolean bl, RealmsServer realmsServer) {
		if (bl) {
			(new Thread("Realms-leave-server") {
				public void run() {
					try {
						RealmsClient realmsClient = RealmsClient.create();
						realmsClient.uninviteMyselfFrom(realmsServer.id);
						RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.removeServer(realmsServer));
					} catch (RealmsServiceException var2) {
						RealmsMainScreen.LOGGER.error("Couldn't configure world");
						RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var2, RealmsMainScreen.this)));
					}
				}
			}).start();
		}

		this.minecraft.setScreen(this);
	}

	void removeServer(RealmsServer realmsServer) {
		this.realmsServers = this.serverList.removeItem(realmsServer);
		this.realmSelectionList.children().removeIf(entry -> {
			RealmsServer realmsServer2 = entry.getServer();
			return realmsServer2 != null && realmsServer2.id == realmsServer.id;
		});
		this.realmSelectionList.setSelected(null);
		this.updateButtonStates(null);
		this.playButton.active = false;
	}

	public void resetScreen() {
		if (this.realmSelectionList != null) {
			this.realmSelectionList.setSelected(null);
		}
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

	void onClosePopup() {
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
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.0F, 100.0F);
			this.drawPopup(poseStack);
			poseStack.popPose();
		} else {
			if (this.showingPopup) {
				this.updateButtonStates(null);
				if (!this.realmsSelectionListAdded) {
					this.addWidget(this.realmSelectionList);
					this.realmsSelectionListAdded = true;
				}

				this.playButton.active = this.shouldPlayButtonBeActive(this.getSelectedServer());
			}

			this.showingPopup = false;
		}

		super.render(poseStack, i, j, f);
		if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
			RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
			int k = 8;
			int l = 8;
			int m = 0;
			if ((Util.getMillis() / 800L & 1L) == 1L) {
				m = 8;
			}

			GuiComponent.blit(
				poseStack,
				this.createTrialButton.getX() + this.createTrialButton.getWidth() - 8 - 4,
				this.createTrialButton.getY() + this.createTrialButton.getHeight() / 2 - 4,
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
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, LOGO_LOCATION);
		poseStack.pushPose();
		poseStack.scale(0.5F, 0.5F, 0.5F);
		GuiComponent.blit(poseStack, i * 2, j * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
		poseStack.popPose();
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

	private void drawPopup(PoseStack poseStack) {
		int i = this.popupX0();
		int j = this.popupY0();
		if (!this.showingPopup) {
			this.carouselIndex = 0;
			this.carouselTick = 0;
			this.hasSwitchedCarouselImage = true;
			this.updateButtonStates(null);
			if (this.realmsSelectionListAdded) {
				this.removeWidget(this.realmSelectionList);
				this.realmsSelectionListAdded = false;
			}

			this.minecraft.getNarrator().sayNow(POPUP_TEXT);
		}

		if (this.hasFetchedServers) {
			this.showingPopup = true;
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F);
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, DARKEN_LOCATION);
		int k = 0;
		int l = 32;
		GuiComponent.blit(poseStack, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, POPUP_LOCATION);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 310, 166, 310, 166);
		if (!teaserImages.isEmpty()) {
			RenderSystem.setShaderTexture(0, (ResourceLocation)teaserImages.get(this.carouselIndex));
			GuiComponent.blit(poseStack, i + 7, j + 7, 0.0F, 0.0F, 195, 152, 195, 152);
			if (this.carouselTick % 95 < 5) {
				if (!this.hasSwitchedCarouselImage) {
					this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
					this.hasSwitchedCarouselImage = true;
				}
			} else {
				this.hasSwitchedCarouselImage = false;
			}
		}

		this.formattedPopup.renderLeftAlignedNoShadow(poseStack, this.width / 2 + 52, j + 7, 10, 5000268);
	}

	int popupX0() {
		return (this.width - 310) / 2;
	}

	int popupY0() {
		return this.height / 2 - 80;
	}

	void drawInvitationPendingIcon(PoseStack poseStack, int i, int j, int k, int l, boolean bl, boolean bl2) {
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

		RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
		boolean bl5 = bl2 && bl;
		float g = bl5 ? 16.0F : 0.0F;
		GuiComponent.blit(poseStack, k, l - 6, g, 0.0F, 15, 25, 31, 25);
		boolean bl6 = bl2 && m != 0;
		if (bl6) {
			int o = (Math.min(m, 6) - 1) * 8;
			int p = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
			RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
			float h = bl3 ? 8.0F : 0.0F;
			GuiComponent.blit(poseStack, k + 4, l + 4 + p, (float)o, h, 8, 8, 48, 16);
		}

		int o = i + 12;
		boolean bl7 = bl2 && bl3;
		if (bl7) {
			Component component = m == 0 ? NO_PENDING_INVITES_TEXT : PENDING_INVITES_TEXT;
			int q = this.font.width(component);
			this.fillGradient(poseStack, o - 3, j - 3, o + q + 3, j + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(poseStack, component, (float)o, (float)j, -1);
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

	public void play(@Nullable RealmsServer realmsServer, Screen screen) {
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

	boolean isSelfOwnedServer(RealmsServer realmsServer) {
		return realmsServer.ownerUUID != null && realmsServer.ownerUUID.equals(this.minecraft.getUser().getUuid());
	}

	private boolean isSelfOwnedNonExpiredServer(RealmsServer realmsServer) {
		return this.isSelfOwnedServer(realmsServer) && !realmsServer.expired;
	}

	void drawExpired(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.setShaderTexture(0, EXPIRED_ICON_LOCATION);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltipForNextRenderPass(SERVER_EXPIRED_TOOLTIP);
		}
	}

	void drawExpiring(PoseStack poseStack, int i, int j, int k, int l, int m) {
		RenderSystem.setShaderTexture(0, EXPIRES_SOON_ICON_LOCATION);
		if (this.animTick % 20 < 10) {
			GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 20, 28);
		} else {
			GuiComponent.blit(poseStack, i, j, 10.0F, 0.0F, 10, 28, 20, 28);
		}

		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			if (m <= 0) {
				this.setTooltipForNextRenderPass(SERVER_EXPIRES_SOON_TOOLTIP);
			} else if (m == 1) {
				this.setTooltipForNextRenderPass(SERVER_EXPIRES_IN_DAY_TOOLTIP);
			} else {
				this.setTooltipForNextRenderPass(Component.translatable("mco.selectServer.expires.days", m));
			}
		}
	}

	void drawOpen(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.setShaderTexture(0, ON_ICON_LOCATION);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltipForNextRenderPass(SERVER_OPEN_TOOLTIP);
		}
	}

	void drawClose(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.setShaderTexture(0, OFF_ICON_LOCATION);
		GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltipForNextRenderPass(SERVER_CLOSED_TOOLTIP);
		}
	}

	void drawLeave(PoseStack poseStack, int i, int j, int k, int l) {
		boolean bl = false;
		if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			bl = true;
		}

		RenderSystem.setShaderTexture(0, LEAVE_ICON_LOCATION);
		float f = bl ? 28.0F : 0.0F;
		GuiComponent.blit(poseStack, i, j, f, 0.0F, 28, 28, 56, 28);
		if (bl) {
			this.setTooltipForNextRenderPass(LEAVE_SERVER_TOOLTIP);
			this.hoveredElement = RealmsMainScreen.HoveredElement.LEAVE;
		}
	}

	void drawConfigure(PoseStack poseStack, int i, int j, int k, int l) {
		boolean bl = false;
		if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			bl = true;
		}

		RenderSystem.setShaderTexture(0, CONFIGURE_LOCATION);
		float f = bl ? 28.0F : 0.0F;
		GuiComponent.blit(poseStack, i, j, f, 0.0F, 28, 28, 56, 28);
		if (bl) {
			this.setTooltipForNextRenderPass(CONFIGURE_SERVER_TOOLTIP);
			this.hoveredElement = RealmsMainScreen.HoveredElement.CONFIGURE;
		}
	}

	void renderNews(PoseStack poseStack, int i, int j, boolean bl, int k, int l, boolean bl2, boolean bl3) {
		boolean bl4 = false;
		if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
			bl4 = true;
		}

		RenderSystem.setShaderTexture(0, NEWS_LOCATION);
		if (!bl3) {
			RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
		}

		boolean bl5 = bl3 && bl2;
		float f = bl5 ? 20.0F : 0.0F;
		GuiComponent.blit(poseStack, k, l, f, 0.0F, 20, 20, 40, 20);
		if (bl4 && bl3) {
			this.setTooltipForNextRenderPass(NEWS_TOOLTIP);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (bl && bl3) {
			int m = bl4 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
			RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
			GuiComponent.blit(poseStack, k + 10, l + 2 + m, 40.0F, 0.0F, 8, 8, 48, 16);
		}
	}

	private void renderLocal(PoseStack poseStack) {
		String string = "LOCAL!";
		poseStack.pushPose();
		poseStack.translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
		poseStack.scale(1.5F, 1.5F, 1.5F);
		this.font.draw(poseStack, "LOCAL!", 0.0F, 0.0F, 8388479);
		poseStack.popPose();
	}

	private void renderStage(PoseStack poseStack) {
		String string = "STAGE!";
		poseStack.pushPose();
		poseStack.translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
		poseStack.scale(1.5F, 1.5F, 1.5F);
		this.font.draw(poseStack, "STAGE!", 0.0F, 0.0F, -256);
		poseStack.popPose();
	}

	public RealmsMainScreen newScreen() {
		RealmsMainScreen realmsMainScreen = new RealmsMainScreen(this.lastScreen);
		realmsMainScreen.init(this.minecraft, this.width, this.height);
		return realmsMainScreen;
	}

	public static void updateTeaserImages(ResourceManager resourceManager) {
		Collection<ResourceLocation> collection = resourceManager.listResources(
				"textures/gui/images", resourceLocation -> resourceLocation.getPath().endsWith(".png")
			)
			.keySet();
		teaserImages = collection.stream().filter(resourceLocation -> resourceLocation.getNamespace().equals("realms")).toList();
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
				Component.translatable("mco.selectServer.close"),
				button -> RealmsMainScreen.this.onClosePopup(),
				DEFAULT_NARRATION
			);
		}

		@Override
		public void renderWidget(PoseStack poseStack, int i, int j, float f) {
			RenderSystem.setShaderTexture(0, RealmsMainScreen.CROSS_ICON_LOCATION);
			float g = this.isHoveredOrFocused() ? 12.0F : 0.0F;
			blit(poseStack, this.getX(), this.getY(), 0.0F, g, 12, 12, 12, 24);
			if (this.isMouseOver((double)i, (double)j)) {
				RealmsMainScreen.this.setTooltipForNextRenderPass(this.getMessage());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
		@Nullable
		public abstract RealmsServer getServer();
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
			super(RealmsMainScreen.this.width - 115, 6, 20, 20, Component.translatable("mco.news"), button -> {
				if (RealmsMainScreen.this.newsLink != null) {
					RealmsMainScreen.this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
						if (bl) {
							Util.getPlatform().openUri(RealmsMainScreen.this.newsLink);
						}

						RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.this);
					}, RealmsMainScreen.this.newsLink, true));
					if (RealmsMainScreen.this.hasUnreadNews) {
						RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
						realmsPersistenceData.hasUnreadNews = false;
						RealmsMainScreen.this.hasUnreadNews = false;
						RealmsPersistence.writeFile(realmsPersistenceData);
					}
				}
			}, DEFAULT_NARRATION);
		}

		@Override
		public void renderWidget(PoseStack poseStack, int i, int j, float f) {
			RealmsMainScreen.this.renderNews(poseStack, i, j, RealmsMainScreen.this.hasUnreadNews, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInvitesButton extends Button {
		public PendingInvitesButton() {
			super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, CommonComponents.EMPTY, RealmsMainScreen.this::pendingButtonPress, DEFAULT_NARRATION);
		}

		public void tick() {
			this.setMessage(RealmsMainScreen.this.numberOfPendingInvites == 0 ? RealmsMainScreen.NO_PENDING_INVITES_TEXT : RealmsMainScreen.PENDING_INVITES_TEXT);
		}

		@Override
		public void renderWidget(PoseStack poseStack, int i, int j, float f) {
			RealmsMainScreen.this.drawInvitationPendingIcon(poseStack, i, j, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
		public RealmSelectionList() {
			super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 40, 36);
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (i != 257 && i != 32 && i != 335) {
				return super.keyPressed(i, j, k);
			} else {
				RealmsMainScreen.Entry entry = this.getSelected();
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
					this.selectItem(m);
				}

				return true;
			} else {
				return super.mouseClicked(d, e, i);
			}
		}

		public void setSelected(@Nullable RealmsMainScreen.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				RealmsMainScreen.this.updateButtonStates(entry.getServer());
			} else {
				RealmsMainScreen.this.updateButtonStates(null);
			}
		}

		@Override
		public void itemClicked(int i, int j, double d, double e, int k) {
			RealmsMainScreen.Entry entry = this.getEntry(j);
			if (entry instanceof RealmsMainScreen.TrialEntry) {
				RealmsMainScreen.this.popupOpenedByUser = true;
			} else {
				RealmsServer realmsServer = entry.getServer();
				if (realmsServer != null) {
					if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
						Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(realmsServer, RealmsMainScreen.this));
					} else {
						if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.CONFIGURE) {
							RealmsMainScreen.this.configureClicked(realmsServer);
						} else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.LEAVE) {
							RealmsMainScreen.this.leaveClicked(realmsServer);
						} else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.EXPIRED) {
							RealmsMainScreen.this.onRenew(realmsServer);
						} else if (RealmsMainScreen.this.shouldPlayButtonBeActive(realmsServer)) {
							if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isSelectedItem(j)) {
								RealmsMainScreen.this.play(realmsServer, RealmsMainScreen.this);
							}

							RealmsMainScreen.this.lastClickTime = Util.getMillis();
						}
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
		private static final int SKIN_HEAD_LARGE_WIDTH = 36;
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
				RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
			}

			return true;
		}

		private void renderMcoServerItem(RealmsServer realmsServer, PoseStack poseStack, int i, int j, int k, int l) {
			this.renderLegacy(realmsServer, poseStack, i + 36, j, k, l);
		}

		private void renderLegacy(RealmsServer realmsServer, PoseStack poseStack, int i, int j, int k, int l) {
			if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
				RenderSystem.setShaderTexture(0, RealmsMainScreen.WORLDICON_LOCATION);
				GuiComponent.blit(poseStack, i + 10, j + 6, 0.0F, 0.0F, 40, 20, 40, 20);
				float f = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
				int m = 0xFF000000 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
				GuiComponent.drawCenteredString(poseStack, RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, i + 10 + 40 + 75, j + 12, m);
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
					String string = ChatFormatting.GRAY + realmsServer.serverPing.nrOfPlayers;
					RealmsMainScreen.this.font.draw(poseStack, string, (float)(i + 207 - RealmsMainScreen.this.font.width(string)), (float)(j + 3), 8421504);
					if (k >= i + 207 - RealmsMainScreen.this.font.width(string)
						&& k <= i + 207
						&& l >= j + 1
						&& l <= j + 10
						&& l < RealmsMainScreen.this.height - 40
						&& l > 32
						&& !RealmsMainScreen.this.shouldShowPopup()) {
						RealmsMainScreen.this.setTooltipForNextRenderPass(Component.literal(realmsServer.serverPing.playerList));
					}
				}

				if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.expired) {
					RenderSystem.enableBlend();
					RenderSystem.setShaderTexture(0, RealmsMainScreen.BUTTON_LOCATION);
					RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					Component component;
					Component component2;
					if (realmsServer.expiredTrial) {
						component = RealmsMainScreen.TRIAL_EXPIRED_TEXT;
						component2 = RealmsMainScreen.SUBSCRIPTION_CREATE_TEXT;
					} else {
						component = RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
						component2 = RealmsMainScreen.SUBSCRIPTION_RENEW_TEXT;
					}

					int o = RealmsMainScreen.this.font.width(component2) + 17;
					int p = 16;
					int q = i + RealmsMainScreen.this.font.width(component) + 8;
					int r = j + 13;
					boolean bl = false;
					if (k >= q && k < q + o && l > r && l <= r + 16 && l < RealmsMainScreen.this.height - 40 && l > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
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
					RealmsMainScreen.this.font.draw(poseStack, component, (float)(i + 2), (float)(t + 1), 15553363);
					GuiComponent.drawCenteredString(poseStack, RealmsMainScreen.this.font, component2, q + o / 2, t + 1, u);
				} else {
					if (realmsServer.worldType == RealmsServer.WorldType.MINIGAME) {
						int v = 13413468;
						int w = RealmsMainScreen.this.font.width(RealmsMainScreen.SELECT_MINIGAME_PREFIX);
						RealmsMainScreen.this.font.draw(poseStack, RealmsMainScreen.SELECT_MINIGAME_PREFIX, (float)(i + 2), (float)(j + 12), 13413468);
						RealmsMainScreen.this.font.draw(poseStack, realmsServer.getMinigameName(), (float)(i + 2 + w), (float)(j + 12), 7105644);
					} else {
						RealmsMainScreen.this.font.draw(poseStack, realmsServer.getDescription(), (float)(i + 2), (float)(j + 12), 7105644);
					}

					if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer)) {
						RealmsMainScreen.this.font.draw(poseStack, realmsServer.owner, (float)(i + 2), (float)(j + 12 + 11), 5000268);
					}
				}

				RealmsMainScreen.this.font.draw(poseStack, realmsServer.getName(), (float)(i + 2), (float)(j + 1), 16777215);
				RealmsUtil.renderPlayerFace(poseStack, i - 36, j, 32, realmsServer.ownerUUID);
			}
		}

		@Override
		public Component getNarration() {
			return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED
				? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION
				: Component.translatable("narrator.select", this.serverData.name));
		}

		@Nullable
		@Override
		public RealmsServer getServer() {
			return this.serverData;
		}
	}

	@Environment(EnvType.CLIENT)
	class TrialEntry extends RealmsMainScreen.Entry {
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
			boolean bl = false;
			if (j <= l && l <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && k <= m && m <= k + 32) {
				bl = true;
			}

			int p = 8388479;
			if (bl && !RealmsMainScreen.this.shouldShowPopup()) {
				p = 6077788;
			}

			for (Component component : RealmsMainScreen.TRIAL_MESSAGE_LINES) {
				GuiComponent.drawCenteredString(poseStack, RealmsMainScreen.this.font, component, RealmsMainScreen.this.width / 2, n + o, p);
				o += 10;
			}
		}

		@Override
		public Component getNarration() {
			return RealmsMainScreen.TRIAL_TEXT;
		}

		@Nullable
		@Override
		public RealmsServer getServer() {
			return null;
		}
	}
}
