package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RealmsScreen lastScreen;
	private final RealmsServer serverData;
	private final RealmsScreen mainScreen;
	private final int BUTTON_BACK_ID = 0;
	private final int BUTTON_DELETE_ID = 1;
	private final int BUTTON_SUBSCRIPTION_ID = 2;
	private final String subscriptionTitle;
	private final String subscriptionStartLabelText;
	private final String timeLeftLabelText;
	private final String daysLeftLabelText;
	private int daysLeft;
	private String startDate;
	private Subscription.SubscriptionType type;
	private final String PURCHASE_LINK = "https://aka.ms/ExtendJavaRealms";

	public RealmsSubscriptionInfoScreen(RealmsScreen realmsScreen, RealmsServer realmsServer, RealmsScreen realmsScreen2) {
		this.lastScreen = realmsScreen;
		this.serverData = realmsServer;
		this.mainScreen = realmsScreen2;
		this.subscriptionTitle = getLocalizedString("mco.configure.world.subscription.title");
		this.subscriptionStartLabelText = getLocalizedString("mco.configure.world.subscription.start");
		this.timeLeftLabelText = getLocalizedString("mco.configure.world.subscription.timeleft");
		this.daysLeftLabelText = getLocalizedString("mco.configure.world.subscription.recurring.daysleft");
	}

	@Override
	public void init() {
		this.getSubscription(this.serverData.id);
		Realms.narrateNow(this.subscriptionTitle, this.subscriptionStartLabelText, this.startDate, this.timeLeftLabelText, this.daysLeftPresentation(this.daysLeft));
		this.setKeyboardHandlerSendRepeatsToGui(true);
		this.buttonsAdd(
			new RealmsButton(2, this.width() / 2 - 100, RealmsConstants.row(6), getLocalizedString("mco.configure.world.subscription.extend")) {
				@Override
				public void onPress() {
					String string = "https://aka.ms/ExtendJavaRealms?subscriptionId="
						+ RealmsSubscriptionInfoScreen.this.serverData.remoteSubscriptionId
						+ "&profileId="
						+ Realms.getUUID();
					Realms.setClipboard(string);
					RealmsUtil.browseTo(string);
				}
			}
		);
		this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(12), getLocalizedString("gui.back")) {
			@Override
			public void onPress() {
				Realms.setScreen(RealmsSubscriptionInfoScreen.this.lastScreen);
			}
		});
		if (this.serverData.expired) {
			this.buttonsAdd(new RealmsButton(1, this.width() / 2 - 100, RealmsConstants.row(10), getLocalizedString("mco.configure.world.delete.button")) {
				@Override
				public void onPress() {
					String string = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line1");
					String string2 = RealmsScreen.getLocalizedString("mco.configure.world.delete.question.line2");
					Realms.setScreen(new RealmsLongConfirmationScreen(RealmsSubscriptionInfoScreen.this, RealmsLongConfirmationScreen.Type.Warning, string, string2, true, 1));
				}
			});
		}
	}

	private void getSubscription(long l) {
		RealmsClient realmsClient = RealmsClient.createRealmsClient();

		try {
			Subscription subscription = realmsClient.subscriptionFor(l);
			this.daysLeft = subscription.daysLeft;
			this.startDate = this.localPresentation(subscription.startDate);
			this.type = subscription.type;
		} catch (RealmsServiceException var5) {
			LOGGER.error("Couldn't get subscription");
			Realms.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
		} catch (IOException var6) {
			LOGGER.error("Couldn't parse response subscribing");
		}
	}

	@Override
	public void confirmResult(boolean bl, int i) {
		if (i == 1 && bl) {
			(new Thread("Realms-delete-realm") {
				public void run() {
					try {
						RealmsClient realmsClient = RealmsClient.createRealmsClient();
						realmsClient.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
					} catch (RealmsServiceException var2) {
						RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
						RealmsSubscriptionInfoScreen.LOGGER.error(var2);
					} catch (IOException var3) {
						RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world");
						var3.printStackTrace();
					}

					Realms.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen);
				}
			}).start();
		}

		Realms.setScreen(this);
	}

	private String localPresentation(long l) {
		Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
		calendar.setTimeInMillis(l);
		return DateFormat.getDateTimeInstance().format(calendar.getTime());
	}

	@Override
	public void removed() {
		this.setKeyboardHandlerSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			Realms.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		int k = this.width() / 2 - 100;
		this.drawCenteredString(this.subscriptionTitle, this.width() / 2, 17, 16777215);
		this.drawString(this.subscriptionStartLabelText, k, RealmsConstants.row(0), 10526880);
		this.drawString(this.startDate, k, RealmsConstants.row(1), 16777215);
		if (this.type == Subscription.SubscriptionType.NORMAL) {
			this.drawString(this.timeLeftLabelText, k, RealmsConstants.row(3), 10526880);
		} else if (this.type == Subscription.SubscriptionType.RECURRING) {
			this.drawString(this.daysLeftLabelText, k, RealmsConstants.row(3), 10526880);
		}

		this.drawString(this.daysLeftPresentation(this.daysLeft), k, RealmsConstants.row(4), 16777215);
		super.render(i, j, f);
	}

	private String daysLeftPresentation(int i) {
		if (i == -1 && this.serverData.expired) {
			return getLocalizedString("mco.configure.world.subscription.expired");
		} else if (i <= 1) {
			return getLocalizedString("mco.configure.world.subscription.less_than_a_day");
		} else {
			int j = i / 30;
			int k = i % 30;
			StringBuilder stringBuilder = new StringBuilder();
			if (j > 0) {
				stringBuilder.append(j).append(" ");
				if (j == 1) {
					stringBuilder.append(getLocalizedString("mco.configure.world.subscription.month").toLowerCase(Locale.ROOT));
				} else {
					stringBuilder.append(getLocalizedString("mco.configure.world.subscription.months").toLowerCase(Locale.ROOT));
				}
			}

			if (k > 0) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}

				stringBuilder.append(k).append(" ");
				if (k == 1) {
					stringBuilder.append(getLocalizedString("mco.configure.world.subscription.day").toLowerCase(Locale.ROOT));
				} else {
					stringBuilder.append(getLocalizedString("mco.configure.world.subscription.days").toLowerCase(Locale.ROOT));
				}
			}

			return stringBuilder.toString();
		}
	}
}
