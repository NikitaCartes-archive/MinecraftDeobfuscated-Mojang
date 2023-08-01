package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsHttpException;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public interface RealmsError {
	Component NO_MESSAGE = Component.translatable("mco.errorMessage.noDetails");
	Logger LOGGER = LogUtils.getLogger();

	int errorCode();

	Component errorMessage();

	String logMessage();

	static RealmsError parse(int i, String string) {
		if (i == 429) {
			return RealmsError.CustomError.SERVICE_BUSY;
		} else if (Strings.isNullOrEmpty(string)) {
			return RealmsError.CustomError.noPayload(i);
		} else {
			try {
				JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
				String string2 = GsonHelper.getAsString(jsonObject, "reason", null);
				String string3 = GsonHelper.getAsString(jsonObject, "errorMsg", null);
				int j = GsonHelper.getAsInt(jsonObject, "errorCode", -1);
				if (string3 != null || string2 != null || j != -1) {
					return new RealmsError.ErrorWithJsonPayload(i, j != -1 ? j : i, string2, string3);
				}
			} catch (Exception var6) {
				LOGGER.error("Could not parse RealmsError", (Throwable)var6);
			}

			return new RealmsError.ErrorWithRawPayload(i, string);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record AuthenticationError(String message) implements RealmsError {
		public static final int ERROR_CODE = 401;

		@Override
		public int errorCode() {
			return 401;
		}

		@Override
		public Component errorMessage() {
			return Component.literal(this.message);
		}

		@Override
		public String logMessage() {
			return String.format(Locale.ROOT, "Realms authentication error with message '%s'", this.message);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record CustomError(int httpCode, @Nullable Component payload) implements RealmsError {
		public static final RealmsError.CustomError SERVICE_BUSY = new RealmsError.CustomError(429, Component.translatable("mco.errorMessage.serviceBusy"));
		public static final Component RETRY_MESSAGE = Component.translatable("mco.errorMessage.retry");

		public static RealmsError.CustomError unknownCompatibilityResponse(String string) {
			return new RealmsError.CustomError(500, Component.translatable("mco.errorMessage.realmsService.unknownCompatibility", string));
		}

		public static RealmsError.CustomError connectivityError(RealmsHttpException realmsHttpException) {
			return new RealmsError.CustomError(500, Component.translatable("mco.errorMessage.realmsService.connectivity", realmsHttpException.getMessage()));
		}

		public static RealmsError.CustomError retry(int i) {
			return new RealmsError.CustomError(i, RETRY_MESSAGE);
		}

		public static RealmsError.CustomError noPayload(int i) {
			return new RealmsError.CustomError(i, null);
		}

		@Override
		public int errorCode() {
			return this.httpCode;
		}

		@Override
		public Component errorMessage() {
			return this.payload != null ? this.payload : NO_MESSAGE;
		}

		@Override
		public String logMessage() {
			return this.payload != null
				? String.format(Locale.ROOT, "Realms service error (%d) with message '%s'", this.httpCode, this.payload.getString())
				: String.format(Locale.ROOT, "Realms service error (%d) with no payload", this.httpCode);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record ErrorWithJsonPayload(int httpCode, int code, @Nullable String reason, @Nullable String message) implements RealmsError {
		@Override
		public int errorCode() {
			return this.code;
		}

		@Override
		public Component errorMessage() {
			String string = "mco.errorMessage." + this.code;
			if (I18n.exists(string)) {
				return Component.translatable(string);
			} else {
				if (this.reason != null) {
					String string2 = "mco.errorReason." + this.reason;
					if (I18n.exists(string2)) {
						return Component.translatable(string2);
					}
				}

				return (Component)(this.message != null ? Component.literal(this.message) : NO_MESSAGE);
			}
		}

		@Override
		public String logMessage() {
			return String.format(Locale.ROOT, "Realms service error (%d/%d/%s) with message '%s'", this.httpCode, this.code, this.reason, this.message);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record ErrorWithRawPayload(int httpCode, String payload) implements RealmsError {
		@Override
		public int errorCode() {
			return this.httpCode;
		}

		@Override
		public Component errorMessage() {
			return Component.literal(this.payload);
		}

		@Override
		public String logMessage() {
			return String.format(Locale.ROOT, "Realms service error (%d) with raw payload '%s'", this.httpCode, this.payload);
		}
	}
}
