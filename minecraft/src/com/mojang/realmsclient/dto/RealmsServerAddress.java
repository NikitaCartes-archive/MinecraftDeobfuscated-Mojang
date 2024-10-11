package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServerAddress extends ValueObject {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	public String address;
	@Nullable
	public String resourcePackUrl;
	@Nullable
	public String resourcePackHash;

	public static RealmsServerAddress parse(String string) {
		RealmsServerAddress realmsServerAddress = new RealmsServerAddress();

		try {
			JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
			realmsServerAddress.address = JsonUtils.getStringOr("address", jsonObject, null);
			realmsServerAddress.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonObject, null);
			realmsServerAddress.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonObject, null);
		} catch (Exception var3) {
			LOGGER.error("Could not parse RealmsServerAddress: {}", var3.getMessage());
		}

		return realmsServerAddress;
	}
}
