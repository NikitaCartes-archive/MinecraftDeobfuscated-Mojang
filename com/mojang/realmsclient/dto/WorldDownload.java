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
public class WorldDownload
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String downloadLink;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static WorldDownload parse(String string) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
        WorldDownload worldDownload = new WorldDownload();
        try {
            worldDownload.downloadLink = JsonUtils.getStringOr("downloadLink", jsonObject, "");
            worldDownload.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonObject, "");
            worldDownload.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonObject, "");
        } catch (Exception exception) {
            LOGGER.error("Could not parse WorldDownload: " + exception.getMessage());
        }
        return worldDownload;
    }
}

