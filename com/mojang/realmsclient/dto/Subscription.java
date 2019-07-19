/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Subscription
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public long startDate;
    public int daysLeft;
    public SubscriptionType type = SubscriptionType.NORMAL;

    public static Subscription parse(String string) {
        Subscription subscription = new Subscription();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
            subscription.startDate = JsonUtils.getLongOr("startDate", jsonObject, 0L);
            subscription.daysLeft = JsonUtils.getIntOr("daysLeft", jsonObject, 0);
            subscription.type = Subscription.typeFrom(JsonUtils.getStringOr("subscriptionType", jsonObject, SubscriptionType.NORMAL.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse Subscription: " + exception.getMessage());
        }
        return subscription;
    }

    private static SubscriptionType typeFrom(String string) {
        try {
            return SubscriptionType.valueOf(string);
        } catch (Exception exception) {
            return SubscriptionType.NORMAL;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SubscriptionType {
        NORMAL,
        RECURRING;

    }
}

