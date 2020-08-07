package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class RealmsWorldOptions extends ValueObject {
	public Boolean pvp;
	public Boolean spawnAnimals;
	public Boolean spawnMonsters;
	public Boolean spawnNPCs;
	public Integer spawnProtection;
	public Boolean commandBlocks;
	public Boolean forceGameMode;
	public Integer difficulty;
	public Integer gameMode;
	public String slotName;
	public long templateId;
	public String templateImage;
	public boolean adventureMap;
	public boolean empty;
	private static final String DEFAULT_TEMPLATE_IMAGE = null;

	public RealmsWorldOptions(
		Boolean boolean_,
		Boolean boolean2,
		Boolean boolean3,
		Boolean boolean4,
		Integer integer,
		Boolean boolean5,
		Integer integer2,
		Integer integer3,
		Boolean boolean6,
		String string
	) {
		this.pvp = boolean_;
		this.spawnAnimals = boolean2;
		this.spawnMonsters = boolean3;
		this.spawnNPCs = boolean4;
		this.spawnProtection = integer;
		this.commandBlocks = boolean5;
		this.difficulty = integer2;
		this.gameMode = integer3;
		this.forceGameMode = boolean6;
		this.slotName = string;
	}

	public static RealmsWorldOptions createDefaults() {
		return new RealmsWorldOptions(true, true, true, true, 0, false, 2, 0, false, "");
	}

	public static RealmsWorldOptions createEmptyDefaults() {
		RealmsWorldOptions realmsWorldOptions = createDefaults();
		realmsWorldOptions.setEmpty(true);
		return realmsWorldOptions;
	}

	public void setEmpty(boolean bl) {
		this.empty = bl;
	}

	public static RealmsWorldOptions parse(JsonObject jsonObject) {
		RealmsWorldOptions realmsWorldOptions = new RealmsWorldOptions(
			JsonUtils.getBooleanOr("pvp", jsonObject, true),
			JsonUtils.getBooleanOr("spawnAnimals", jsonObject, true),
			JsonUtils.getBooleanOr("spawnMonsters", jsonObject, true),
			JsonUtils.getBooleanOr("spawnNPCs", jsonObject, true),
			JsonUtils.getIntOr("spawnProtection", jsonObject, 0),
			JsonUtils.getBooleanOr("commandBlocks", jsonObject, false),
			JsonUtils.getIntOr("difficulty", jsonObject, 2),
			JsonUtils.getIntOr("gameMode", jsonObject, 0),
			JsonUtils.getBooleanOr("forceGameMode", jsonObject, false),
			JsonUtils.getStringOr("slotName", jsonObject, "")
		);
		realmsWorldOptions.templateId = JsonUtils.getLongOr("worldTemplateId", jsonObject, -1L);
		realmsWorldOptions.templateImage = JsonUtils.getStringOr("worldTemplateImage", jsonObject, DEFAULT_TEMPLATE_IMAGE);
		realmsWorldOptions.adventureMap = JsonUtils.getBooleanOr("adventureMap", jsonObject, false);
		return realmsWorldOptions;
	}

	public String getSlotName(int i) {
		if (this.slotName != null && !this.slotName.isEmpty()) {
			return this.slotName;
		} else {
			return this.empty ? I18n.get("mco.configure.world.slot.empty") : this.getDefaultSlotName(i);
		}
	}

	public String getDefaultSlotName(int i) {
		return I18n.get("mco.configure.world.slot", i);
	}

	public String toJson() {
		JsonObject jsonObject = new JsonObject();
		if (!this.pvp) {
			jsonObject.addProperty("pvp", this.pvp);
		}

		if (!this.spawnAnimals) {
			jsonObject.addProperty("spawnAnimals", this.spawnAnimals);
		}

		if (!this.spawnMonsters) {
			jsonObject.addProperty("spawnMonsters", this.spawnMonsters);
		}

		if (!this.spawnNPCs) {
			jsonObject.addProperty("spawnNPCs", this.spawnNPCs);
		}

		if (this.spawnProtection != 0) {
			jsonObject.addProperty("spawnProtection", this.spawnProtection);
		}

		if (this.commandBlocks) {
			jsonObject.addProperty("commandBlocks", this.commandBlocks);
		}

		if (this.difficulty != 2) {
			jsonObject.addProperty("difficulty", this.difficulty);
		}

		if (this.gameMode != 0) {
			jsonObject.addProperty("gameMode", this.gameMode);
		}

		if (this.forceGameMode) {
			jsonObject.addProperty("forceGameMode", this.forceGameMode);
		}

		if (!Objects.equals(this.slotName, "")) {
			jsonObject.addProperty("slotName", this.slotName);
		}

		return jsonObject.toString();
	}

	public RealmsWorldOptions clone() {
		return new RealmsWorldOptions(
			this.pvp,
			this.spawnAnimals,
			this.spawnMonsters,
			this.spawnNPCs,
			this.spawnProtection,
			this.commandBlocks,
			this.difficulty,
			this.gameMode,
			this.forceGameMode,
			this.slotName
		);
	}
}
