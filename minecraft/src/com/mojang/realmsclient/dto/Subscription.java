package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Subscription extends ValueObject {
	private static final Logger LOGGER = LogManager.getLogger();
	public long startDate;
	public int daysLeft;
	public Subscription.SubscriptionType type = Subscription.SubscriptionType.NORMAL;

	public static Subscription parse(String string) {
		Subscription subscription = new Subscription();

		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			subscription.startDate = JsonUtils.getLongOr("startDate", jsonObject, 0L);
			subscription.daysLeft = JsonUtils.getIntOr("daysLeft", jsonObject, 0);
			subscription.type = typeFrom(JsonUtils.getStringOr("subscriptionType", jsonObject, Subscription.SubscriptionType.NORMAL.name()));
		} catch (Exception var4) {
			LOGGER.error("Could not parse Subscription: " + var4.getMessage());
		}

		return subscription;
	}

	private static Subscription.SubscriptionType typeFrom(String string) {
		try {
			return Subscription.SubscriptionType.valueOf(string);
		} catch (Exception var2) {
			return Subscription.SubscriptionType.NORMAL;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum SubscriptionType {
		NORMAL,
		RECURRING;
	}
}
