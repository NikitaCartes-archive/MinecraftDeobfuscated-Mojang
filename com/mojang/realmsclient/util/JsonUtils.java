/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class JsonUtils {
    public static String getStringOr(String string, JsonObject jsonObject, String string2) {
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement != null) {
            return jsonElement.isJsonNull() ? string2 : jsonElement.getAsString();
        }
        return string2;
    }

    public static int getIntOr(String string, JsonObject jsonObject, int i) {
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement != null) {
            return jsonElement.isJsonNull() ? i : jsonElement.getAsInt();
        }
        return i;
    }

    public static long getLongOr(String string, JsonObject jsonObject, long l) {
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement != null) {
            return jsonElement.isJsonNull() ? l : jsonElement.getAsLong();
        }
        return l;
    }

    public static boolean getBooleanOr(String string, JsonObject jsonObject, boolean bl) {
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement != null) {
            return jsonElement.isJsonNull() ? bl : jsonElement.getAsBoolean();
        }
        return bl;
    }

    public static Date getDateOr(String string, JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement != null) {
            return new Date(Long.parseLong(jsonElement.getAsString()));
        }
        return new Date();
    }
}

