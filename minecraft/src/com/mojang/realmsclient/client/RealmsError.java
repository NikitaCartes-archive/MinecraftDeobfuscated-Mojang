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
	private String errorMessage;
	private int errorCode;

	public RealmsError(String string) {
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			this.errorMessage = JsonUtils.getStringOr("errorMsg", jsonObject, "");
			this.errorCode = JsonUtils.getIntOr("errorCode", jsonObject, -1);
		} catch (Exception var4) {
			LOGGER.error("Could not parse RealmsError: " + var4.getMessage());
			LOGGER.error("The error was: " + string);
		}
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public int getErrorCode() {
		return this.errorCode;
	}
}
