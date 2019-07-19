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
public class RealmsServerAddress
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String address;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static RealmsServerAddress parse(String string) {
        JsonParser jsonParser = new JsonParser();
        RealmsServerAddress realmsServerAddress = new RealmsServerAddress();
        try {
            JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
            realmsServerAddress.address = JsonUtils.getStringOr("address", jsonObject, null);
            realmsServerAddress.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonObject, null);
            realmsServerAddress.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonObject, null);
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerAddress: " + exception.getMessage());
        }
        return realmsServerAddress;
    }
}

