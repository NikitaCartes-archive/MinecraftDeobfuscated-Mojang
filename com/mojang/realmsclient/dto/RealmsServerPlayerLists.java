/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.ValueObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServerPlayerLists
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public List<RealmsServerPlayerList> servers;

    public static RealmsServerPlayerLists parse(String string) {
        RealmsServerPlayerLists realmsServerPlayerLists = new RealmsServerPlayerLists();
        realmsServerPlayerLists.servers = new ArrayList<RealmsServerPlayerList>();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
            if (jsonObject.get("lists").isJsonArray()) {
                JsonArray jsonArray = jsonObject.get("lists").getAsJsonArray();
                Iterator<JsonElement> iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    realmsServerPlayerLists.servers.add(RealmsServerPlayerList.parse(iterator.next().getAsJsonObject()));
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerPlayerLists: " + exception.getMessage());
        }
        return realmsServerPlayerLists;
    }
}

