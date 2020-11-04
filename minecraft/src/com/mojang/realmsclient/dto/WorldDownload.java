package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldDownload extends ValueObject {
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
		} catch (Exception var5) {
			LOGGER.error("Could not parse WorldDownload: {}", var5.getMessage());
		}

		return worldDownload;
	}
}
