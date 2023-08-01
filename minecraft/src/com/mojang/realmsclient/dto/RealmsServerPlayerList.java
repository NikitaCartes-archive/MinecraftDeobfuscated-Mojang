package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import com.mojang.util.UndashedUuid;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServerPlayerList extends ValueObject {
	private static final Logger LOGGER = LogUtils.getLogger();
	public long serverId;
	public List<UUID> players;

	public static RealmsServerPlayerList parse(JsonObject jsonObject) {
		RealmsServerPlayerList realmsServerPlayerList = new RealmsServerPlayerList();

		try {
			realmsServerPlayerList.serverId = JsonUtils.getLongOr("serverId", jsonObject, -1L);
			String string = JsonUtils.getStringOr("playerList", jsonObject, null);
			if (string != null) {
				JsonElement jsonElement = JsonParser.parseString(string);
				if (jsonElement.isJsonArray()) {
					realmsServerPlayerList.players = parsePlayers(jsonElement.getAsJsonArray());
				} else {
					realmsServerPlayerList.players = Lists.<UUID>newArrayList();
				}
			} else {
				realmsServerPlayerList.players = Lists.<UUID>newArrayList();
			}
		} catch (Exception var4) {
			LOGGER.error("Could not parse RealmsServerPlayerList: {}", var4.getMessage());
		}

		return realmsServerPlayerList;
	}

	private static List<UUID> parsePlayers(JsonArray jsonArray) {
		List<UUID> list = Lists.<UUID>newArrayList();

		for (JsonElement jsonElement : jsonArray) {
			try {
				list.add(UndashedUuid.fromStringLenient(jsonElement.getAsString()));
			} catch (Exception var5) {
			}
		}

		return list;
	}
}
