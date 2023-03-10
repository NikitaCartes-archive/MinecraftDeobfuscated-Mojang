/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.KeyCombo;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNews;
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
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsMainScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
    private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
    private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
    private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
    private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
    private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
    static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
    private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
    private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
    private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
    static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
    private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
    static final ResourceLocation INFO_ICON_LOCATION = new ResourceLocation("minecraft", "textures/gui/info_icon.png");
    static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
    static final Component PENDING_INVITES_TEXT = Component.translatable("mco.invites.pending");
    static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(Component.translatable("mco.trial.message.line1"), Component.translatable("mco.trial.message.line2"));
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
    private static List<ResourceLocation> teaserImages = ImmutableList.of();
    @Nullable
    private DataFetcher.Subscription dataSubscription;
    private RealmsServerList serverList;
    private final Set<UUID> handledSeenNotifications = new HashSet<UUID>();
    private static boolean overrideConfigure;
    private static int lastScrollYPosition;
    static volatile boolean hasParentalConsent;
    static volatile boolean checkedParentalConsent;
    static volatile boolean checkedClientCompatability;
    @Nullable
    static Screen realmsGenericErrorScreen;
    private static boolean regionsPinged;
    private final RateLimiter inviteNarrationLimiter;
    private boolean dontSetConnectedToRealms;
    final Screen lastScreen;
    RealmSelectionList realmSelectionList;
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
    private final List<RealmsNotification> notifications = new ArrayList<RealmsNotification>();
    private Button showPopupButton;
    private PendingInvitesButton pendingInvitesButton;
    private Button newsButton;
    private Button createTrialButton;
    private Button buyARealmButton;
    private Button closeButton;

    public RealmsMainScreen(Screen screen) {
        super(GameNarrator.NO_TITLE);
        this.lastScreen = screen;
        this.inviteNarrationLimiter = RateLimiter.create(0.01666666753590107);
    }

    private boolean shouldShowMessageInList() {
        if (!RealmsMainScreen.hasParentalConsent() || !this.hasFetchedServers) {
            return false;
        }
        if (this.trialsAvailable && !this.createdTrial) {
            return true;
        }
        for (RealmsServer realmsServer : this.realmsServers) {
            if (!realmsServer.ownerUUID.equals(this.minecraft.getUser().getUuid())) continue;
            return false;
        }
        return true;
    }

    public boolean shouldShowPopup() {
        if (!RealmsMainScreen.hasParentalConsent() || !this.hasFetchedServers) {
            return false;
        }
        if (this.popupOpenedByUser) {
            return true;
        }
        return this.realmsServers.isEmpty();
    }

    @Override
    public void init() {
        this.keyCombos = Lists.newArrayList(new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
            overrideConfigure = !overrideConfigure;
        }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
            if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
                this.switchToProd();
            } else {
                this.switchToStage();
            }
        }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
            if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
                this.switchToProd();
            } else {
                this.switchToLocal();
            }
        }));
        if (realmsGenericErrorScreen != null) {
            this.minecraft.setScreen(realmsGenericErrorScreen);
            return;
        }
        this.connectLock = new ReentrantLock();
        if (checkedClientCompatability && !RealmsMainScreen.hasParentalConsent()) {
            this.checkParentalConsent();
        }
        this.checkClientCompatability();
        if (!this.dontSetConnectedToRealms) {
            this.minecraft.setConnectedToRealms(false);
        }
        this.showingPopup = false;
        this.realmSelectionList = new RealmSelectionList();
        if (lastScrollYPosition != -1) {
            this.realmSelectionList.setScrollAmount(lastScrollYPosition);
        }
        this.addWidget(this.realmSelectionList);
        this.realmsSelectionListAdded = true;
        this.setInitialFocus(this.realmSelectionList);
        this.addMiddleButtons();
        this.addFooterButtons();
        this.addTopButtons();
        this.updateButtonStates(null);
        this.formattedPopup = MultiLineLabel.create(this.font, (FormattedText)POPUP_TEXT, 100);
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

    private static boolean hasParentalConsent() {
        return checkedParentalConsent && hasParentalConsent;
    }

    public void addTopButtons() {
        this.pendingInvitesButton = this.addRenderableWidget(new PendingInvitesButton());
        this.newsButton = this.addRenderableWidget(new NewsButton());
        this.showPopupButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.purchase"), button -> {
            this.popupOpenedByUser = !this.popupOpenedByUser;
        }).bounds(this.width - 90, 6, 80, 20).build());
    }

    public void addMiddleButtons() {
        this.createTrialButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.trial"), button -> {
            if (!this.trialsAvailable || this.createdTrial) {
                return;
            }
            Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20).build());
        this.buyARealmButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.buy"), button -> Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms")).bounds(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20).build());
        this.closeButton = this.addRenderableWidget(new CloseButton());
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
        LinearLayout linearLayout2 = rowHelper.addChild(new LinearLayout(204, 20, LinearLayout.Orientation.HORIZONTAL), rowHelper.newCellSettings().alignHorizontallyCenter());
        linearLayout2.addChild(this.leaveButton);
        linearLayout2.addChild(this.backButton);
        gridLayout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        gridLayout.arrangeElements();
        FrameLayout.centerInRectangle(gridLayout, 0, this.height - 64, this.width, 64);
    }

    void updateButtonStates(@Nullable RealmsServer realmsServer) {
        boolean bl;
        this.backButton.active = true;
        if (!RealmsMainScreen.hasParentalConsent() || !this.hasFetchedServers) {
            RealmsMainScreen.hideWidgets(this.playButton, this.renewButton, this.configureButton, this.createTrialButton, this.buyARealmButton, this.closeButton, this.newsButton, this.pendingInvitesButton, this.showPopupButton, this.leaveButton);
            return;
        }
        this.createTrialButton.visible = bl = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
        this.createTrialButton.active = bl;
        this.buyARealmButton.visible = this.shouldShowPopup();
        this.closeButton.visible = this.shouldShowPopup() && this.popupOpenedByUser;
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
    }

    private boolean shouldShowPopupButton() {
        return (!this.shouldShowPopup() || this.popupOpenedByUser) && RealmsMainScreen.hasParentalConsent() && this.hasFetchedServers;
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
        ++this.animTick;
        boolean bl = RealmsMainScreen.hasParentalConsent();
        if (this.dataSubscription == null && bl) {
            this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
        } else if (this.dataSubscription != null && !bl) {
            this.dataSubscription = null;
        }
        if (this.dataSubscription != null) {
            this.dataSubscription.tick();
        }
        if (this.shouldShowPopup()) {
            ++this.carouselTick;
        }
        if (this.showPopupButton != null) {
            this.showPopupButton.active = this.showPopupButton.visible = this.shouldShowPopupButton();
        }
    }

    private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher realmsDataFetcher) {
        DataFetcher.Subscription subscription = realmsDataFetcher.dataFetcher.createSubscription();
        subscription.subscribe(realmsDataFetcher.serverListUpdateTask, list -> {
            List<RealmsServer> list2 = this.serverList.updateServersList((List<RealmsServer>)list);
            boolean bl = false;
            for (RealmsServer realmsServer : list2) {
                if (!this.isSelfOwnedNonExpiredServer(realmsServer)) continue;
                bl = true;
            }
            this.realmsServers = list2;
            this.hasFetchedServers = true;
            this.refreshRealmsSelectionList();
            if (!regionsPinged && bl) {
                regionsPinged = true;
                this.pingRegions();
            }
        });
        RealmsMainScreen.callRealmsClient(RealmsClient::getNotifications, list -> {
            this.notifications.clear();
            this.notifications.addAll((Collection<RealmsNotification>)list);
            this.refreshRealmsSelectionList();
        });
        subscription.subscribe(realmsDataFetcher.pendingInvitesTask, integer -> {
            this.numberOfPendingInvites = integer;
            if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", this.numberOfPendingInvites));
            }
        });
        subscription.subscribe(realmsDataFetcher.trialAvailabilityTask, boolean_ -> {
            if (this.createdTrial) {
                return;
            }
            if (boolean_ != this.trialsAvailable && this.shouldShowPopup()) {
                this.trialsAvailable = boolean_;
                this.showingPopup = false;
            } else {
                this.trialsAvailable = boolean_;
            }
        });
        subscription.subscribe(realmsDataFetcher.liveStatsTask, realmsServerPlayerLists -> {
            block0: for (RealmsServerPlayerList realmsServerPlayerList : realmsServerPlayerLists.servers) {
                for (RealmsServer realmsServer : this.realmsServers) {
                    if (realmsServer.id != realmsServerPlayerList.serverId) continue;
                    realmsServer.updateServerPing(realmsServerPlayerList);
                    continue block0;
                }
            }
        });
        subscription.subscribe(realmsDataFetcher.newsTask, realmsNews -> {
            realmsDataFetcher.newsManager.updateUnreadNews((RealmsNews)realmsNews);
            this.hasUnreadNews = realmsDataFetcher.newsManager.hasUnreadNews();
            this.newsLink = realmsDataFetcher.newsManager.newsLink();
            this.updateButtonStates(null);
        });
        return subscription;
    }

    private static <T> void callRealmsClient(RealmsCall<T> realmsCall, Consumer<T> consumer) {
        Minecraft minecraft = Minecraft.getInstance();
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
            try {
                return realmsCall.request(RealmsClient.create(minecraft));
            } catch (RealmsServiceException realmsServiceException) {
                throw new RuntimeException(realmsServiceException);
            }
        }).thenAcceptAsync(consumer, (Executor)minecraft)).exceptionally(throwable -> {
            LOGGER.error("Failed to execute call to Realms Service", (Throwable)throwable);
            return null;
        });
    }

    private void refreshRealmsSelectionList() {
        boolean bl = !this.hasFetchedServers;
        this.realmSelectionList.clear();
        ArrayList<UUID> list = new ArrayList<UUID>();
        for (RealmsNotification realmsNotification : this.notifications) {
            this.addEntriesForNotification(this.realmSelectionList, realmsNotification);
            if (realmsNotification.seen() || this.handledSeenNotifications.contains(realmsNotification.uuid())) continue;
            list.add(realmsNotification.uuid());
        }
        if (!list.isEmpty()) {
            RealmsMainScreen.callRealmsClient(realmsClient -> {
                realmsClient.notificationsSeen(list);
                return null;
            }, object -> this.handledSeenNotifications.addAll(list));
        }
        if (this.shouldShowMessageInList()) {
            this.realmSelectionList.addEntry(new TrialEntry());
        }
        ServerEntry entry = null;
        RealmsServer realmsServer = this.getSelectedServer();
        for (RealmsServer realmsServer2 : this.realmsServers) {
            ServerEntry serverEntry = new ServerEntry(realmsServer2);
            this.realmSelectionList.addEntry(serverEntry);
            if (realmsServer == null || realmsServer.id != realmsServer2.id) continue;
            entry = serverEntry;
        }
        if (bl) {
            this.updateButtonStates(null);
        } else {
            this.realmSelectionList.setSelected(entry);
        }
    }

    private void addEntriesForNotification(RealmSelectionList realmSelectionList, RealmsNotification realmsNotification) {
        if (realmsNotification instanceof RealmsNotification.VisitUrl) {
            RealmsNotification.VisitUrl visitUrl = (RealmsNotification.VisitUrl)realmsNotification;
            realmSelectionList.addEntry(new NotificationMessageEntry(visitUrl.getMessage(), visitUrl));
            realmSelectionList.addEntry(new ButtonEntry(visitUrl.buildOpenLinkButton(this)));
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
            } catch (Throwable throwable) {
                LOGGER.warn("Could not send ping result to Realms: ", throwable);
            }
        }).start();
    }

    private List<Long> getOwnedNonExpiredWorldIds() {
        ArrayList<Long> list = Lists.newArrayList();
        for (RealmsServer realmsServer : this.realmsServers) {
            if (!this.isSelfOwnedNonExpiredServer(realmsServer)) continue;
            list.add(realmsServer.id);
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
            new Thread("MCO Compatability Checker #1"){

                @Override
                public void run() {
                    RealmsClient realmsClient = RealmsClient.create();
                    try {
                        RealmsClient.CompatibleVersionResponse compatibleVersionResponse = realmsClient.clientCompatible();
                        if (compatibleVersionResponse != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                            realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen);
                            RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(realmsGenericErrorScreen));
                            return;
                        }
                        RealmsMainScreen.this.checkParentalConsent();
                    } catch (RealmsServiceException realmsServiceException) {
                        checkedClientCompatability = false;
                        LOGGER.error("Couldn't connect to realms", realmsServiceException);
                        if (realmsServiceException.httpResultCode == 401) {
                            realmsGenericErrorScreen = new RealmsGenericErrorScreen(Component.translatable("mco.error.invalid.session.title"), Component.translatable("mco.error.invalid.session.message"), RealmsMainScreen.this.lastScreen);
                            RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(realmsGenericErrorScreen));
                        }
                        RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsServiceException, RealmsMainScreen.this.lastScreen)));
                    }
                }
            }.start();
        }
    }

    void checkParentalConsent() {
        new Thread("MCO Compatability Checker #1"){

            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.create();
                try {
                    Boolean boolean_ = realmsClient.mcoEnabled();
                    if (boolean_.booleanValue()) {
                        LOGGER.info("Realms is available for this user");
                        hasParentalConsent = true;
                    } else {
                        LOGGER.info("Realms is not available for this user");
                        hasParentalConsent = false;
                        RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen)));
                    }
                    checkedParentalConsent = true;
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't connect to realms", realmsServiceException);
                    RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsServiceException, RealmsMainScreen.this.lastScreen)));
                }
            }
        }.start();
    }

    private void switchToStage() {
        if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
            new Thread("MCO Stage Availability Checker #1"){

                @Override
                public void run() {
                    RealmsClient realmsClient = RealmsClient.create();
                    try {
                        Boolean boolean_ = realmsClient.stageAvailable();
                        if (boolean_.booleanValue()) {
                            RealmsClient.switchToStage();
                            LOGGER.info("Switched to stage");
                            RealmsMainScreen.this.refreshFetcher();
                        }
                    } catch (RealmsServiceException realmsServiceException) {
                        LOGGER.error("Couldn't connect to Realms: {}", (Object)realmsServiceException.toString());
                    }
                }
            }.start();
        }
    }

    private void switchToLocal() {
        if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
            new Thread("MCO Local Availability Checker #1"){

                @Override
                public void run() {
                    RealmsClient realmsClient = RealmsClient.create();
                    try {
                        Boolean boolean_ = realmsClient.stageAvailable();
                        if (boolean_.booleanValue()) {
                            RealmsClient.switchToLocal();
                            LOGGER.info("Switched to local");
                            RealmsMainScreen.this.refreshFetcher();
                        }
                    } catch (RealmsServiceException realmsServiceException) {
                        LOGGER.error("Couldn't connect to Realms: {}", (Object)realmsServiceException.toString());
                    }
                }
            }.start();
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
            MutableComponent component = Component.translatable("mco.configure.world.leave.question.line1");
            MutableComponent component2 = Component.translatable("mco.configure.world.leave.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen(bl -> this.leaveServer(bl, realmsServer), RealmsLongConfirmationScreen.Type.Info, component, component2, true));
        }
    }

    private void saveListScrollPosition() {
        lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
    }

    @Nullable
    private RealmsServer getSelectedServer() {
        if (this.realmSelectionList == null) {
            return null;
        }
        Entry entry = (Entry)this.realmSelectionList.getSelected();
        return entry != null ? entry.getServer() : null;
    }

    private void leaveServer(boolean bl, final RealmsServer realmsServer) {
        if (bl) {
            new Thread("Realms-leave-server"){

                @Override
                public void run() {
                    try {
                        RealmsClient realmsClient = RealmsClient.create();
                        realmsClient.uninviteMyselfFrom(realmsServer.id);
                        RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.removeServer(realmsServer));
                    } catch (RealmsServiceException realmsServiceException) {
                        LOGGER.error("Couldn't configure world");
                        RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsServiceException, (Screen)RealmsMainScreen.this)));
                    }
                }
            }.start();
        }
        this.minecraft.setScreen(this);
    }

    void removeServer(RealmsServer realmsServer) {
        this.realmsServers = this.serverList.removeItem(realmsServer);
        this.realmSelectionList.children().removeIf(entry -> {
            RealmsServer realmsServer2 = entry.getServer();
            return realmsServer2 != null && realmsServer2.id == realmsServer.id;
        });
        this.realmSelectionList.setSelected((Entry)null);
        this.updateButtonStates(null);
        this.playButton.active = false;
    }

    void dismissNotification(UUID uUID) {
        RealmsMainScreen.callRealmsClient(realmsClient -> {
            realmsClient.notificationsDismiss(List.of(uUID));
            return null;
        }, object -> {
            this.notifications.removeIf(realmsNotification -> realmsNotification.dismissable() && uUID.equals(realmsNotification.uuid()));
            this.refreshRealmsSelectionList();
        });
    }

    public void resetScreen() {
        if (this.realmSelectionList != null) {
            this.realmSelectionList.setSelected((Entry)null);
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.keyCombos.forEach(KeyCombo::reset);
            this.onClosePopup();
            return true;
        }
        return super.keyPressed(i, j, k);
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
            poseStack.translate(0.0f, 0.0f, 100.0f);
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
            GuiComponent.blit(poseStack, this.createTrialButton.getX() + this.createTrialButton.getWidth() - 8 - 4, this.createTrialButton.getY() + this.createTrialButton.getHeight() / 2 - 4, 0.0f, m, 8, 8, 8, 16);
        }
    }

    private void drawRealmsLogo(PoseStack poseStack, int i, int j) {
        RenderSystem.setShaderTexture(0, LOGO_LOCATION);
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        GuiComponent.blit(poseStack, i * 2, j * 2 - 5, 0.0f, 0.0f, 200, 50, 200, 50);
        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.isOutsidePopup(d, e) && this.popupOpenedByUser) {
            this.popupOpenedByUser = false;
            this.justClosedPopup = true;
            return true;
        }
        return super.mouseClicked(d, e, i);
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
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.7f);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, DARKEN_LOCATION);
        boolean k = false;
        int l = 32;
        GuiComponent.blit(poseStack, 0, 32, 0.0f, 0.0f, this.width, this.height - 40 - 32, 310, 166);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, POPUP_LOCATION);
        GuiComponent.blit(poseStack, i, j, 0.0f, 0.0f, 310, 166, 310, 166);
        if (!teaserImages.isEmpty()) {
            RenderSystem.setShaderTexture(0, teaserImages.get(this.carouselIndex));
            GuiComponent.blit(poseStack, i + 7, j + 7, 0.0f, 0.0f, 195, 152, 195, 152);
            if (this.carouselTick % 95 < 5) {
                if (!this.hasSwitchedCarouselImage) {
                    this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
                    this.hasSwitchedCarouselImage = true;
                }
            } else {
                this.hasSwitchedCarouselImage = false;
            }
        }
        this.formattedPopup.renderLeftAlignedNoShadow(poseStack, this.width / 2 + 52, j + 7, 10, 0xFFFFFF);
    }

    int popupX0() {
        return (this.width - 310) / 2;
    }

    int popupY0() {
        return this.height / 2 - 80;
    }

    void drawInvitationPendingIcon(PoseStack poseStack, int i, int j, int k, int l, boolean bl, boolean bl2) {
        boolean bl7;
        boolean bl6;
        int q;
        int p;
        boolean bl4;
        int m = this.numberOfPendingInvites;
        boolean bl3 = this.inPendingInvitationArea(i, j);
        boolean bl5 = bl4 = bl2 && bl;
        if (bl4) {
            float f = 0.25f + (1.0f + Mth.sin((float)this.animTick * 0.5f)) * 0.25f;
            int n = 0xFF000000 | (int)(f * 64.0f) << 16 | (int)(f * 64.0f) << 8 | (int)(f * 64.0f) << 0;
            int o = k - 2;
            p = k + 16;
            q = l + 1;
            int r = l + 16;
            RealmsMainScreen.fillGradient(poseStack, o, q, p, r, n, n);
            n = 0xFF000000 | (int)(f * 255.0f) << 16 | (int)(f * 255.0f) << 8 | (int)(f * 255.0f) << 0;
            RealmsMainScreen.fillGradient(poseStack, o, l, p, l + 1, n, n);
            RealmsMainScreen.fillGradient(poseStack, o - 1, l, o, r + 1, n, n);
            RealmsMainScreen.fillGradient(poseStack, p, l, p + 1, r, n, n);
            RealmsMainScreen.fillGradient(poseStack, o, r, p + 1, r + 1, n, n);
        }
        RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
        boolean bl52 = bl2 && bl;
        float g = bl52 ? 16.0f : 0.0f;
        GuiComponent.blit(poseStack, k, l - 6, g, 0.0f, 15, 25, 31, 25);
        boolean bl8 = bl6 = bl2 && m != 0;
        if (bl6) {
            p = (Math.min(m, 6) - 1) * 8;
            q = (int)(Math.max(0.0f, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57f), Mth.cos((float)this.animTick * 0.35f))) * -6.0f);
            RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
            float h = bl3 ? 8.0f : 0.0f;
            GuiComponent.blit(poseStack, k + 4, l + 4 + q, p, h, 8, 8, 48, 16);
        }
        p = i + 12;
        q = j;
        boolean bl9 = bl7 = bl2 && bl3;
        if (bl7) {
            Component component = m == 0 ? NO_PENDING_INVITES_TEXT : PENDING_INVITES_TEXT;
            int s = this.font.width(component);
            RealmsMainScreen.fillGradient(poseStack, p - 3, q - 3, p + s + 3, q + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(poseStack, component, (float)p, (float)q, -1);
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
            } catch (InterruptedException interruptedException) {
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
        GuiComponent.blit(poseStack, i, j, 0.0f, 0.0f, 10, 28, 10, 28);
        if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
            this.setTooltipForNextRenderPass(SERVER_EXPIRED_TOOLTIP);
        }
    }

    void drawExpiring(PoseStack poseStack, int i, int j, int k, int l, int m) {
        RenderSystem.setShaderTexture(0, EXPIRES_SOON_ICON_LOCATION);
        if (this.animTick % 20 < 10) {
            GuiComponent.blit(poseStack, i, j, 0.0f, 0.0f, 10, 28, 20, 28);
        } else {
            GuiComponent.blit(poseStack, i, j, 10.0f, 0.0f, 10, 28, 20, 28);
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
        GuiComponent.blit(poseStack, i, j, 0.0f, 0.0f, 10, 28, 10, 28);
        if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
            this.setTooltipForNextRenderPass(SERVER_OPEN_TOOLTIP);
        }
    }

    void drawClose(PoseStack poseStack, int i, int j, int k, int l) {
        RenderSystem.setShaderTexture(0, OFF_ICON_LOCATION);
        GuiComponent.blit(poseStack, i, j, 0.0f, 0.0f, 10, 28, 10, 28);
        if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
            this.setTooltipForNextRenderPass(SERVER_CLOSED_TOOLTIP);
        }
    }

    void renderNews(PoseStack poseStack, int i, int j, boolean bl, int k, int l, boolean bl2, boolean bl3) {
        boolean bl4 = false;
        if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
            bl4 = true;
        }
        RenderSystem.setShaderTexture(0, NEWS_LOCATION);
        if (!bl3) {
            RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);
        }
        boolean bl5 = bl3 && bl2;
        float f = bl5 ? 20.0f : 0.0f;
        GuiComponent.blit(poseStack, k, l, f, 0.0f, 20, 20, 40, 20);
        if (bl4 && bl3) {
            this.setTooltipForNextRenderPass(NEWS_TOOLTIP);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (bl && bl3) {
            int m = bl4 ? 0 : (int)(Math.max(0.0f, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57f), Mth.cos((float)this.animTick * 0.35f))) * -6.0f);
            RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
            GuiComponent.blit(poseStack, k + 10, l + 2 + m, 40.0f, 0.0f, 8, 8, 48, 16);
        }
    }

    private void renderLocal(PoseStack poseStack) {
        String string = "LOCAL!";
        poseStack.pushPose();
        poseStack.translate(this.width / 2 - 25, 20.0f, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0f));
        poseStack.scale(1.5f, 1.5f, 1.5f);
        this.font.draw(poseStack, "LOCAL!", 0.0f, 0.0f, 0x7FFF7F);
        poseStack.popPose();
    }

    private void renderStage(PoseStack poseStack) {
        String string = "STAGE!";
        poseStack.pushPose();
        poseStack.translate(this.width / 2 - 25, 20.0f, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0f));
        poseStack.scale(1.5f, 1.5f, 1.5f);
        this.font.draw(poseStack, "STAGE!", 0.0f, 0.0f, -256);
        poseStack.popPose();
    }

    public RealmsMainScreen newScreen() {
        RealmsMainScreen realmsMainScreen = new RealmsMainScreen(this.lastScreen);
        realmsMainScreen.init(this.minecraft, this.width, this.height);
        return realmsMainScreen;
    }

    public static void updateTeaserImages(ResourceManager resourceManager) {
        Set<ResourceLocation> collection = resourceManager.listResources("textures/gui/images", resourceLocation -> resourceLocation.getPath().endsWith(".png")).keySet();
        teaserImages = collection.stream().filter(resourceLocation -> resourceLocation.getNamespace().equals("realms")).toList();
    }

    private void pendingButtonPress(Button button) {
        this.minecraft.setScreen(new RealmsPendingInvitesScreen(this.lastScreen));
    }

    static {
        lastScrollYPosition = -1;
    }

    @Environment(value=EnvType.CLIENT)
    class RealmSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public RealmSelectionList() {
            super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 64, 36);
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            if (i == 257 || i == 32 || i == 335) {
                Entry entry = (Entry)this.getSelected();
                if (entry == null) {
                    return super.keyPressed(i, j, k);
                }
                entry.keyPressed(i, j, k);
            }
            return super.keyPressed(i, j, k);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (i == 0 && d < (double)this.getScrollbarPosition() && e >= (double)this.y0 && e <= (double)this.y1) {
                int j = RealmsMainScreen.this.realmSelectionList.getRowLeft();
                int k = this.getScrollbarPosition();
                int l = (int)Math.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int m = l / this.itemHeight;
                if (d >= (double)j && d <= (double)k && m >= 0 && l >= 0 && m < this.getItemCount()) {
                    this.itemClicked(l, m, d, e, this.width, i);
                    this.selectItem(m);
                }
                return true;
            }
            return super.mouseClicked(d, e, i);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                RealmsMainScreen.this.updateButtonStates(entry.getServer());
            } else {
                RealmsMainScreen.this.updateButtonStates(null);
            }
        }

        @Override
        public void itemClicked(int i, int j, double d, double e, int k, int l) {
            Entry entry = (Entry)this.getEntry(j);
            if (entry.mouseClicked(d, e, l)) {
                return;
            }
            if (entry instanceof TrialEntry) {
                RealmsMainScreen.this.popupOpenedByUser = true;
                return;
            }
            RealmsServer realmsServer = entry.getServer();
            if (realmsServer == null) {
                return;
            }
            if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
                Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(realmsServer, RealmsMainScreen.this));
                return;
            }
            if (RealmsMainScreen.this.shouldPlayButtonBeActive(realmsServer)) {
                if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isSelectedItem(j)) {
                    RealmsMainScreen.this.play(realmsServer, RealmsMainScreen.this);
                }
                RealmsMainScreen.this.lastClickTime = Util.getMillis();
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

    @Environment(value=EnvType.CLIENT)
    class PendingInvitesButton
    extends Button {
        public PendingInvitesButton() {
            super(RealmsMainScreen.this.width / 2 + 50, 6, 22, 22, CommonComponents.EMPTY, RealmsMainScreen.this::pendingButtonPress, DEFAULT_NARRATION);
        }

        public void tick() {
            this.setMessage(RealmsMainScreen.this.numberOfPendingInvites == 0 ? NO_PENDING_INVITES_TEXT : PENDING_INVITES_TEXT);
        }

        @Override
        public void renderWidget(PoseStack poseStack, int i, int j, float f) {
            RealmsMainScreen.this.drawInvitationPendingIcon(poseStack, i, j, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class NewsButton
    extends Button {
        public NewsButton() {
            super(RealmsMainScreen.this.width - 115, 6, 20, 20, Component.translatable("mco.news"), button -> {
                if (realmsMainScreen.newsLink == null) {
                    return;
                }
                ConfirmLinkScreen.confirmLinkNow(realmsMainScreen.newsLink, RealmsMainScreen.this, true);
                if (realmsMainScreen.hasUnreadNews) {
                    RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
                    realmsPersistenceData.hasUnreadNews = false;
                    realmsMainScreen.hasUnreadNews = false;
                    RealmsPersistence.writeFile(realmsPersistenceData);
                }
            }, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(PoseStack poseStack, int i, int j, float f) {
            RealmsMainScreen.this.renderNews(poseStack, i, j, RealmsMainScreen.this.hasUnreadNews, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class CloseButton
    extends CrossButton {
        public CloseButton() {
            super(RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, button -> RealmsMainScreen.this.onClosePopup(), Component.translatable("mco.selectServer.close"));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface RealmsCall<T> {
        public T request(RealmsClient var1) throws RealmsServiceException;
    }

    @Environment(value=EnvType.CLIENT)
    class TrialEntry
    extends Entry {
        TrialEntry() {
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
            boolean bl = false;
            if (j <= l && l <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && k <= m && m <= k + 32) {
                bl = true;
            }
            int p = 0x7FFF7F;
            if (bl && !RealmsMainScreen.this.shouldShowPopup()) {
                p = 6077788;
            }
            for (Component component : TRIAL_MESSAGE_LINES) {
                GuiComponent.drawCenteredString(poseStack, RealmsMainScreen.this.font, component, RealmsMainScreen.this.width / 2, n + o, p);
                o += 10;
            }
        }

        @Override
        public Component getNarration() {
            return TRIAL_TEXT;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ServerEntry
    extends Entry {
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
                RenderSystem.setShaderTexture(0, WORLDICON_LOCATION);
                GuiComponent.blit(poseStack, i + 10, j + 6, 0.0f, 0.0f, 40, 20, 40, 20);
                float f = 0.5f + (1.0f + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25f)) * 0.25f;
                int m = 0xFF000000 | (int)(127.0f * f) << 16 | (int)(255.0f * f) << 8 | (int)(127.0f * f);
                GuiComponent.drawCenteredString(poseStack, RealmsMainScreen.this.font, SERVER_UNITIALIZED_TEXT, i + 10 + 40 + 75, j + 12, m);
                return;
            }
            int n = 225;
            int m = 2;
            this.renderStatusLights(realmsServer, poseStack, i, j, k, l, 225, 2);
            if (!"0".equals(realmsServer.serverPing.nrOfPlayers)) {
                String string = ChatFormatting.GRAY + realmsServer.serverPing.nrOfPlayers;
                RealmsMainScreen.this.font.draw(poseStack, string, (float)(i + 207 - RealmsMainScreen.this.font.width(string)), (float)(j + 3), 0x808080);
                if (k >= i + 207 - RealmsMainScreen.this.font.width(string) && k <= i + 207 && l >= j + 1 && l <= j + 10 && l < RealmsMainScreen.this.height - 40 && l > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                    RealmsMainScreen.this.setTooltipForNextRenderPass(Component.literal(realmsServer.serverPing.playerList));
                }
            }
            if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.expired) {
                Component component = realmsServer.expiredTrial ? TRIAL_EXPIRED_TEXT : SUBSCRIPTION_EXPIRED_TEXT;
                int o = j + 11 + 5;
                RealmsMainScreen.this.font.draw(poseStack, component, (float)(i + 2), (float)(o + 1), 15553363);
            } else {
                if (realmsServer.worldType == RealmsServer.WorldType.MINIGAME) {
                    int p = 0xCCAC5C;
                    int o = RealmsMainScreen.this.font.width(SELECT_MINIGAME_PREFIX);
                    RealmsMainScreen.this.font.draw(poseStack, SELECT_MINIGAME_PREFIX, (float)(i + 2), (float)(j + 12), 0xCCAC5C);
                    RealmsMainScreen.this.font.draw(poseStack, realmsServer.getMinigameName(), (float)(i + 2 + o), (float)(j + 12), 0x6C6C6C);
                } else {
                    RealmsMainScreen.this.font.draw(poseStack, realmsServer.getDescription(), (float)(i + 2), (float)(j + 12), 0x6C6C6C);
                }
                if (!RealmsMainScreen.this.isSelfOwnedServer(realmsServer)) {
                    RealmsMainScreen.this.font.draw(poseStack, realmsServer.owner, (float)(i + 2), (float)(j + 12 + 11), 0x4C4C4C);
                }
            }
            RealmsMainScreen.this.font.draw(poseStack, realmsServer.getName(), (float)(i + 2), (float)(j + 1), 0xFFFFFF);
            RealmsUtil.renderPlayerFace(poseStack, i - 36, j, 32, realmsServer.ownerUUID);
        }

        private void renderStatusLights(RealmsServer realmsServer, PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
            int o = i + m + 22;
            if (realmsServer.expired) {
                RealmsMainScreen.this.drawExpired(poseStack, o, j + n, k, l);
            } else if (realmsServer.state == RealmsServer.State.CLOSED) {
                RealmsMainScreen.this.drawClose(poseStack, o, j + n, k, l);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(realmsServer) && realmsServer.daysLeft < 7) {
                RealmsMainScreen.this.drawExpiring(poseStack, o, j + n, k, l, realmsServer.daysLeft);
            } else if (realmsServer.state == RealmsServer.State.OPEN) {
                RealmsMainScreen.this.drawOpen(poseStack, o, j + n, k, l);
            }
        }

        @Override
        public Component getNarration() {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                return UNITIALIZED_WORLD_NARRATION;
            }
            return Component.translatable("narrator.select", this.serverData.name);
        }

        @Override
        @Nullable
        public RealmsServer getServer() {
            return this.serverData;
        }
    }

    @Environment(value=EnvType.CLIENT)
    abstract class Entry
    extends ObjectSelectionList.Entry<Entry> {
        Entry() {
        }

        @Nullable
        public RealmsServer getServer() {
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class NotificationMessageEntry
    extends Entry {
        private static final int SIDE_MARGINS = 40;
        private static final int ITEM_HEIGHT = 36;
        private static final int OUTLINE_COLOR = -12303292;
        private final Component text;
        private final List<AbstractWidget> children = new ArrayList<AbstractWidget>();
        @Nullable
        private final CrossButton dismissButton;
        private final MultiLineTextWidget textWidget;
        private final GridLayout gridLayout;
        private final FrameLayout textFrame;
        private int lastEntryWidth = -1;

        public NotificationMessageEntry(Component component, RealmsNotification realmsNotification) {
            this.text = component;
            this.gridLayout = new GridLayout();
            int i = 7;
            this.gridLayout.addChild(new ImageWidget(20, 20, INFO_ICON_LOCATION), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
            this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
            this.textFrame = this.gridLayout.addChild(new FrameLayout(0, ((RealmsMainScreen)RealmsMainScreen.this).font.lineHeight * 3), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
            this.textWidget = this.textFrame.addChild(new MultiLineTextWidget(component, RealmsMainScreen.this.font).setCentered(true).setMaxRows(3), this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop());
            this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
            this.dismissButton = realmsNotification.dismissable() ? this.gridLayout.addChild(new CrossButton(button -> RealmsMainScreen.this.dismissNotification(realmsNotification.uuid()), Component.translatable("mco.notification.dismiss")), 0, 2, this.gridLayout.newCellSettings().alignHorizontallyRight().padding(0, 7, 7, 0)) : null;
            this.gridLayout.visitWidgets(this.children::add);
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            if (this.dismissButton != null && this.dismissButton.keyPressed(i, j, k)) {
                return true;
            }
            return super.keyPressed(i, j, k);
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
        public void renderBack(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            super.renderBack(poseStack, i, j, k, l, m, n, o, bl, f);
            GuiComponent.renderOutline(poseStack, k - 2, j - 2, l, 70, -12303292);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.gridLayout.setPosition(k, j);
            this.updateEntryWidth(l - 4);
            this.children.forEach(abstractWidget -> abstractWidget.render(poseStack, n, o, f));
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (this.dismissButton != null && this.dismissButton.mouseClicked(d, e, i)) {
                return true;
            }
            return super.mouseClicked(d, e, i);
        }

        @Override
        public Component getNarration() {
            return this.text;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ButtonEntry
    extends Entry {
        private final Button button;
        private final int xPos;

        public ButtonEntry(Button button) {
            this.xPos = RealmsMainScreen.this.width / 2 - 75;
            this.button = button;
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (this.button.isMouseOver(d, e)) {
                return this.button.mouseClicked(d, e, i);
            }
            return false;
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            if (this.button.keyPressed(i, j, k)) {
                return true;
            }
            return super.keyPressed(i, j, k);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.button.setPosition(this.xPos, j + 4);
            this.button.render(poseStack, n, o, f);
        }

        @Override
        public Component getNarration() {
            return this.button.getMessage();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CrossButton
    extends Button {
        protected CrossButton(Button.OnPress onPress, Component component) {
            this(0, 0, onPress, component);
        }

        protected CrossButton(int i, int j, Button.OnPress onPress, Component component) {
            super(i, j, 14, 14, component, onPress, DEFAULT_NARRATION);
            this.setTooltip(Tooltip.create(component));
        }

        @Override
        public void renderWidget(PoseStack poseStack, int i, int j, float f) {
            RenderSystem.setShaderTexture(0, CROSS_ICON_LOCATION);
            float g = this.isHoveredOrFocused() ? 14.0f : 0.0f;
            CrossButton.blit(poseStack, this.getX(), this.getY(), 0.0f, g, 14, 14, 14, 28);
        }
    }
}

