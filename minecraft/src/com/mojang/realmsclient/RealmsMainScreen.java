package com.mojang.realmsclient;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.screens.RealmsPopupScreen;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
	static final ResourceLocation INFO_SPRITE = new ResourceLocation("icon/info");
	static final ResourceLocation NEW_REALM_SPRITE = new ResourceLocation("icon/new_realm");
	static final ResourceLocation EXPIRED_SPRITE = new ResourceLocation("realm_status/expired");
	static final ResourceLocation EXPIRES_SOON_SPRITE = new ResourceLocation("realm_status/expires_soon");
	static final ResourceLocation OPEN_SPRITE = new ResourceLocation("realm_status/open");
	static final ResourceLocation CLOSED_SPRITE = new ResourceLocation("realm_status/closed");
	private static final ResourceLocation INVITE_SPRITE = new ResourceLocation("icon/invite");
	private static final ResourceLocation NEWS_SPRITE = new ResourceLocation("icon/news");
	static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/realms.png");
	private static final ResourceLocation NO_REALMS_LOCATION = new ResourceLocation("textures/gui/realms/no_realms.png");
	private static final Component TITLE = Component.translatable("menu.online");
	private static final Component LOADING_TEXT = Component.translatable("mco.selectServer.loading");
	static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
	static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
	private static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
	static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
	private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
	private static final Component LEAVE_SERVER_TEXT = Component.translatable("mco.selectServer.leave");
	private static final Component CONFIGURE_SERVER_TEXT = Component.translatable("mco.selectServer.configure");
	static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
	static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
	static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
	static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
	static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
	static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
	private static final Component NO_REALMS_TEXT = Component.translatable("mco.selectServer.noRealms");
	private static final Tooltip NO_PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.nopending"));
	private static final Tooltip PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.pending"));
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_COLUMNS = 3;
	private static final int BUTTON_SPACING = 4;
	private static final int CONTENT_WIDTH = 308;
	private static final int LOGO_WIDTH = 128;
	private static final int LOGO_HEIGHT = 34;
	private static final int LOGO_TEXTURE_WIDTH = 128;
	private static final int LOGO_TEXTURE_HEIGHT = 64;
	private static final int LOGO_PADDING = 5;
	private static final int HEADER_HEIGHT = 44;
	private static final int FOOTER_PADDING = 11;
	private static final int NEW_REALM_SPRITE_WIDTH = 40;
	private static final int NEW_REALM_SPRITE_HEIGHT = 20;
	private static final int ENTRY_WIDTH = 216;
	private static final int ITEM_HEIGHT = 36;
	private static final boolean SNAPSHOT = !SharedConstants.getCurrentVersion().isStable();
	private static boolean snapshotToggle = SNAPSHOT;
	private final CompletableFuture<RealmsAvailability.Result> availability = RealmsAvailability.get();
	@Nullable
	private DataFetcher.Subscription dataSubscription;
	private final Set<UUID> handledSeenNotifications = new HashSet();
	private static boolean regionsPinged;
	private final RateLimiter inviteNarrationLimiter;
	private final Screen lastScreen;
	private Button playButton;
	private Button backButton;
	private Button renewButton;
	private Button configureButton;
	private Button leaveButton;
	private RealmsMainScreen.RealmSelectionList realmSelectionList;
	private RealmsServerList serverList;
	private List<RealmsServer> availableSnapshotServers = List.of();
	private volatile boolean trialsAvailable;
	@Nullable
	private volatile String newsLink;
	long lastClickTime;
	private final List<RealmsNotification> notifications = new ArrayList();
	private Button addRealmButton;
	private RealmsMainScreen.NotificationButton pendingInvitesButton;
	private RealmsMainScreen.NotificationButton newsButton;
	private RealmsMainScreen.LayoutState activeLayoutState;
	@Nullable
	private HeaderAndFooterLayout layout;

	public RealmsMainScreen(Screen screen) {
		super(TITLE);
		this.lastScreen = screen;
		this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
	}

	@Override
	public void init() {
		this.serverList = new RealmsServerList(this.minecraft);
		this.realmSelectionList = this.addRenderableWidget(new RealmsMainScreen.RealmSelectionList());
		Component component = Component.translatable("mco.invites.title");
		this.pendingInvitesButton = new RealmsMainScreen.NotificationButton(
			component, INVITE_SPRITE, button -> this.minecraft.setScreen(new RealmsPendingInvitesScreen(this, component))
		);
		Component component2 = Component.translatable("mco.news");
		this.newsButton = new RealmsMainScreen.NotificationButton(component2, NEWS_SPRITE, button -> {
			if (this.newsLink != null) {
				ConfirmLinkScreen.confirmLinkNow(this.newsLink, this, true);
				if (this.newsButton.notificationCount() != 0) {
					RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
					realmsPersistenceData.hasUnreadNews = false;
					RealmsPersistence.writeFile(realmsPersistenceData);
					this.newsButton.setNotificationCount(0);
				}
			}
		});
		this.newsButton.setTooltip(Tooltip.create(component2));
		this.playButton = Button.builder(PLAY_TEXT, button -> play(this.getSelectedServer(), this)).width(100).build();
		this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, button -> this.configureClicked(this.getSelectedServer())).width(100).build();
		this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, button -> this.onRenew(this.getSelectedServer())).width(100).build();
		this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, button -> this.leaveClicked(this.getSelectedServer())).width(100).build();
		this.addRealmButton = Button.builder(Component.translatable("mco.selectServer.purchase"), button -> this.openTrialAvailablePopup()).size(100, 20).build();
		this.backButton = Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).width(100).build();
		if (RealmsClient.ENVIRONMENT == RealmsClient.Environment.STAGE) {
			this.addRenderableWidget(
				CycleButton.booleanBuilder(Component.literal("Snapshot"), Component.literal("Release"))
					.create(5, 5, 100, 20, Component.literal("Realm"), (cycleButton, boolean_) -> {
						snapshotToggle = boolean_;
						this.availableSnapshotServers = List.of();
						this.debugRefreshDataFetchers();
					})
			);
		}

		this.updateLayout(RealmsMainScreen.LayoutState.LOADING);
		this.updateButtonStates();
		this.availability.thenAcceptAsync(result -> {
			Screen screen = result.createErrorScreen(this.lastScreen);
			if (screen == null) {
				this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
			} else {
				this.minecraft.setScreen(screen);
			}
		}, this.screenExecutor);
	}

	public static boolean isSnapshot() {
		return SNAPSHOT && snapshotToggle;
	}

	@Override
	protected void repositionElements() {
		if (this.layout != null) {
			this.realmSelectionList.updateSize(this.width, this.height, this.layout.getHeaderHeight(), this.height - this.layout.getFooterHeight());
			this.layout.arrangeElements();
		}
	}

	private void updateLayout() {
		if (this.serverList.isEmpty() && this.availableSnapshotServers.isEmpty() && this.notifications.isEmpty()) {
			this.updateLayout(RealmsMainScreen.LayoutState.NO_REALMS);
		} else {
			this.updateLayout(RealmsMainScreen.LayoutState.LIST);
		}
	}

	private void updateLayout(RealmsMainScreen.LayoutState layoutState) {
		if (this.activeLayoutState != layoutState) {
			if (this.layout != null) {
				this.layout.visitWidgets(guiEventListener -> this.removeWidget(guiEventListener));
			}

			this.layout = this.createLayout(layoutState);
			this.activeLayoutState = layoutState;
			this.layout.visitWidgets(guiEventListener -> {
				AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
			});
			this.repositionElements();
		}
	}

	private HeaderAndFooterLayout createLayout(RealmsMainScreen.LayoutState layoutState) {
		HeaderAndFooterLayout headerAndFooterLayout = new HeaderAndFooterLayout(this);
		headerAndFooterLayout.setHeaderHeight(44);
		headerAndFooterLayout.addToHeader(this.createHeader());
		Layout layout = this.createFooter(layoutState);
		layout.arrangeElements();
		headerAndFooterLayout.setFooterHeight(layout.getHeight() + 22);
		headerAndFooterLayout.addToFooter(layout);
		switch (layoutState) {
			case LOADING:
				headerAndFooterLayout.addToContents(new LoadingDotsWidget(this.font, LOADING_TEXT));
				break;
			case NO_REALMS:
				headerAndFooterLayout.addToContents(this.createNoRealmsContent());
		}

		return headerAndFooterLayout;
	}

	private Layout createHeader() {
		int i = 90;
		LinearLayout linearLayout = LinearLayout.horizontal().spacing(4);
		linearLayout.defaultCellSetting().alignVerticallyMiddle();
		linearLayout.addChild(this.pendingInvitesButton);
		linearLayout.addChild(this.newsButton);
		LinearLayout linearLayout2 = LinearLayout.horizontal();
		linearLayout2.defaultCellSetting().alignVerticallyMiddle();
		linearLayout2.addChild(SpacerElement.width(90));
		linearLayout2.addChild(ImageWidget.texture(128, 34, LOGO_LOCATION, 128, 64), LayoutSettings::alignHorizontallyCenter);
		linearLayout2.addChild(new FrameLayout(90, 44)).addChild(linearLayout, LayoutSettings::alignHorizontallyRight);
		return linearLayout2;
	}

	private Layout createFooter(RealmsMainScreen.LayoutState layoutState) {
		GridLayout gridLayout = new GridLayout().spacing(4);
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(3);
		if (layoutState == RealmsMainScreen.LayoutState.LIST) {
			rowHelper.addChild(this.playButton);
			rowHelper.addChild(this.configureButton);
			rowHelper.addChild(this.renewButton);
			rowHelper.addChild(this.leaveButton);
		}

		rowHelper.addChild(this.addRealmButton);
		rowHelper.addChild(this.backButton);
		return gridLayout;
	}

	private LinearLayout createNoRealmsContent() {
		LinearLayout linearLayout = LinearLayout.vertical().spacing(10);
		linearLayout.defaultCellSetting().alignHorizontallyCenter();
		linearLayout.addChild(ImageWidget.texture(130, 64, NO_REALMS_LOCATION, 130, 64));
		FocusableTextWidget focusableTextWidget = new FocusableTextWidget(308, NO_REALMS_TEXT, this.font, false);
		linearLayout.addChild(focusableTextWidget);
		return linearLayout;
	}

	void updateButtonStates() {
		RealmsServer realmsServer = this.getSelectedServer();
		this.addRealmButton.active = this.activeLayoutState != RealmsMainScreen.LayoutState.LOADING;
		this.playButton.active = realmsServer != null && this.shouldPlayButtonBeActive(realmsServer);
		this.renewButton.active = realmsServer != null && this.shouldRenewButtonBeActive(realmsServer);
		this.leaveButton.active = realmsServer != null && this.shouldLeaveButtonBeActive(realmsServer);
		this.configureButton.active = realmsServer != null && this.shouldConfigureButtonBeActive(realmsServer);
	}

	boolean shouldPlayButtonBeActive(RealmsServer realmsServer) {
		boolean bl = !realmsServer.expired && realmsServer.state == RealmsServer.State.OPEN;
		return bl && (realmsServer.isCompatible() || this.isSelfOwnedServer(realmsServer));
	}

	private boolean shouldRenewButtonBeActive(RealmsServer realmsServer) {
		return realmsServer.expired && this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldConfigureButtonBeActive(RealmsServer realmsServer) {
		return this.isSelfOwnedServer(realmsServer);
	}

	private boolean shouldLeaveButtonBeActive(RealmsServer realmsServer) {
		return !this.isSelfOwnedServer(realmsServer);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.dataSubscription != null) {
			this.dataSubscription.tick();
		}
	}

	public static void refreshPendingInvites() {
		Minecraft.getInstance().realmsDataFetcher().pendingInvitesTask.reset();
	}

	public static void refreshServerList() {
		Minecraft.getInstance().realmsDataFetcher().serverListUpdateTask.reset();
	}

	private void debugRefreshDataFetchers() {
		for (DataFetcher.Task<?> task : this.minecraft.realmsDataFetcher().getTasks()) {
			task.reset();
		}
	}

	private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher realmsDataFetcher) {
		DataFetcher.Subscription subscription = realmsDataFetcher.dataFetcher.createSubscription();
		subscription.subscribe(realmsDataFetcher.serverListUpdateTask, serverListData -> {
			this.serverList.updateServersList(serverListData.serverList());
			this.availableSnapshotServers = serverListData.availableSnapshotServers();
			this.refreshListAndLayout();
			boolean bl = false;

			for (RealmsServer realmsServer : this.serverList) {
				if (this.isSelfOwnedNonExpiredServer(realmsServer)) {
					bl = true;
				}
			}

			if (!regionsPinged && bl) {
				regionsPinged = true;
				this.pingRegions();
			}
		});
		callRealmsClient(RealmsClient::getNotifications, list -> {
			this.notifications.clear();
			this.notifications.addAll(list);

			for (RealmsNotification realmsNotification : list) {
				if (realmsNotification instanceof RealmsNotification.InfoPopup infoPopup) {
					PopupScreen popupScreen = infoPopup.buildScreen(this, this::dismissNotification);
					if (popupScreen != null) {
						this.minecraft.setScreen(popupScreen);
						this.markNotificationsAsSeen(List.of(realmsNotification));
						break;
					}
				}
			}

			if (!this.notifications.isEmpty() && this.activeLayoutState != RealmsMainScreen.LayoutState.LOADING) {
				this.refreshListAndLayout();
			}
		});
		subscription.subscribe(realmsDataFetcher.pendingInvitesTask, integer -> {
			this.pendingInvitesButton.setNotificationCount(integer);
			this.pendingInvitesButton.setTooltip(integer == 0 ? NO_PENDING_INVITES : PENDING_INVITES);
			if (integer > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
				this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", integer));
			}
		});
		subscription.subscribe(realmsDataFetcher.trialAvailabilityTask, boolean_ -> this.trialsAvailable = boolean_);
		subscription.subscribe(realmsDataFetcher.newsTask, realmsNews -> {
			realmsDataFetcher.newsManager.updateUnreadNews(realmsNews);
			this.newsLink = realmsDataFetcher.newsManager.newsLink();
			this.newsButton.setNotificationCount(realmsDataFetcher.newsManager.hasUnreadNews() ? Integer.MAX_VALUE : 0);
		});
		return subscription;
	}

	private void markNotificationsAsSeen(Collection<RealmsNotification> collection) {
		List<UUID> list = new ArrayList(collection.size());

		for (RealmsNotification realmsNotification : collection) {
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

	private void refreshListAndLayout() {
		RealmsServer realmsServer = this.getSelectedServer();
		this.realmSelectionList.clear();

		for (RealmsNotification realmsNotification : this.notifications) {
			if (this.addListEntriesForNotification(realmsNotification)) {
				this.markNotificationsAsSeen(List.of(realmsNotification));
				break;
			}
		}

		for (RealmsServer realmsServer2 : this.availableSnapshotServers) {
			this.realmSelectionList.addEntry(new RealmsMainScreen.AvailableSnapshotEntry(realmsServer2));
		}

		for (RealmsServer realmsServer2 : this.serverList) {
			RealmsMainScreen.Entry entry;
			if (isSnapshot() && !realmsServer2.isSnapshotRealm()) {
				if (realmsServer2.state == RealmsServer.State.UNINITIALIZED) {
					continue;
				}

				entry = new RealmsMainScreen.ParentEntry(realmsServer2);
			} else {
				entry = new RealmsMainScreen.ServerEntry(realmsServer2);
			}

			this.realmSelectionList.addEntry(entry);
			if (realmsServer != null && realmsServer.id == realmsServer2.id) {
				this.realmSelectionList.setSelected(entry);
			}
		}

		this.updateLayout();
		this.updateButtonStates();
	}

	private boolean addListEntriesForNotification(RealmsNotification realmsNotification) {
		if (!(realmsNotification instanceof RealmsNotification.VisitUrl visitUrl)) {
			return false;
		} else {
			Component component = visitUrl.getMessage();
			int i = this.font.wordWrapHeight(component, 216);
			int j = Mth.positiveCeilDiv(i + 7, 36) - 1;
			this.realmSelectionList.addEntry(new RealmsMainScreen.NotificationMessageEntry(component, j + 2, visitUrl));

			for (int k = 0; k < j; k++) {
				this.realmSelectionList.addEntry(new RealmsMainScreen.EmptyEntry());
			}

			this.realmSelectionList.addEntry(new RealmsMainScreen.ButtonEntry(visitUrl.buildOpenLinkButton(this)));
			return true;
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

		for (RealmsServer realmsServer : this.serverList) {
			if (this.isSelfOwnedNonExpiredServer(realmsServer)) {
				list.add(realmsServer.id);
			}
		}

		return list;
	}

	private void onRenew(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null) {
			String string = CommonLinks.extendRealms(realmsServer.remoteSubscriptionId, this.minecraft.getUser().getProfileId(), realmsServer.expiredTrial);
			this.minecraft.keyboardHandler.setClipboard(string);
			Util.getPlatform().openUri(string);
		}
	}

	private void configureClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && this.minecraft.isLocalPlayer(realmsServer.ownerUUID)) {
			this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, realmsServer.id));
		}
	}

	private void leaveClicked(@Nullable RealmsServer realmsServer) {
		if (realmsServer != null && !this.minecraft.isLocalPlayer(realmsServer.ownerUUID)) {
			Component component = Component.translatable("mco.configure.world.leave.question.line1");
			Component component2 = Component.translatable("mco.configure.world.leave.question.line2");
			this.minecraft
				.setScreen(new RealmsLongConfirmationScreen(bl -> this.leaveServer(bl, realmsServer), RealmsLongConfirmationScreen.Type.INFO, component, component2, true));
		}
	}

	@Nullable
	private RealmsServer getSelectedServer() {
		return this.realmSelectionList.getSelected() instanceof RealmsMainScreen.ServerEntry serverEntry ? serverEntry.getServer() : null;
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
						RealmsMainScreen.LOGGER.error("Couldn't configure world", (Throwable)var2);
						RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var2, RealmsMainScreen.this)));
					}
				}
			}).start();
		}

		this.minecraft.setScreen(this);
	}

	void removeServer(RealmsServer realmsServer) {
		this.serverList.removeItem(realmsServer);
		this.realmSelectionList.children().removeIf(entry -> {
			if (entry instanceof RealmsMainScreen.ServerEntry serverEntry) {
				RealmsServer realmsServer2 = serverEntry.getServer();
				return realmsServer2.id == realmsServer.id;
			} else {
				return false;
			}
		});
		this.realmSelectionList.setSelected(null);
		this.updateButtonStates();
	}

	void dismissNotification(UUID uUID) {
		callRealmsClient(realmsClient -> {
			realmsClient.notificationsDismiss(List.of(uUID));
			return null;
		}, object -> {
			this.notifications.removeIf(realmsNotification -> realmsNotification.dismissable() && uUID.equals(realmsNotification.uuid()));
			this.refreshListAndLayout();
		});
	}

	public void resetScreen() {
		this.realmSelectionList.setSelected(null);
	}

	@Override
	public Component getNarrationMessage() {
		return (Component)(switch (this.activeLayoutState) {
			case LOADING -> CommonComponents.joinForNarration(super.getNarrationMessage(), LOADING_TEXT);
			case NO_REALMS -> CommonComponents.joinForNarration(super.getNarrationMessage(), NO_REALMS_TEXT);
			case LIST -> super.getNarrationMessage();
		});
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		if (isSnapshot()) {
			guiGraphics.drawString(this.font, "Minecraft " + SharedConstants.getCurrentVersion().getName(), 2, this.height - 10, -1);
		}

		if (this.trialsAvailable && this.addRealmButton.active) {
			RealmsPopupScreen.renderDiamond(guiGraphics, this.addRealmButton);
		}

		switch (RealmsClient.ENVIRONMENT) {
			case STAGE:
				this.renderEnvironment(guiGraphics, "STAGE!", -256);
				break;
			case LOCAL:
				this.renderEnvironment(guiGraphics, "LOCAL!", 8388479);
		}
	}

	private void openTrialAvailablePopup() {
		this.minecraft.setScreen(new RealmsPopupScreen(this, this.trialsAvailable));
	}

	public static void play(@Nullable RealmsServer realmsServer, Screen screen) {
		play(realmsServer, screen, false);
	}

	public static void play(@Nullable RealmsServer realmsServer, Screen screen, boolean bl) {
		if (realmsServer != null) {
			if (!isSnapshot() || bl) {
				Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(screen, new GetServerDetailsTask(screen, realmsServer)));
				return;
			}

			switch (realmsServer.compatibility) {
				case COMPATIBLE:
					Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(screen, new GetServerDetailsTask(screen, realmsServer)));
					break;
				case UNVERIFIABLE:
					confirmToPlay(
						realmsServer,
						screen,
						Component.translatable("mco.compatibility.unverifiable.title").withStyle(ChatFormatting.YELLOW),
						Component.translatable("mco.compatibility.unverifiable.message"),
						CommonComponents.GUI_CONTINUE
					);
					break;
				case NEEDS_DOWNGRADE:
					confirmToPlay(
						realmsServer,
						screen,
						Component.translatable("selectWorld.backupQuestion.downgrade").withStyle(style -> style.withColor(-2142128)),
						Component.translatable(
							"mco.compatibility.downgrade.description",
							Component.literal(realmsServer.activeVersion).withStyle(ChatFormatting.YELLOW),
							Component.literal(SharedConstants.getCurrentVersion().getName()).withStyle(ChatFormatting.YELLOW)
						),
						Component.translatable("mco.compatibility.downgrade")
					);
					break;
				case NEEDS_UPGRADE:
					confirmToPlay(
						realmsServer,
						screen,
						Component.translatable("mco.compatibility.upgrade.title").withStyle(ChatFormatting.YELLOW),
						Component.translatable(
							"mco.compatibility.upgrade.description",
							Component.literal(realmsServer.activeVersion).withStyle(ChatFormatting.YELLOW),
							Component.literal(SharedConstants.getCurrentVersion().getName()).withStyle(ChatFormatting.YELLOW)
						),
						Component.translatable("mco.compatibility.upgrade")
					);
			}
		}
	}

	private static void confirmToPlay(RealmsServer realmsServer, Screen screen, Component component, Component component2, Component component3) {
		Minecraft.getInstance().setScreen(new ConfirmScreen(bl -> {
			Screen screen2;
			if (bl) {
				screen2 = new RealmsLongRunningMcoTaskScreen(screen, new GetServerDetailsTask(screen, realmsServer));
				refreshServerList();
			} else {
				screen2 = screen;
			}

			Minecraft.getInstance().setScreen(screen2);
		}, component, component2, component3, CommonComponents.GUI_CANCEL));
	}

	boolean isSelfOwnedServer(RealmsServer realmsServer) {
		return this.minecraft.isLocalPlayer(realmsServer.ownerUUID);
	}

	private boolean isSelfOwnedNonExpiredServer(RealmsServer realmsServer) {
		return this.isSelfOwnedServer(realmsServer) && !realmsServer.expired;
	}

	private void renderEnvironment(GuiGraphics guiGraphics, String string, int i) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
		guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
		guiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
		guiGraphics.drawString(this.font, string, 0, 0, i, false);
		guiGraphics.pose().popPose();
	}

	@Environment(EnvType.CLIENT)
	class AvailableSnapshotEntry extends RealmsMainScreen.Entry {
		private static final Component START_SNAPSHOT_REALM = Component.translatable("mco.snapshot.start");
		private static final int TEXT_PADDING = 5;
		private final Tooltip tooltip;
		private final RealmsServer parent;

		public AvailableSnapshotEntry(RealmsServer realmsServer) {
			this.parent = realmsServer;
			this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.tooltip"));
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			guiGraphics.blitSprite(RealmsMainScreen.NEW_REALM_SPRITE, k - 5, j + m / 2 - 10, 40, 20);
			int p = j + m / 2 - 9 / 2;
			guiGraphics.drawString(RealmsMainScreen.this.font, START_SNAPSHOT_REALM, k + 40 - 2, p - 5, 8388479);
			guiGraphics.drawString(RealmsMainScreen.this.font, Component.translatable("mco.snapshot.description", this.parent.name), k + 40 - 2, p + 5, -8355712);
			this.tooltip.refreshTooltipForNextRenderPass(bl, this.isFocused(), new ScreenRectangle(k, j, l, m));
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			this.addSnapshotRealm();
			return true;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (CommonInputs.selected(i)) {
				this.addSnapshotRealm();
				return true;
			} else {
				return super.keyPressed(i, j, k);
			}
		}

		private void addSnapshotRealm() {
			RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			RealmsMainScreen.this.minecraft
				.setScreen(
					new PopupScreen.Builder(RealmsMainScreen.this, Component.translatable("mco.snapshot.createSnapshotPopup.title"))
						.setMessage(Component.translatable("mco.snapshot.createSnapshotPopup.text"))
						.addButton(
							Component.translatable("mco.selectServer.create"),
							popupScreen -> RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(RealmsMainScreen.this, this.parent.id))
						)
						.addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose)
						.build()
				);
		}

		@Override
		public Component getNarration() {
			return Component.translatable(
				"gui.narrate.button", CommonComponents.joinForNarration(START_SNAPSHOT_REALM, Component.translatable("mco.snapshot.description", this.parent.name))
			);
		}
	}

	@Environment(EnvType.CLIENT)
	class ButtonEntry extends RealmsMainScreen.Entry {
		private final Button button;

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
			this.button.setPosition(RealmsMainScreen.this.width / 2 - 75, j + 4);
			this.button.render(guiGraphics, n, o, f);
		}

		@Override
		public Component getNarration() {
			return this.button.getMessage();
		}
	}

	@Environment(EnvType.CLIENT)
	static class CrossButton extends ImageButton {
		private static final WidgetSprites SPRITES = new WidgetSprites(
			new ResourceLocation("widget/cross_button"), new ResourceLocation("widget/cross_button_highlighted")
		);

		protected CrossButton(Button.OnPress onPress, Component component) {
			super(0, 0, 14, 14, SPRITES, onPress);
			this.setTooltip(Tooltip.create(component));
		}
	}

	@Environment(EnvType.CLIENT)
	class EmptyEntry extends RealmsMainScreen.Entry {
		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
		}

		@Override
		public Component getNarration() {
			return Component.empty();
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
		private static final int STATUS_LIGHT_WIDTH = 10;
		private static final int STATUS_LIGHT_HEIGHT = 28;
		private static final int PADDING = 7;

		protected void renderStatusLights(RealmsServer realmsServer, GuiGraphics guiGraphics, int i, int j, int k, int l) {
			int m = i - 10 - 7;
			int n = j + 2;
			if (realmsServer.expired) {
				this.drawRealmStatus(guiGraphics, m, n, k, l, RealmsMainScreen.EXPIRED_SPRITE, () -> RealmsMainScreen.SERVER_EXPIRED_TOOLTIP);
			} else if (realmsServer.state == RealmsServer.State.CLOSED) {
				this.drawRealmStatus(guiGraphics, m, n, k, l, RealmsMainScreen.CLOSED_SPRITE, () -> RealmsMainScreen.SERVER_CLOSED_TOOLTIP);
			} else if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.daysLeft < 7) {
				this.drawRealmStatus(
					guiGraphics,
					m,
					n,
					k,
					l,
					RealmsMainScreen.EXPIRES_SOON_SPRITE,
					() -> {
						if (realmsServer.daysLeft <= 0) {
							return RealmsMainScreen.SERVER_EXPIRES_SOON_TOOLTIP;
						} else {
							return (Component)(realmsServer.daysLeft == 1
								? RealmsMainScreen.SERVER_EXPIRES_IN_DAY_TOOLTIP
								: Component.translatable("mco.selectServer.expires.days", realmsServer.daysLeft));
						}
					}
				);
			} else if (realmsServer.state == RealmsServer.State.OPEN) {
				this.drawRealmStatus(guiGraphics, m, n, k, l, RealmsMainScreen.OPEN_SPRITE, () -> RealmsMainScreen.SERVER_OPEN_TOOLTIP);
			}
		}

		private void drawRealmStatus(GuiGraphics guiGraphics, int i, int j, int k, int l, ResourceLocation resourceLocation, Supplier<Component> supplier) {
			guiGraphics.blitSprite(resourceLocation, i, j, 10, 28);
			if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < RealmsMainScreen.this.height - 40 && l > 32) {
				RealmsMainScreen.this.setTooltipForNextRenderPass((Component)supplier.get());
			}
		}

		protected void renderClampedName(GuiGraphics guiGraphics, String string, int i, int j, int k, int l) {
			int m = k - i;
			if (RealmsMainScreen.this.font.width(string) > m) {
				String string2 = RealmsMainScreen.this.font.plainSubstrByWidth(string, m - RealmsMainScreen.this.font.width("... "));
				guiGraphics.drawString(RealmsMainScreen.this.font, string2 + "...", i, j, l, false);
			} else {
				guiGraphics.drawString(RealmsMainScreen.this.font, string, i, j, l, false);
			}
		}

		protected Component getVersionComponent(RealmsServer realmsServer, int i) {
			return (Component)(StringUtils.isBlank(realmsServer.activeVersion)
				? CommonComponents.EMPTY
				: Component.translatable("mco.version", Component.literal(realmsServer.activeVersion).withStyle(style -> style.withColor(i))));
		}

		protected int versionTextX(int i, int j, Component component) {
			return i + j - RealmsMainScreen.this.font.width(component) - 20;
		}

		protected int firstLineY(int i) {
			return i + 1;
		}

		protected int lineHeight() {
			return 2 + 9;
		}

		protected int textX(int i) {
			return i + 36 + 2;
		}

		protected int secondLineY(int i) {
			return i + this.lineHeight();
		}

		protected int thirdLineY(int i) {
			return i + this.lineHeight() * 2;
		}
	}

	@Environment(EnvType.CLIENT)
	static enum LayoutState {
		LOADING,
		NO_REALMS,
		LIST;
	}

	@Environment(EnvType.CLIENT)
	static class NotificationButton extends SpriteIconButton.CenteredIcon {
		private static final ResourceLocation[] NOTIFICATION_ICONS = new ResourceLocation[]{
			new ResourceLocation("notification/1"),
			new ResourceLocation("notification/2"),
			new ResourceLocation("notification/3"),
			new ResourceLocation("notification/4"),
			new ResourceLocation("notification/5"),
			new ResourceLocation("notification/more")
		};
		private static final int UNKNOWN_COUNT = Integer.MAX_VALUE;
		private static final int SIZE = 20;
		private static final int SPRITE_SIZE = 14;
		private int notificationCount;

		public NotificationButton(Component component, ResourceLocation resourceLocation, Button.OnPress onPress) {
			super(20, 20, component, 14, 14, resourceLocation, onPress);
		}

		int notificationCount() {
			return this.notificationCount;
		}

		public void setNotificationCount(int i) {
			this.notificationCount = i;
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			super.renderWidget(guiGraphics, i, j, f);
			if (this.active && this.notificationCount != 0) {
				this.drawNotificationCounter(guiGraphics);
			}
		}

		private void drawNotificationCounter(GuiGraphics guiGraphics) {
			guiGraphics.blitSprite(NOTIFICATION_ICONS[Math.min(this.notificationCount, 6) - 1], this.getX() + this.getWidth() - 5, this.getY() - 3, 8, 8);
		}
	}

	@Environment(EnvType.CLIENT)
	class NotificationMessageEntry extends RealmsMainScreen.Entry {
		private static final int SIDE_MARGINS = 40;
		private static final int OUTLINE_COLOR = -12303292;
		private final Component text;
		private final int frameItemHeight;
		private final List<AbstractWidget> children = new ArrayList();
		@Nullable
		private final RealmsMainScreen.CrossButton dismissButton;
		private final MultiLineTextWidget textWidget;
		private final GridLayout gridLayout;
		private final FrameLayout textFrame;
		private int lastEntryWidth = -1;

		public NotificationMessageEntry(Component component, int i, RealmsNotification realmsNotification) {
			this.text = component;
			this.frameItemHeight = i;
			this.gridLayout = new GridLayout();
			int j = 7;
			this.gridLayout.addChild(ImageWidget.sprite(20, 20, RealmsMainScreen.INFO_SPRITE), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
			this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
			this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3 * (i - 1)), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
			this.textWidget = this.textFrame
				.addChild(
					new MultiLineTextWidget(component, RealmsMainScreen.this.font).setCentered(true),
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
			guiGraphics.renderOutline(k - 2, j - 2, l, 36 * this.frameItemHeight - 2, -12303292);
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
	class ParentEntry extends RealmsMainScreen.Entry {
		private final RealmsServer server;
		private final Tooltip tooltip;

		public ParentEntry(RealmsServer realmsServer) {
			this.server = realmsServer;
			this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.parent.tooltip"));
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			return true;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			RealmsUtil.renderPlayerFace(guiGraphics, k, j, 32, this.server.ownerUUID);
			int p = this.textX(k);
			int q = this.firstLineY(j);
			Component component = this.getVersionComponent(this.server, -8355712);
			int r = this.versionTextX(k, l, component);
			this.renderClampedName(guiGraphics, this.server.getName(), p, q, r, -8355712);
			if (component != CommonComponents.EMPTY) {
				guiGraphics.drawString(RealmsMainScreen.this.font, component, r, q, -8355712, false);
			}

			guiGraphics.drawString(RealmsMainScreen.this.font, this.server.getDescription(), p, this.secondLineY(q), -8355712, false);
			if (!RealmsMainScreen.this.isSelfOwnedServer(this.server)) {
				guiGraphics.drawString(RealmsMainScreen.this.font, this.server.owner, p, this.thirdLineY(q), -8355712, false);
			}

			this.tooltip.refreshTooltipForNextRenderPass(bl, this.isFocused(), new ScreenRectangle(k, j, l, m));
			this.renderStatusLights(this.server, guiGraphics, k + l, j, n, o);
		}

		@Override
		public Component getNarration() {
			return Component.literal(this.server.name);
		}
	}

	@Environment(EnvType.CLIENT)
	class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
		public RealmSelectionList() {
			super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 0, RealmsMainScreen.this.height, 36);
		}

		public void setSelected(@Nullable RealmsMainScreen.Entry entry) {
			super.setSelected(entry);
			RealmsMainScreen.this.updateButtonStates();
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
		@Nullable
		private final Tooltip tooltip;

		public ServerEntry(RealmsServer realmsServer) {
			this.serverData = realmsServer;
			boolean bl = RealmsMainScreen.this.isSelfOwnedServer(realmsServer);
			if (RealmsMainScreen.isSnapshot() && bl && realmsServer.isSnapshotRealm()) {
				this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.paired", realmsServer.parentWorldName));
			} else if (RealmsMainScreen.isSnapshot() && !bl && realmsServer.needsUpgrade()) {
				this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.friendsRealm.upgrade", realmsServer.owner));
			} else if (RealmsMainScreen.isSnapshot() && !bl && realmsServer.needsDowngrade()) {
				this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.friendsRealm.downgrade", realmsServer.activeVersion));
			} else {
				this.tooltip = null;
			}
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
				guiGraphics.blitSprite(RealmsMainScreen.NEW_REALM_SPRITE, k - 5, j + m / 2 - 10, 40, 20);
				int p = j + m / 2 - 9 / 2;
				guiGraphics.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, k + 40 - 2, p, 8388479);
			} else {
				this.renderStatusLights(this.serverData, guiGraphics, k + l, j, n, o);
				int p = this.textX(k);
				int q = this.firstLineY(j);
				int r = this.secondLineY(q);
				int s = this.thirdLineY(q);
				Component component = this.getVersionComponent(this.serverData, this.serverData.isCompatible() ? -8355712 : -2142128);
				int t = this.versionTextX(k, l, component);
				this.renderClampedName(guiGraphics, this.serverData.getName(), p, q, t, -1);
				if (RealmsMainScreen.this.isSelfOwnedServer(this.serverData) && this.serverData.expired) {
					Component component2 = this.serverData.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
					guiGraphics.drawString(RealmsMainScreen.this.font, component2, p, s, 15553363, false);
				} else if (this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
					Component component2 = Component.literal(this.serverData.getMinigameName()).withStyle(ChatFormatting.GRAY);
					guiGraphics.drawString(
						RealmsMainScreen.this.font, Component.translatable("mco.selectServer.minigameName", component2).withStyle(ChatFormatting.YELLOW), p, r, 13413468, false
					);
				} else {
					guiGraphics.drawString(RealmsMainScreen.this.font, this.serverData.getDescription(), p, r, -8355712, false);
					if (component != CommonComponents.EMPTY) {
						guiGraphics.drawString(RealmsMainScreen.this.font, component, t, q, -8355712, false);
					}

					if (!RealmsMainScreen.this.isSelfOwnedServer(this.serverData)) {
						guiGraphics.drawString(RealmsMainScreen.this.font, this.serverData.owner, p, s, -8355712, false);
					}
				}

				if (this.tooltip != null) {
					this.tooltip.refreshTooltipForNextRenderPass(bl, this.isFocused(), new ScreenRectangle(k, j, l, m));
				}

				RealmsUtil.renderPlayerFace(guiGraphics, k, j, 32, this.serverData.ownerUUID);
			}
		}

		private void playRealm() {
			RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			RealmsMainScreen.play(this.serverData, RealmsMainScreen.this);
		}

		private void createUnitializedRealm() {
			RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			RealmsCreateRealmScreen realmsCreateRealmScreen = new RealmsCreateRealmScreen(RealmsMainScreen.this, this.serverData);
			RealmsMainScreen.this.minecraft.setScreen(realmsCreateRealmScreen);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
				this.createUnitializedRealm();
			} else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
				if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isFocused()) {
					this.playRealm();
				}

				RealmsMainScreen.this.lastClickTime = Util.getMillis();
			}

			return true;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (CommonInputs.selected(i)) {
				if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
					this.createUnitializedRealm();
					return true;
				}

				if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
					this.playRealm();
					return true;
				}
			}

			return super.keyPressed(i, j, k);
		}

		@Override
		public Component getNarration() {
			return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED
				? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION
				: Component.translatable("narrator.select", this.serverData.name));
		}

		public RealmsServer getServer() {
			return this.serverData;
		}
	}
}
