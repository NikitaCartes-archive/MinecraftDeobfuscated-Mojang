package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServerPlayerList extends ValueObject {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final JsonParser JSON_PARSER = new JsonParser();
	public long serverId;
	public List<String> players;

	public static RealmsServerPlayerList parse(JsonObject jsonObject) {
		RealmsServerPlayerList realmsServerPlayerList = new RealmsServerPlayerList();

		try {
			realmsServerPlayerList.serverId = JsonUtils.getLongOr("serverId", jsonObject, -1L);
			String string = JsonUtils.getStringOr("playerList", jsonObject, null);
			if (string != null) {
				JsonElement jsonElement = JSON_PARSER.parse(string);
				if (jsonElement.isJsonArray()) {
					realmsServerPlayerList.players = parsePlayers(jsonElement.getAsJsonArray());
				} else {
					realmsServerPlayerList.players = Lists.<String>newArrayList();
				}
			} else {
				realmsServerPlayerList.players = Lists.<String>newArrayList();
			}
		} catch (Exception var4) {
			LOGGER.error("Could not parse RealmsServerPlayerList: " + var4.getMessage());
		}

		return realmsServerPlayerList;
	}

	private static List<String> parsePlayers(JsonArray jsonArray) {
		List<String> list = Lists.<String>newArrayList();

		for (JsonElement jsonElement : jsonArray) {
			try {
				list.add(jsonElement.getAsString());
			} catch (Exception var5) {
			}
		}

		return list;
	}
}
