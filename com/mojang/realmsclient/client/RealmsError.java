/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsError {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String errorMessage;
    private final int errorCode;

    private RealmsError(String string, int i) {
        this.errorMessage = string;
        this.errorCode = i;
    }

    @Nullable
    public static RealmsError parse(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return null;
        }
        try {
            JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
            String string2 = JsonUtils.getStringOr("errorMsg", jsonObject, "");
            int i = JsonUtils.getIntOr("errorCode", jsonObject, -1);
            return new RealmsError(string2, i);
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsError: {}", (Object)exception.getMessage());
            LOGGER.error("The error was: {}", (Object)string);
            return null;
        }
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}

