package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.GuiComponent;
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (validClient) {
			this.drawIcons(poseStack, i, j);
		}

		super.render(poseStack, i, j, f);
	}

	private void drawIcons(PoseStack poseStack, int i, int j) {
		int k = this.numberOfPendingInvites;
		int l = 24;
		int m = this.height / 4 + 48;
		int n = this.width / 2 + 80;
		int o = m + 48 + 2;
		int p = 0;
		if (hasUnseenNotifications) {
			RenderSystem.setShaderTexture(0, UNSEEN_NOTIFICATION_ICON_LOCATION);
			GuiComponent.blit(poseStack, n - p + 5, o + 3, 0.0F, 0.0F, 10, 10, 10, 10);
			p += 14;
		}

		if (this.currentConfiguration != null && this.currentConfiguration.showOldNotifications()) {
			if (hasUnreadNews) {
				RenderSystem.setShaderTexture(0, NEWS_ICON_LOCATION);
				poseStack.pushPose();
				poseStack.scale(0.4F, 0.4F, 0.4F);
				GuiComponent.blit(poseStack, (int)((double)(n + 2 - p) * 2.5), (int)((double)o * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
				poseStack.popPose();
				p += 14;
			}

			if (k != 0) {
				RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
				GuiComponent.blit(poseStack, n - p, o - 6, 0.0F, 0.0F, 15, 25, 31, 25);
				p += 16;
			}

			if (trialAvailable) {
				RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
				int q = 0;
				if ((Util.getMillis() / 800L & 1L) == 1L) {
					q = 8;
				}

				GuiComponent.blit(poseStack, n + 4 - p, o + 4, 0.0F, (float)q, 8, 8, 8, 16);
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
