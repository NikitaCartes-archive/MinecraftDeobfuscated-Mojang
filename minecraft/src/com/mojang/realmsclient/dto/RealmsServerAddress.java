package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServerAddress extends ValueObject {
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
		} catch (Exception var4) {
			LOGGER.error("Could not parse RealmsServerAddress: {}", var4.getMessage());
		}

		return realmsServerAddress;
	}
}
