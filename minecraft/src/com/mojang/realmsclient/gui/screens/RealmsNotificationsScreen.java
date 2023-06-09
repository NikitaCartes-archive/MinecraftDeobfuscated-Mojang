package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.task.DataFetcher;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
	private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
	private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
	private static final ResourceLocation NEWS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_notification_mainscreen.png");
	private static final ResourceLocation UNSEEN_NOTIFICATION_ICON_LOCATION = new ResourceLocation("minecraft", "textures/gui/unseen_notification.png");
	@Nullable
	private DataFetcher.Subscription realmsDataSubscription;
	@Nullable
	private RealmsNotificationsScreen.DataFetcherConfiguration currentConfiguration;
	private volatile int numberOfPendingInvites;
	static boolean checkedMcoAvailability;
	private static boolean trialAvailable;
	static boolean validClient;
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
		this.checkIfMcoEnabled();
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
		boolean bl = this.inTitleScreen() && validClient;
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

	private void checkIfMcoEnabled() {
		if (!checkedMcoAvailability) {
			checkedMcoAvailability = true;
			(new Thread("Realms Notification Availability checker #1") {
				public void run() {
					RealmsClient realmsClient = RealmsClient.create();

					try {
						RealmsClient.CompatibleVersionResponse compatibleVersionResponse = realmsClient.clientCompatible();
						if (compatibleVersionResponse != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
							return;
						}
					} catch (RealmsServiceException var3) {
						if (var3.httpResultCode != 401) {
							RealmsNotificationsScreen.checkedMcoAvailability = false;
						}

						return;
					}

					RealmsNotificationsScreen.validClient = true;
				}
			}).start();
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (validClient) {
			this.drawIcons(guiGraphics);
		}

		super.render(guiGraphics, i, j, f);
	}

	private void drawIcons(GuiGraphics guiGraphics) {
		int i = this.numberOfPendingInvites;
		int j = 24;
		int k = this.height / 4 + 48;
		int l = this.width / 2 + 80;
		int m = k + 48 + 2;
		int n = 0;
		if (hasUnseenNotifications) {
			guiGraphics.blit(UNSEEN_NOTIFICATION_ICON_LOCATION, l - n + 5, m + 3, 0.0F, 0.0F, 10, 10, 10, 10);
			n += 14;
		}

		if (this.currentConfiguration != null && this.currentConfiguration.showOldNotifications()) {
			if (hasUnreadNews) {
				guiGraphics.pose().pushPose();
				guiGraphics.pose().scale(0.4F, 0.4F, 0.4F);
				guiGraphics.blit(NEWS_ICON_LOCATION, (int)((double)(l + 2 - n) * 2.5), (int)((double)m * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
				guiGraphics.pose().popPose();
				n += 14;
			}

			if (i != 0) {
				guiGraphics.blit(INVITE_ICON_LOCATION, l - n, m, 0.0F, 0.0F, 18, 15, 18, 30);
				n += 16;
			}

			if (trialAvailable) {
				int o = 0;
				if ((Util.getMillis() / 800L & 1L) == 1L) {
					o = 8;
				}

				guiGraphics.blit(TRIAL_ICON_LOCATION, l + 4 - n, m + 4, 0.0F, (float)o, 8, 8, 8, 16);
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
