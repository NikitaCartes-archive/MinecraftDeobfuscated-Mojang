package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
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

	@Nullable
	public static RealmsError parse(String string) {
		if (Strings.isNullOrEmpty(string)) {
			return null;
		} else {
			try {
				JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
				String string2 = JsonUtils.getStringOr("errorMsg", jsonObject, "");
				int i = JsonUtils.getIntOr("errorCode", jsonObject, -1);
				return new RealmsError(string2, i);
			} catch (Exception var4) {
				LOGGER.error("Could not parse RealmsError: {}", var4.getMessage());
				LOGGER.error("The error was: {}", string);
				return null;
			}
		}
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public int getErrorCode() {
		return this.errorCode;
	}
}
