package com.mojang.realmsclient.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsError {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String errorMessage;
	private final int errorCode;

	private RealmsError(String string, int i) {
		this.errorMessage = string;
		this.errorCode = i;
	}

	public static RealmsError create(String string) {
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			String string2 = JsonUtils.getStringOr("errorMsg", jsonObject, "");
			int i = JsonUtils.getIntOr("errorCode", jsonObject, -1);
			return new RealmsError(string2, i);
		} catch (Exception var5) {
			LOGGER.error("Could not parse RealmsError: " + var5.getMessage());
			LOGGER.error("The error was: " + string);
			return new RealmsError("Failed to parse response from server", -1);
		}
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public int getErrorCode() {
		return this.errorCode;
	}
}
