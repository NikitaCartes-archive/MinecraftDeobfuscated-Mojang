/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServerPlayerList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final JsonParser JSON_PARSER = new JsonParser();
    public long serverId;
    public List<String> players;

    public static RealmsServerPlayerList parse(JsonObject jsonObject) {
        RealmsServerPlayerList realmsServerPlayerList = new RealmsServerPlayerList();
        try {
            JsonElement jsonElement;
            realmsServerPlayerList.serverId = JsonUtils.getLongOr("serverId", jsonObject, -1L);
            String string = JsonUtils.getStringOr("playerList", jsonObject, null);
            realmsServerPlayerList.players = string != null ? ((jsonElement = JSON_PARSER.parse(string)).isJsonArray() ? RealmsServerPlayerList.parsePlayers(jsonElement.getAsJsonArray()) : Lists.newArrayList()) : Lists.newArrayList();
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerPlayerList: {}", (Object)exception.getMessage());
        }
        return realmsServerPlayerList;
    }

    private static List<String> parsePlayers(JsonArray jsonArray) {
        ArrayList<String> list = Lists.newArrayList();
        for (JsonElement jsonElement : jsonArray) {
            try {
                list.add(jsonElement.getAsString());
            } catch (Exception exception) {}
        }
        return list;
    }
}

