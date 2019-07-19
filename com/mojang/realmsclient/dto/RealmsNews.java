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
public class RealmsNews
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String newsLink;

    public static RealmsNews parse(String string) {
        RealmsNews realmsNews = new RealmsNews();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
            realmsNews.newsLink = JsonUtils.getStringOr("newsLink", jsonObject, null);
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsNews: " + exception.getMessage());
        }
        return realmsNews;
    }
}

