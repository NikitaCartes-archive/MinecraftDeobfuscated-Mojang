/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsText {
    private static final String TRANSLATION_KEY = "translationKey";
    private static final String ARGS = "args";
    private final String translationKey;
    @Nullable
    private final Object[] args;

    private RealmsText(String string, @Nullable Object[] objects) {
        this.translationKey = string;
        this.args = objects;
    }

    public Component createComponent(Component component) {
        if (!I18n.exists(this.translationKey)) {
            return component;
        }
        if (this.args == null) {
            return Component.translatable(this.translationKey);
        }
        return Component.translatable(this.translationKey, this.args);
    }

    public static RealmsText parse(JsonObject jsonObject) {
        String[] strings;
        String string = JsonUtils.getRequiredString(TRANSLATION_KEY, jsonObject);
        JsonElement jsonElement = jsonObject.get(ARGS);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            strings = null;
        } else {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            strings = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); ++i) {
                strings[i] = jsonArray.get(i).getAsString();
            }
        }
        return new RealmsText(string, strings);
    }
}

