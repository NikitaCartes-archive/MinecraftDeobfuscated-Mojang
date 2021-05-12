package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
	private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
	private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
	private static final ResourceLocation NEWS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_notification_mainscreen.png");
	private static final RealmsDataFetcher REALMS_DATA_FETCHER = new RealmsDataFetcher(Minecraft.getInstance(), RealmsClient.create());
	private volatile int numberOfPendingInvites;
	static boolean checkedMcoAvailability;
	private static boolean trialAvailable;
	static boolean validClient;
	private static boolean hasUnreadNews;

	@Override
	public void init() {
		this.checkIfMcoEnabled();
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
	}

	@Override
	public void tick() {
		if ((!this.getRealmsNotificationsEnabled() || !this.inTitleScreen() || !validClient) && !REALMS_DATA_FETCHER.isStopped()) {
			REALMS_DATA_FETCHER.stop();
		} else if (validClient && this.getRealmsNotificationsEnabled()) {
			REALMS_DATA_FETCHER.initWithSpecificTaskList();
			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
				this.numberOfPendingInvites = REALMS_DATA_FETCHER.getPendingInvitesCount();
			}

			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE)) {
				trialAvailable = REALMS_DATA_FETCHER.isTrialAvailable();
			}

			if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
				hasUnreadNews = REALMS_DATA_FETCHER.hasUnreadNews();
			}

			REALMS_DATA_FETCHER.markClean();
		}
	}

	private boolean getRealmsNotificationsEnabled() {
		return this.minecraft.options.realmsNotifications;
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
		if (hasUnreadNews) {
			RenderSystem.setShaderTexture(0, NEWS_ICON_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.pushPose();
			poseStack.scale(0.4F, 0.4F, 0.4F);
			GuiComponent.blit(poseStack, (int)((double)(n + 2 - p) * 2.5), (int)((double)o * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
			poseStack.popPose();
			p += 14;
		}

		if (k != 0) {
			RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, n - p, o - 6, 0.0F, 0.0F, 15, 25, 31, 25);
			p += 16;
		}

		if (trialAvailable) {
			RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int q = 0;
			if ((Util.getMillis() / 800L & 1L) == 1L) {
				q = 8;
			}

			GuiComponent.blit(poseStack, n + 4 - p, o + 4, 0.0F, (float)q, 8, 8, 8, 16);
		}
	}

	@Override
	public void removed() {
		REALMS_DATA_FETCHER.stop();
	}
}
