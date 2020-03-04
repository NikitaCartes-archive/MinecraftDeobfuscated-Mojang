package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class UploadInfo extends ValueObject {
	private static final Logger LOGGER = LogManager.getLogger();
	private boolean worldClosed;
	private String token = "";
	private String uploadEndpoint = "";
	private int port;

	public static UploadInfo parse(String string) {
		UploadInfo uploadInfo = new UploadInfo();

		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			uploadInfo.worldClosed = JsonUtils.getBooleanOr("worldClosed", jsonObject, false);
			uploadInfo.token = JsonUtils.getStringOr("token", jsonObject, null);
			uploadInfo.uploadEndpoint = JsonUtils.getStringOr("uploadEndpoint", jsonObject, null);
			uploadInfo.port = JsonUtils.getIntOr("port", jsonObject, 8080);
		} catch (Exception var4) {
			LOGGER.error("Could not parse UploadInfo: " + var4.getMessage());
		}

		return uploadInfo;
	}

	public String getToken() {
		return this.token;
	}

	public String getUploadEndpoint() {
		return this.uploadEndpoint;
	}

	public boolean isWorldClosed() {
		return this.worldClosed;
	}

	public void setToken(String string) {
		this.token = string;
	}

	public int getPort() {
		return this.port;
	}
}
