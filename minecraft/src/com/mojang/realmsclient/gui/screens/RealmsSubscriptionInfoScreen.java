package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private final RealmsServer serverData;
	private final Screen mainScreen;
	private final String subscriptionTitle;
	private final String subscriptionStartLabelText;
	private final String timeLeftLabelText;
	private final String daysLeftLabelText;
	private int daysLeft;
	private String startDate;
	private Subscription.SubscriptionType type;

	public RealmsSubscriptionInfoScreen(Screen screen, RealmsServer realmsServer, Screen screen2) {
		this.lastScreen = screen;
		this.serverData = realmsServer;
		this.mainScreen = screen2;
		this.subscriptionTitle = I18n.get("mco.configure.world.subscription.title");
		this.subscriptionStartLabelText = I18n.get("mco.configure.world.subscription.start");
		this.timeLeftLabelText = I18n.get("mco.configure.world.subscription.timeleft");
		this.daysLeftLabelText = I18n.get("mco.configure.world.subscription.recurring.daysleft");
	}

	@Override
	public void init() {
		this.getSubscription(this.serverData.id);
		NarrationHelper.now(this.subscriptionTitle, this.subscriptionStartLabelText, this.startDate, this.timeLeftLabelText, this.daysLeftPresentation(this.daysLeft));
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.addButton(
			new Button(
				this.width / 2 - 100,
				row(6),
				200,
				20,
				I18n.get("mco.configure.world.subscription.extend"),
				button -> {
					String string = "https://aka.ms/ExtendJavaRealms?subscriptionId="
						+ this.serverData.remoteSubscriptionId
						+ "&profileId="
						+ this.minecraft.getUser().getUuid();
					this.minecraft.keyboardHandler.setClipboard(string);
					Util.getPlatform().openUri(string);
				}
			)
		);
		this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, I18n.get("gui.back"), button -> this.minecraft.setScreen(this.lastScreen)));
		if (this.serverData.expired) {
			this.addButton(new Button(this.width / 2 - 100, row(10), 200, 20, I18n.get("mco.configure.world.delete.button"), button -> {
				String string = I18n.get("mco.configure.world.delete.question.line1");
				String string2 = I18n.get("mco.configure.world.delete.question.line2");
				this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::deleteRealm, RealmsLongConfirmationScreen.Type.Warning, string, string2, true));
			}));
		}
	}

	private void deleteRealm(boolean bl) {
		if (bl) {
			(new Thread("Realms-delete-realm") {
					public void run() {
						try {
							RealmsClient realmsClient = RealmsClient.create();
							realmsClient.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
						} catch (RealmsServiceException var2) {
							RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
							RealmsSubscriptionInfoScreen.LOGGER.error(var2);
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
			this.daysLeft = subscription.daysLeft;
			this.startDate = this.localPresentation(subscription.startDate);
			this.type = subscription.type;
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't get subscription");
			this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
		}
	}

	private String localPresentation(long l) {
		Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
		calendar.setTimeInMillis(l);
		return DateFormat.getDateTimeInstance().format(calendar.getTime());
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
	public void render(int i, int j, float f) {
		this.renderBackground();
		int k = this.width / 2 - 100;
		this.drawCenteredString(this.font, this.subscriptionTitle, this.width / 2, 17, 16777215);
		this.font.draw(this.subscriptionStartLabelText, (float)k, (float)row(0), 10526880);
		this.font.draw(this.startDate, (float)k, (float)row(1), 16777215);
		if (this.type == Subscription.SubscriptionType.NORMAL) {
			this.font.draw(this.timeLeftLabelText, (float)k, (float)row(3), 10526880);
		} else if (this.type == Subscription.SubscriptionType.RECURRING) {
			this.font.draw(this.daysLeftLabelText, (float)k, (float)row(3), 10526880);
		}

		this.font.draw(this.daysLeftPresentation(this.daysLeft), (float)k, (float)row(4), 16777215);
		super.render(i, j, f);
	}

	private String daysLeftPresentation(int i) {
		if (i == -1 && this.serverData.expired) {
			return I18n.get("mco.configure.world.subscription.expired");
		} else if (i <= 1) {
			return I18n.get("mco.configure.world.subscription.less_than_a_day");
		} else {
			int j = i / 30;
			int k = i % 30;
			StringBuilder stringBuilder = new StringBuilder();
			if (j > 0) {
				stringBuilder.append(j).append(" ");
				if (j == 1) {
					stringBuilder.append(I18n.get("mco.configure.world.subscription.month").toLowerCase(Locale.ROOT));
				} else {
					stringBuilder.append(I18n.get("mco.configure.world.subscription.months").toLowerCase(Locale.ROOT));
				}
			}

			if (k > 0) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}

				stringBuilder.append(k).append(" ");
				if (k == 1) {
					stringBuilder.append(I18n.get("mco.configure.world.subscription.day").toLowerCase(Locale.ROOT));
				} else {
					stringBuilder.append(I18n.get("mco.configure.world.subscription.days").toLowerCase(Locale.ROOT));
				}
			}

			return stringBuilder.toString();
		}
	}
}
