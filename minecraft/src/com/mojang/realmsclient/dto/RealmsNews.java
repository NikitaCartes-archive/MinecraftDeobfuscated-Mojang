package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsNews extends ValueObject {
	private static final Logger LOGGER = LogUtils.getLogger();
	public String newsLink;

	public static RealmsNews parse(String string) {
		RealmsNews realmsNews = new RealmsNews();

		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			realmsNews.newsLink = JsonUtils.getStringOr("newsLink", jsonObject, null);
		} catch (Exception var4) {
			LOGGER.error("Could not parse RealmsNews: {}", var4.getMessage());
		}

		return realmsNews;
	}
}
