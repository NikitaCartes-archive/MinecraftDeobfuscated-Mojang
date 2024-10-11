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
public class RealmsNews extends ValueObject {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	public String newsLink;

	public static RealmsNews parse(String string) {
		RealmsNews realmsNews = new RealmsNews();

		try {
			JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
			realmsNews.newsLink = JsonUtils.getStringOr("newsLink", jsonObject, null);
		} catch (Exception var3) {
			LOGGER.error("Could not parse RealmsNews: {}", var3.getMessage());
		}

		return realmsNews;
	}
}
