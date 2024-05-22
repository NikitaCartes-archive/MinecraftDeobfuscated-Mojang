package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsAvailability;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.task.DataFetcher;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
	private static final ResourceLocation UNSEEN_NOTIFICATION_SPRITE = ResourceLocation.withDefaultNamespace("icon/unseen_notification");
	private static final ResourceLocation NEWS_SPRITE = ResourceLocation.withDefaultNamespace("icon/news");
	private static final ResourceLocation INVITE_SPRITE = ResourceLocation.withDefaultNamespace("icon/invite");
	private static final ResourceLocation TRIAL_AVAILABLE_SPRITE = ResourceLocation.withDefaultNamespace("icon/trial_available");
	private final CompletableFuture<Boolean> validClient = RealmsAvailability.get().thenApply(result -> result.type() == RealmsAvailability.Type.SUCCESS);
	@Nullable
	private DataFetcher.Subscription realmsDataSubscription;
	@Nullable
	private RealmsNotificationsScreen.DataFetcherConfiguration currentConfiguration;
	private volatile int numberOfPendingInvites;
	private static boolean trialAvailable;
	private static boolean hasUnreadNews;
	private static boolean hasUnseenNotifications;
	private final RealmsNotificationsScreen.DataFetcherConfiguration showAll = new RealmsNotificationsScreen.DataFetcherConfiguration() {
		@Override
		public DataFetcher.Subscription initDataFetcher(RealmsDataFetcher realmsDataFetcher) {
			DataFetcher.Subscription subscription = realmsDataFetcher.dataFetcher.createSubscription();
			RealmsNotificationsScreen.this.addNewsAndInvitesSubscriptions(realmsDataFetcher, subscription);
			RealmsNotificationsScreen.this.addNotificationsSubscriptions(realmsDataFetcher, subscription);
			return subscription;
		}

		@Override
		public boolean showOldNotifications() {
			return true;
		}
	};
	private final RealmsNotificationsScreen.DataFetcherConfiguration onlyNotifications = new RealmsNotificationsScreen.DataFetcherConfiguration() {
		@Override
		public DataFetcher.Subscription initDataFetcher(RealmsDataFetcher realmsDataFetcher) {
			DataFetcher.Subscription subscription = realmsDataFetcher.dataFetcher.createSubscription();
			RealmsNotificationsScreen.this.addNotificationsSubscriptions(realmsDataFetcher, subscription);
			return subscription;
		}

		@Override
		public boolean showOldNotifications() {
			return false;
		}
	};

	public RealmsNotificationsScreen() {
		super(GameNarrator.NO_TITLE);
	}

	@Override
	public void init() {
		if (this.realmsDataSubscription != null) {
			this.realmsDataSubscription.forceUpdate();
		}
	}

	@Override
	public void added() {
		super.added();
		this.minecraft.realmsDataFetcher().notificationsTask.reset();
	}

	@Nullable
	private RealmsNotificationsScreen.DataFetcherConfiguration getConfiguration() {
		boolean bl = this.inTitleScreen() && (Boolean)this.validClient.getNow(false);
		if (!bl) {
			return null;
		} else {
			return this.getRealmsNotificationsEnabled() ? this.showAll : this.onlyNotifications;
		}
	}

	@Override
	public void tick() {
		RealmsNotificationsScreen.DataFetcherConfiguration dataFetcherConfiguration = this.getConfiguration();
		if (!Objects.equals(this.currentConfiguration, dataFetcherConfiguration)) {
			this.currentConfiguration = dataFetcherConfiguration;
			if (this.currentConfiguration != null) {
				this.realmsDataSubscription = this.currentConfiguration.initDataFetcher(this.minecraft.realmsDataFetcher());
			} else {
				this.realmsDataSubscription = null;
			}
		}

		if (this.realmsDataSubscription != null) {
			this.realmsDataSubscription.tick();
		}
	}

	private boolean getRealmsNotificationsEnabled() {
		return this.minecraft.options.realmsNotifications().get();
	}

	private boolean inTitleScreen() {
		return this.minecraft.screen instanceof TitleScreen;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		if ((Boolean)this.validClient.getNow(false)) {
			this.drawIcons(guiGraphics);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
	}

	private void drawIcons(GuiGraphics guiGraphics) {
		int i = this.numberOfPendingInvites;
		int j = 24;
		int k = this.height / 4 + 48;
		int l = this.width / 2 + 100;
		int m = k + 48 + 2;
		int n = l - 3;
		if (hasUnseenNotifications) {
			guiGraphics.blitSprite(UNSEEN_NOTIFICATION_SPRITE, n - 12, m + 3, 10, 10);
			n -= 16;
		}

		if (this.currentConfiguration != null && this.currentConfiguration.showOldNotifications()) {
			if (hasUnreadNews) {
				guiGraphics.blitSprite(NEWS_SPRITE, n - 14, m + 1, 14, 14);
				n -= 16;
			}

			if (i != 0) {
				guiGraphics.blitSprite(INVITE_SPRITE, n - 14, m + 1, 14, 14);
				n -= 16;
			}

			if (trialAvailable) {
				guiGraphics.blitSprite(TRIAL_AVAILABLE_SPRITE, n - 10, m + 4, 8, 8);
			}
		}
	}

	void addNewsAndInvitesSubscriptions(RealmsDataFetcher realmsDataFetcher, DataFetcher.Subscription subscription) {
		subscription.subscribe(realmsDataFetcher.pendingInvitesTask, integer -> this.numberOfPendingInvites = integer);
		subscription.subscribe(realmsDataFetcher.trialAvailabilityTask, boolean_ -> trialAvailable = boolean_);
		subscription.subscribe(realmsDataFetcher.newsTask, realmsNews -> {
			realmsDataFetcher.newsManager.updateUnreadNews(realmsNews);
			hasUnreadNews = realmsDataFetcher.newsManager.hasUnreadNews();
		});
	}

	void addNotificationsSubscriptions(RealmsDataFetcher realmsDataFetcher, DataFetcher.Subscription subscription) {
		subscription.subscribe(realmsDataFetcher.notificationsTask, list -> {
			hasUnseenNotifications = false;

			for (RealmsNotification realmsNotification : list) {
				if (!realmsNotification.seen()) {
					hasUnseenNotifications = true;
					break;
				}
			}
		});
	}

	@Environment(EnvType.CLIENT)
	interface DataFetcherConfiguration {
		DataFetcher.Subscription initDataFetcher(RealmsDataFetcher realmsDataFetcher);

		boolean showOldNotifications();
	}
}
