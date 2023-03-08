package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.CommonLinks;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final Component SUBSCRIPTION_TITLE = Component.translatable("mco.configure.world.subscription.title");
	private static final Component SUBSCRIPTION_START_LABEL = Component.translatable("mco.configure.world.subscription.start");
	private static final Component TIME_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.timeleft");
	private static final Component DAYS_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.recurring.daysleft");
	private static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.configure.world.subscription.expired");
	private static final Component SUBSCRIPTION_LESS_THAN_A_DAY_TEXT = Component.translatable("mco.configure.world.subscription.less_than_a_day");
	private static final Component MONTH_SUFFIX = Component.translatable("mco.configure.world.subscription.month");
	private static final Component MONTHS_SUFFIX = Component.translatable("mco.configure.world.subscription.months");
	private static final Component DAY_SUFFIX = Component.translatable("mco.configure.world.subscription.day");
	private static final Component DAYS_SUFFIX = Component.translatable("mco.configure.world.subscription.days");
	private static final Component UNKNOWN = Component.translatable("mco.configure.world.subscription.unknown");
	private final Screen lastScreen;
	final RealmsServer serverData;
	final Screen mainScreen;
	private Component daysLeft = UNKNOWN;
	private Component startDate = UNKNOWN;
	@Nullable
	private Subscription.SubscriptionType type;

	public RealmsSubscriptionInfoScreen(Screen screen, RealmsServer realmsServer, Screen screen2) {
		super(GameNarrator.NO_TITLE);
		this.lastScreen = screen;
		this.serverData = realmsServer;
		this.mainScreen = screen2;
	}

	@Override
	public void init() {
		this.getSubscription(this.serverData.id);
		this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.subscription.extend"), button -> {
			String string = CommonLinks.extendRealms(this.serverData.remoteSubscriptionId, this.minecraft.getUser().getUuid());
			this.minecraft.keyboardHandler.setClipboard(string);
			Util.getPlatform().openUri(string);
		}).bounds(this.width / 2 - 100, row(6), 200, 20).build());
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, row(12), 200, 20).build()
		);
		if (this.serverData.expired) {
			this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.delete.button"), button -> {
				Component component = Component.translatable("mco.configure.world.delete.question.line1");
				Component component2 = Component.translatable("mco.configure.world.delete.question.line2");
				this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::deleteRealm, RealmsLongConfirmationScreen.Type.Warning, component, component2, true));
			}).bounds(this.width / 2 - 100, row(10), 200, 20).build());
		}
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinLines(SUBSCRIPTION_TITLE, SUBSCRIPTION_START_LABEL, this.startDate, TIME_LEFT_LABEL, this.daysLeft);
	}

	private void deleteRealm(boolean bl) {
		if (bl) {
			(new Thread("Realms-delete-realm") {
					public void run() {
						try {
							RealmsClient realmsClient = RealmsClient.create();
							realmsClient.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
						} catch (RealmsServiceException var2) {
							RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world", (Throwable)var2);
						}

						RealmsSubscriptionInfoScreen.this.minecraft
							.execute(() -> RealmsSubscriptionInfoScreen.this.minecraft.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen));
					}
				})
				.start();
		}

		this.minecraft.setScreen(this);
	}

	private void getSubscription(long l) {
		RealmsClient realmsClient = RealmsClient.create();

		try {
			Subscription subscription = realmsClient.subscriptionFor(l);
			this.daysLeft = this.daysLeftPresentation(subscription.daysLeft);
			this.startDate = localPresentation(subscription.startDate);
			this.type = subscription.type;
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't get subscription");
			this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
		}
	}

	private static Component localPresentation(long l) {
		Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
		calendar.setTimeInMillis(l);
		return Component.literal(DateFormat.getDateTimeInstance().format(calendar.getTime()));
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		int k = this.width / 2 - 100;
		drawCenteredString(poseStack, this.font, SUBSCRIPTION_TITLE, this.width / 2, 17, 16777215);
		this.font.draw(poseStack, SUBSCRIPTION_START_LABEL, (float)k, (float)row(0), 10526880);
		this.font.draw(poseStack, this.startDate, (float)k, (float)row(1), 16777215);
		if (this.type == Subscription.SubscriptionType.NORMAL) {
			this.font.draw(poseStack, TIME_LEFT_LABEL, (float)k, (float)row(3), 10526880);
		} else if (this.type == Subscription.SubscriptionType.RECURRING) {
			this.font.draw(poseStack, DAYS_LEFT_LABEL, (float)k, (float)row(3), 10526880);
		}

		this.font.draw(poseStack, this.daysLeft, (float)k, (float)row(4), 16777215);
		super.render(poseStack, i, j, f);
	}

	private Component daysLeftPresentation(int i) {
		if (i < 0 && this.serverData.expired) {
			return SUBSCRIPTION_EXPIRED_TEXT;
		} else if (i <= 1) {
			return SUBSCRIPTION_LESS_THAN_A_DAY_TEXT;
		} else {
			int j = i / 30;
			int k = i % 30;
			MutableComponent mutableComponent = Component.empty();
			if (j > 0) {
				mutableComponent.append(Integer.toString(j)).append(CommonComponents.SPACE);
				if (j == 1) {
					mutableComponent.append(MONTH_SUFFIX);
				} else {
					mutableComponent.append(MONTHS_SUFFIX);
				}
			}

			if (k > 0) {
				if (j > 0) {
					mutableComponent.append(", ");
				}

				mutableComponent.append(Integer.toString(k)).append(CommonComponents.SPACE);
				if (k == 1) {
					mutableComponent.append(DAY_SUFFIX);
				} else {
					mutableComponent.append(DAYS_SUFFIX);
				}
			}

			return mutableComponent;
		}
	}
}
