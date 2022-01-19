package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldTemplate extends ValueObject {
	private static final Logger LOGGER = LogUtils.getLogger();
	public String id = "";
	public String name = "";
	public String version = "";
	public String author = "";
	public String link = "";
	@Nullable
	public String image;
	public String trailer = "";
	public String recommendedPlayers = "";
	public WorldTemplate.WorldTemplateType type = WorldTemplate.WorldTemplateType.WORLD_TEMPLATE;

	public static WorldTemplate parse(JsonObject jsonObject) {
		WorldTemplate worldTemplate = new WorldTemplate();

		try {
			worldTemplate.id = JsonUtils.getStringOr("id", jsonObject, "");
			worldTemplate.name = JsonUtils.getStringOr("name", jsonObject, "");
			worldTemplate.version = JsonUtils.getStringOr("version", jsonObject, "");
			worldTemplate.author = JsonUtils.getStringOr("author", jsonObject, "");
			worldTemplate.link = JsonUtils.getStringOr("link", jsonObject, "");
			worldTemplate.image = JsonUtils.getStringOr("image", jsonObject, null);
			worldTemplate.trailer = JsonUtils.getStringOr("trailer", jsonObject, "");
			worldTemplate.recommendedPlayers = JsonUtils.getStringOr("recommendedPlayers", jsonObject, "");
			worldTemplate.type = WorldTemplate.WorldTemplateType.valueOf(
				JsonUtils.getStringOr("type", jsonObject, WorldTemplate.WorldTemplateType.WORLD_TEMPLATE.name())
			);
		} catch (Exception var3) {
			LOGGER.error("Could not parse WorldTemplate: {}", var3.getMessage());
		}

		return worldTemplate;
	}

	@Environment(EnvType.CLIENT)
	public static enum WorldTemplateType {
		WORLD_TEMPLATE,
		MINIGAME,
		ADVENTUREMAP,
		EXPERIENCE,
		INSPIRATION;
	}
}
