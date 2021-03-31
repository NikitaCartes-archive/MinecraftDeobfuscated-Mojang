package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class RealmsWorldOptions extends ValueObject {
	public final boolean pvp;
	public final boolean spawnAnimals;
	public final boolean spawnMonsters;
	public final boolean spawnNPCs;
	public final int spawnProtection;
	public final boolean commandBlocks;
	public final boolean forceGameMode;
	public final int difficulty;
	public final int gameMode;
	@Nullable
	private final String slotName;
	public long templateId;
	@Nullable
	public String templateImage;
	public boolean empty;
	private static final boolean DEFAULT_FORCE_GAME_MODE = false;
	private static final boolean DEFAULT_PVP = true;
	private static final boolean DEFAULT_SPAWN_ANIMALS = true;
	private static final boolean DEFAULT_SPAWN_MONSTERS = true;
	private static final boolean DEFAULT_SPAWN_NPCS = true;
	private static final int DEFAULT_SPAWN_PROTECTION = 0;
	private static final boolean DEFAULT_COMMAND_BLOCKS = false;
	private static final int DEFAULT_DIFFICULTY = 2;
	private static final int DEFAULT_GAME_MODE = 0;
	private static final String DEFAULT_SLOT_NAME = "";
	private static final long DEFAULT_TEMPLATE_ID = -1L;
	private static final String DEFAULT_TEMPLATE_IMAGE = null;

	public RealmsWorldOptions(boolean bl, boolean bl2, boolean bl3, boolean bl4, int i, boolean bl5, int j, int k, boolean bl6, @Nullable String string) {
		this.pvp = bl;
		this.spawnAnimals = bl2;
		this.spawnMonsters = bl3;
		this.spawnNPCs = bl4;
		this.spawnProtection = i;
		this.commandBlocks = bl5;
		this.difficulty = j;
		this.gameMode = k;
		this.forceGameMode = bl6;
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
