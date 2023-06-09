package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNotification;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
	private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
	private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
	private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
	static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
	static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
	static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
	private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
	private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
	private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
	private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
	static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
	private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
	static final ResourceLocation INFO_ICON_LOCATION = new ResourceLocation("minecraft", "textures/gui/info_icon.png");
	static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(
		Component.translatable("mco.trial.message.line1"), Component.translatable("mco.trial.message.line2")
	);
	static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
	static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
	private static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
	static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
	static final Component SELECT_MINIGAME_PREFIX = Component.translatable("mco.selectServer.minigame").append(CommonComponents.SPACE);
	private static final Component POPUP_TEXT = Component.translatable("mco.selectServer.popup");
	private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
	private static final Component LEAVE_SERVER_TEXT = Component.translatable("mco.selectServer.leave");
	private static final Component CONFIGURE_SERVER_TEXT = Component.translatable("mco.selectServer.configure");
	private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
	private static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
	private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
	private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
	private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
	private static final Component NEWS_TOOLTIP = Component.translatable("mco.news");
	static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
	static final Component TRIAL_TEXT = CommonComponents.joinLines(TRIAL_MESSAGE_LINES);
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_TOP_ROW_WIDTH = 308;
	private static final int BUTTON_BOTTOM_ROW_WIDTH = 204;
	private static final int FOOTER_HEIGHT = 64;
	private static final int LOGO_WIDTH = 128;
	private static final int LOGO_HEIGHT = 34;
	private static final int LOGO_TEXTURE_WIDTH = 128;
	private static final int LOGO_TEXTURE_HEIGHT = 64;
	private static final int LOGO_PADDING = 5;
	private static final int HEADER_HEIGHT = 44;
	private static List<ResourceLocation> teaserImages = ImmutableList.of();
	@Nullable
	private DataFetcher.Subscription dataSubscription;
	private RealmsServerList serverList;
	private final Set<UUID> handledSeenNotifications = new HashSet();
	private static boolean overrideConfigure;
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
	private final List<RealmsNotification> notifications = new ArrayList();
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
			this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
			if (lastScrollYPosition != -1) {
				this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
			}

			this.addWidget(this.realmSelectionList);
			this.realmsSelectionListAdded = true;
			this.setInitialFocus(this.realmSelectionList);
			this.addMiddleButtons();
			this.addFooterButtons();
			this.addTopButtons();
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
				.bounds(this.width - 90, 12, 80, 20)
				.build()
		);
	}

	public void addMiddleButtons() {
		this.createTrialButton = this.addWidget(Button.builder(Component.translatable("mco.selectServer.trial"), button -> {
			if (this.trialsAvailable && !this.createdTrial) {
				Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
				this.minecraft.setScreen(this.lastScreen);
			}
		}).bounds(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20).build());
		this.buyARealmButton = this.addWidget(
			Button.builder(Component.translatable("mco.selectServer.buy"), button -> Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms"))
				.bounds(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20)
				.build()
		);
		this.closeButton = this.addWidget(new RealmsMainScreen.CloseButton());
	}

	public void addFooterButtons() {
		this.playButton = Button.builder(PLAY_TEXT, button -> this.play(this.getSelectedServer(), this)).width(100).build();
		this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, button -> this.configureClicked(this.getSelectedServer())).width(100).build();
		this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, button -> this.onRenew(this.getSelectedServer())).width(100).build();
		this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, button -> this.leaveClicked(this.getSelectedServer())).width(100).build();
		this.backButton = Button.builder(CommonComponents.GUI_BACK, button -> {
			if (!this.justClosedPopup) {
				this.minecraft.setScreen(this.lastScreen);
			}
		}).width(100).build();
		GridLayout gridLayout = new GridLayout();
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(1);
		LinearLayout linearLayout = rowHelper.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL), rowHelper.newCellSettings().paddingBottom(4));
		linearLayout.addChild(this.playButton);
		linearLayout.addChild(this.configureButton);
		linearLayout.addChild(this.renewButton);
		LinearLayout linearLayout2 = rowHelper.addChild(
			new LinearLayout(204, 20, LinearLayout.Orientation.HORIZONTAL), rowHelper.newCellSettings().alignHorizontallyCenter()
		);
		linearLayout2.addChild(this.leaveButton);
		linearLayout2.addChild(this.backButton);
		gridLayout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		gridLayout.arrangeElements();
		FrameLayout.centerInRectangle(gridLayout, 0, this.height - 64, this.width, 64);
	}

	void updateButtonStates(@Nullable RealmsServer realmsServer) {
		this.backButton.active = true;
		if (hasParentalConsent() && this.hasFetchedServers) {
			boolean bl = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
			this.createTrialButton.visible = bl;
			this.createTrialButton.active = bl;
			this.buyARealmButton.visible = this.shouldShowPopup();
			this.closeButton.visible = this.shouldShowPopup();
			this.newsButton.active = true;
			this.newsButton.visible = this.newsLink != null;
			this.pendingInvitesButton.active = true;
			this.pendingInvitesButton.visible = true;
			this.showPopupButton.active = !this.shouldShowPopup();
			this.playButton.visible = !this.shouldShowPopup();
			this.renewButton.visible = !this.shouldShowPopup();
			this.leaveButton.visible = !this.shouldShowPopup();
			this.configureButton.visible = !this.shouldShowPopup();
			this.backButton.visible = !this.shouldShowPopup();
			this.playButton.active = this.shouldPlayButtonBeActive(realmsServer);
			this.renewButton.active = this.shouldRenewButtonBeActive(realmsServer);
			this.leaveButton.active = this.shouldLeaveButtonBeActive(realmsServer);
			this.configureButton.active = this.shouldConfigureButtonBeActive(realmsServer);
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

	private boolean shouldConfigureButtonBeActive(@Nullable RealmsServer realmsServer) {
		return realmsServer != null && this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldLeaveButtonBeActive(@Nullable RealmsServer realmsServer) {
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
			boolean bl = false;

			for (RealmsServer realmsServer : list2) {
				if (this.isSelfOwnedNonExpiredServer(realmsServer)) {
					bl = true;
				}
			}

			this.realmsServers = list2;
			this.hasFetchedServers = true;
			this.refreshRealmsSelectionList();
			if (!regionsPinged && bl) {
				regionsPinged = true;
				this.pingRegions();
			}
		});
		callRealmsClient(RealmsClient::getNotifications, list -> {
			this.notifications.clear();
			this.notifications.addAll(list);
			this.refreshRealmsSelectionList();
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

	private static <T> void callRealmsClient(RealmsMainScreen.RealmsCall<T> realmsCall, Consumer<T> consumer) {
		Minecraft minecraft = Minecraft.getInstance();
		CompletableFuture.supplyAsync(() -> {
			try {
				return realmsCall.request(RealmsClient.create(minecraft));
			} catch (RealmsServiceException var3) {
				throw new RuntimeException(var3);
			}
		}).thenAcceptAsync(consumer, minecraft).exceptionally(throwable -> {
			LOGGER.error("Failed to execute call to Realms Service", throwable);
			return null;
		});
	}

	private void refreshRealmsSelectionList() {
		boolean bl = !this.hasFetchedServers;
		this.realmSelectionList.clear();
		List<UUID> list = new ArrayList();

		for (RealmsNotification realmsNotification : this.notifications) {
			this.addEntriesForNotification(this.realmSelectionList, realmsNotification);
			if (!realmsNotification.seen() && !this.handledSeenNotifications.contains(realmsNotification.uuid())) {
				list.add(realmsNotification.uuid());
			}
		}

		if (!list.isEmpty()) {
			callRealmsClient(realmsClient -> {
				realmsClient.notificationsSeen(list);
				return null;
			}, object -> this.handledSeenNotifications.addAll(list));
		}

		if (this.shouldShowMessageInList()) {
			this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
		}

		RealmsMainScreen.Entry entry = null;
		RealmsServer realmsServer = this.getSelectedServer();

		for (RealmsServer realmsServer2 : this.realmsServers) {
			RealmsMainScreen.ServerEntry serverEntry = new RealmsMainScreen.ServerEntry(realmsServer2);
			this.realmSelectionList.addEntry(serverEntry);
			if (realmsServer != null && realmsServer.id == realmsServer2.id) {
				entry = serverEntry;
			}
		}

		if (bl) {
			this.updateButtonStates(null);
		} else {
			this.realmSelectionList.setSelected(entry);
		}
	}

	private void addEntriesForNotification(RealmsMainScreen.RealmSelectionList realmSelectionList, RealmsNotification realmsNotification) {
		if (realmsNotification instanceof RealmsNotification.VisitUrl visitUrl) {
			realmSelectionList.addEntry(new RealmsMainScreen.NotificationMessageEntry(visitUrl.getMessage(), visitUrl));
			realmSelectionList.addEntry(new RealmsMainScreen.ButtonEntry(visitUrl.buildOpenLinkButton(this)));
		}
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

	private void onRenew(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null) {
			String string = CommonLinks.extendRealms(realmsServer.remoteSubscriptionId, this.minecraft.getUser().getUuid(), realmsServer.expiredTrial);
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

	private void configureClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && (this.minecraft.getUser().getUuid().equals(realmsServer.ownerUUID) || overrideConfigure)) {
			this.saveListScrollPosition();
			this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, realmsServer.id));
		}
	}

	private void leaveClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && !this.minecraft.getUser().getUuid().equals(realmsServer.ownerUUID)) {
			this.saveListScrollPosition();
			Component component = Component.translatable("mco.configure.world.leave.question.line1");
			Component component2 = Component.translatable("mco.configure.world.leave.question.line2");
			this.minecraft
				.setScreen(new RealmsLongConfirmationScreen(bl -> this.leaveServer(bl, realmsServer), RealmsLongConfirmationScreen.Type.INFO, component, component2, true));
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

	void dismissNotification(UUID uUID) {
		callRealmsClient(realmsClient -> {
			realmsClient.notificationsDismiss(List.of(uUID));
			return null;
		}, object -> {
			this.notifications.removeIf(realmsNotification -> realmsNotification.dismissable() && uUID.equals(realmsNotification.uuid()));
			this.refreshRealmsSelectionList();
		});
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		this.realmSelectionList.render(guiGraphics, i, j, f);
		guiGraphics.blit(LOGO_LOCATION, this.width / 2 - 64, 5, 0.0F, 0.0F, 128, 34, 128, 64);
		if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
			this.renderStage(guiGraphics);
		}

		if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
			this.renderLocal(guiGraphics);
		}

		if (this.shouldShowPopup()) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
			this.drawPopup(guiGraphics, i, j, f);
			guiGraphics.pose().popPose();
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

		super.render(guiGraphics, i, j, f);
		if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
			int k = 8;
			int l = 8;
			int m = 0;
			if ((Util.getMillis() / 800L & 1L) == 1L) {
				m = 8;
			}

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 110.0F);
			guiGraphics.blit(
				TRIAL_ICON_LOCATION,
				this.createTrialButton.getX() + this.createTrialButton.getWidth() - 8 - 4,
				this.createTrialButton.getY() + this.createTrialButton.getHeight() / 2 - 4,
				0.0F,
				(float)m,
				8,
				8,
				8,
				16
			);
			guiGraphics.pose().popPose();
		}
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

	private void drawPopup(GuiGraphics guiGraphics, int i, int j, float f) {
		int k = this.popupX0();
		int l = this.popupY0();
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

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.7F);
		RenderSystem.enableBlend();
		guiGraphics.blit(DARKEN_LOCATION, 0, 44, 0.0F, 0.0F, this.width, this.height - 44, 310, 166);
		RenderSystem.disableBlend();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		guiGraphics.blit(POPUP_LOCATION, k, l, 0.0F, 0.0F, 310, 166, 310, 166);
		if (!teaserImages.isEmpty()) {
			guiGraphics.blit((ResourceLocation)teaserImages.get(this.carouselIndex), k + 7, l + 7, 0.0F, 0.0F, 195, 152, 195, 152);
			if (this.carouselTick % 95 < 5) {
				if (!this.hasSwitchedCarouselImage) {
					this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
					this.hasSwitchedCarouselImage = true;
				}
			} else {
				this.hasSwitchedCarouselImage = false;
			}
		}

		this.formattedPopup.renderLeftAlignedNoShadow(guiGraphics, this.width / 2 + 52, l + 7, 10, 16777215);
		this.createTrialButton.render(guiGraphics, i, j, f);
		this.buyARealmButton.render(guiGraphics, i, j, f);
		this.closeButton.render(guiGraphics, i, j, f);
	}

	int popupX0() {
		return (this.width - 310) / 2;
	}

	int popupY0() {
		return this.height / 2 - 80;
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

	void drawExpired(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		guiGraphics.blit(EXPIRED_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltipForNextRenderPass(SERVER_EXPIRED_TOOLTIP);
		}
	}

	void drawExpiring(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		if (this.animTick % 20 < 10) {
			guiGraphics.blit(EXPIRES_SOON_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 20, 28);
		} else {
			guiGraphics.blit(EXPIRES_SOON_ICON_LOCATION, i, j, 10.0F, 0.0F, 10, 28, 20, 28);
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

	void drawOpen(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		guiGraphics.blit(ON_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltipForNextRenderPass(SERVER_OPEN_TOOLTIP);
		}
	}

	void drawClose(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		guiGraphics.blit(OFF_ICON_LOCATION, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
		if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
			this.setTooltipForNextRenderPass(SERVER_CLOSED_TOOLTIP);
		}
	}

	void renderNews(GuiGraphics guiGraphics, int i, int j, boolean bl, int k, int l, boolean bl2, boolean bl3) {
		boolean bl4 = false;
		if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
			bl4 = true;
		}

		if (!bl3) {
			guiGraphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
		}

		boolean bl5 = bl3 && bl2;
		float f = bl5 ? 20.0F : 0.0F;
		guiGraphics.blit(NEWS_LOCATION, k, l, f, 0.0F, 20, 20, 40, 20);
		if (bl4 && bl3) {
			this.setTooltipForNextRenderPass(NEWS_TOOLTIP);
		}

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (bl && bl3) {
			int m = bl4 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
			guiGraphics.blit(INVITATION_ICONS_LOCATION, k + 10, l + 2 + m, 40.0F, 0.0F, 8, 8, 48, 16);
		}
	}

	private void renderLocal(GuiGraphics guiGraphics) {
		String string = "LOCAL!";
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
		guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
		guiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
		guiGraphics.drawString(this.font, "LOCAL!", 0, 0, 8388479, false);
		guiGraphics.pose().popPose();
	}

	private void renderStage(GuiGraphics guiGraphics) {
		String string = "STAGE!";
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
		guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
		guiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
		guiGraphics.drawString(this.font, "STAGE!", 0, 0, -256, false);
		guiGraphics.pose().popPose();
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

	@Environment(EnvType.CLIENT)
	class ButtonEntry extends RealmsMainScreen.Entry {
		private final Button button;
		private final int xPos = RealmsMainScreen.this.width / 2 - 75;

		public ButtonEntry(Button button) {
			this.button = button;
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			this.button.mouseClicked(d, e, i);
			return true;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			return this.button.keyPressed(i, j, k) ? true : super.keyPressed(i, j, k);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.button.setPosition(this.xPos, j + 4);
			this.button.render(guiGraphics, n, o, f);
		}

		@Override
		public Component getNarration() {
			return this.button.getMessage();
		}
	}

	@Environment(EnvType.CLIENT)
	class CloseButton extends RealmsMainScreen.CrossButton {
		public CloseButton() {
			super(
				RealmsMainScreen.this.popupX0() + 4,
				RealmsMainScreen.this.popupY0() + 4,
				button -> RealmsMainScreen.this.onClosePopup(),
				Component.translatable("mco.selectServer.close")
			);
		}
	}

	@Environment(EnvType.CLIENT)
	static class CrossButton extends Button {
		protected CrossButton(Button.OnPress onPress, Component component) {
			this(0, 0, onPress, component);
		}

		protected CrossButton(int i, int j, Button.OnPress onPress, Component component) {
			super(i, j, 14, 14, component, onPress, DEFAULT_NARRATION);
			this.setTooltip(Tooltip.create(component));
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			float g = this.isHoveredOrFocused() ? 14.0F : 0.0F;
			guiGraphics.blit(RealmsMainScreen.CROSS_ICON_LOCATION, this.getX(), this.getY(), 0.0F, g, 14, 14, 14, 28);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
		@Nullable
		public RealmsServer getServer() {
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	class NewsButton extends Button {
		private static final int SIDE = 20;

		public NewsButton() {
			super(RealmsMainScreen.this.width - 115, 12, 20, 20, Component.translatable("mco.news"), button -> {
				if (RealmsMainScreen.this.newsLink != null) {
					ConfirmLinkScreen.confirmLinkNow(RealmsMainScreen.this.newsLink, RealmsMainScreen.this, true);
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
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			RealmsMainScreen.this.renderNews(guiGraphics, i, j, RealmsMainScreen.this.hasUnreadNews, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
		}
	}

	@Environment(EnvType.CLIENT)
	class NotificationMessageEntry extends RealmsMainScreen.Entry {
		private static final int SIDE_MARGINS = 40;
		private static final int ITEM_HEIGHT = 36;
		private static final int OUTLINE_COLOR = -12303292;
		private final Component text;
		private final List<AbstractWidget> children = new ArrayList();
		@Nullable
		private final RealmsMainScreen.CrossButton dismissButton;
		private final MultiLineTextWidget textWidget;
		private final GridLayout gridLayout;
		private final FrameLayout textFrame;
		private int lastEntryWidth = -1;

		public NotificationMessageEntry(Component component, RealmsNotification realmsNotification) {
			this.text = component;
			this.gridLayout = new GridLayout();
			int i = 7;
			this.gridLayout.addChild(new ImageWidget(20, 20, RealmsMainScreen.INFO_ICON_LOCATION), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
			this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
			this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
			this.textWidget = this.textFrame
				.addChild(
					new MultiLineTextWidget(component, RealmsMainScreen.this.font).setCentered(true).setMaxRows(3),
					this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop()
				);
			this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
			if (realmsNotification.dismissable()) {
				this.dismissButton = this.gridLayout
					.addChild(
						new RealmsMainScreen.CrossButton(
							button -> RealmsMainScreen.this.dismissNotification(realmsNotification.uuid()), Component.translatable("mco.notification.dismiss")
						),
						0,
						2,
						this.gridLayout.newCellSettings().alignHorizontallyRight().padding(0, 7, 7, 0)
					);
			} else {
				this.dismissButton = null;
			}

			this.gridLayout.visitWidgets(this.children::add);
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			return this.dismissButton != null && this.dismissButton.keyPressed(i, j, k) ? true : super.keyPressed(i, j, k);
		}

		private void updateEntryWidth(int i) {
			if (this.lastEntryWidth != i) {
				this.refreshLayout(i);
				this.lastEntryWidth = i;
			}
		}

		private void refreshLayout(int i) {
			int j = i - 80;
			this.textFrame.setMinWidth(j);
			this.textWidget.setMaxWidth(j);
			this.gridLayout.arrangeElements();
		}

		@Override
		public void renderBack(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			super.renderBack(guiGraphics, i, j, k, l, m, n, o, bl, f);
			guiGraphics.renderOutline(k - 2, j - 2, l, 70, -12303292);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.gridLayout.setPosition(k, j);
			this.updateEntryWidth(l - 4);
			this.children.forEach(abstractWidget -> abstractWidget.render(guiGraphics, n, o, f));
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.dismissButton != null) {
				this.dismissButton.mouseClicked(d, e, i);
			}

			return true;
		}

		@Override
		public Component getNarration() {
			return this.text;
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInvitesButton extends ImageButton {
		private static final Component TITLE = Component.translatable("mco.invites.title");
		private static final Tooltip NO_PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.nopending"));
		private static final Tooltip PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.pending"));
		private static final int WIDTH = 18;
		private static final int HEIGHT = 15;
		private static final int X_OFFSET = 10;
		private static final int INVITES_WIDTH = 8;
		private static final int INVITES_HEIGHT = 8;
		private static final int INVITES_OFFSET = 11;

		public PendingInvitesButton() {
			super(
				RealmsMainScreen.this.width / 2 + 64 + 10,
				15,
				18,
				15,
				0,
				0,
				15,
				RealmsMainScreen.INVITE_ICON_LOCATION,
				18,
				30,
				button -> RealmsMainScreen.this.minecraft.setScreen(new RealmsPendingInvitesScreen(RealmsMainScreen.this.lastScreen, TITLE)),
				TITLE
			);
			this.setTooltip(NO_PENDING_INVITES);
		}

		public void tick() {
			this.setTooltip(RealmsMainScreen.this.numberOfPendingInvites == 0 ? NO_PENDING_INVITES : PENDING_INVITES);
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			super.renderWidget(guiGraphics, i, j, f);
			this.drawInvitations(guiGraphics);
		}

		private void drawInvitations(GuiGraphics guiGraphics) {
			boolean bl = this.active && RealmsMainScreen.this.numberOfPendingInvites != 0;
			if (bl) {
				int i = (Math.min(RealmsMainScreen.this.numberOfPendingInvites, 6) - 1) * 8;
				int j = (int)(
					Math.max(0.0F, Math.max(Mth.sin((float)(10 + RealmsMainScreen.this.animTick) * 0.57F), Mth.cos((float)RealmsMainScreen.this.animTick * 0.35F))) * -6.0F
				);
				float f = this.isHoveredOrFocused() ? 8.0F : 0.0F;
				guiGraphics.blit(RealmsMainScreen.INVITATION_ICONS_LOCATION, this.getX() + 11, this.getY() + j, (float)i, f, 8, 8, 48, 16);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
		public RealmSelectionList() {
			super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 44, RealmsMainScreen.this.height - 64, 36);
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
		public int getMaxPosition() {
			return this.getItemCount() * 36;
		}

		@Override
		public int getRowWidth() {
			return 300;
		}
	}

	@Environment(EnvType.CLIENT)
	interface RealmsCall<T> {
		T request(RealmsClient realmsClient) throws RealmsServiceException;
	}

	@Environment(EnvType.CLIENT)
	class ServerEntry extends RealmsMainScreen.Entry {
		private static final int SKIN_HEAD_LARGE_WIDTH = 36;
		private final RealmsServer serverData;

		public ServerEntry(RealmsServer realmsServer) {
			this.serverData = realmsServer;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderMcoServerItem(this.serverData, guiGraphics, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
				RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
			} else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
				if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isFocused()) {
					RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
				}

				RealmsMainScreen.this.lastClickTime = Util.getMillis();
			}

			return true;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (CommonInputs.selected(i) && RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
				RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
				return true;
			} else {
				return super.keyPressed(i, j, k);
			}
		}

		private void renderMcoServerItem(RealmsServer realmsServer, GuiGraphics guiGraphics, int i, int j, int k, int l) {
			this.renderLegacy(realmsServer, guiGraphics, i + 36, j, k, l);
		}

		private void renderLegacy(RealmsServer realmsServer, GuiGraphics guiGraphics, int i, int j, int k, int l) {
			if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
				guiGraphics.blit(RealmsMainScreen.WORLDICON_LOCATION, i + 10, j + 6, 0.0F, 0.0F, 40, 20, 40, 20);
				float f = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
				int m = 0xFF000000 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
				guiGraphics.drawCenteredString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, i + 10 + 40 + 75, j + 12, m);
			} else {
				int n = 225;
				int m = 2;
				this.renderStatusLights(realmsServer, guiGraphics, i, j, k, l, 225, 2);
				if (!"0".equals(realmsServer.serverPing.nrOfPlayers)) {
					String string = ChatFormatting.GRAY + realmsServer.serverPing.nrOfPlayers;
					guiGraphics.drawString(RealmsMainScreen.this.font, string, i + 207 - RealmsMainScreen.this.font.width(string), j + 3, 8421504, false);
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
					Component component = realmsServer.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
					int o = j + 11 + 5;
					guiGraphics.drawString(RealmsMainScreen.this.font, component, i + 2, o + 1, 15553363, false);
				} else {
					if (realmsServer.worldType == RealmsServer.WorldType.MINIGAME) {
						int p = 13413468;
						int o = RealmsMainScreen.this.font.width(RealmsMainScreen.SELECT_MINIGAME_PREFIX);
						guiGraphics.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SELECT_MINIGAME_PREFIX, i + 2, j + 12, 13413468, false);
						guiGraphics.drawString(RealmsMainScreen.this.font, realmsServer.getMinigameName(), i + 2 + o, j + 12, 7105644, false);
					} else {
						guiGraphics.drawString(RealmsMainScreen.this.font, realmsServer.getDescription(), i + 2, j + 12, 7105644, false);
					}

					if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer)) {
						guiGraphics.drawString(RealmsMainScreen.this.font, realmsServer.owner, i + 2, j + 12 + 11, 5000268, false);
					}
				}

				guiGraphics.drawString(RealmsMainScreen.this.font, realmsServer.getName(), i + 2, j + 1, 16777215, false);
				RealmsUtil.renderPlayerFace(guiGraphics, i - 36, j, 32, realmsServer.ownerUUID);
			}
		}

		private void renderStatusLights(RealmsServer realmsServer, GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n) {
			int o = i + m + 22;
			if (realmsServer.expired) {
				RealmsMainScreen.this.drawExpired(guiGraphics, o, j + n, k, l);
			} else if (realmsServer.state == RealmsServer.State.CLOSED) {
				RealmsMainScreen.this.drawClose(guiGraphics, o, j + n, k, l);
			} else if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.daysLeft < 7) {
				RealmsMainScreen.this.drawExpiring(guiGraphics, o, j + n, k, l, realmsServer.daysLeft);
			} else if (realmsServer.state == RealmsServer.State.OPEN) {
				RealmsMainScreen.this.drawOpen(guiGraphics, o, j + n, k, l);
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
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderTrialItem(guiGraphics, i, k, j, n, o);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsMainScreen.this.popupOpenedByUser = true;
			return true;
		}

		private void renderTrialItem(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
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
				guiGraphics.drawCenteredString(RealmsMainScreen.this.font, component, RealmsMainScreen.this.width / 2, n + o, p);
				o += 10;
			}
		}

		@Override
		public Component getNarration() {
			return RealmsMainScreen.TRIAL_TEXT;
		}
	}
}
