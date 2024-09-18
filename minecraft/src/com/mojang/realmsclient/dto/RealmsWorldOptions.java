package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;

@Environment(EnvType.CLIENT)
public class RealmsWorldOptions extends ValueObject {
	public final boolean pvp;
	public final boolean spawnMonsters;
	public final int spawnProtection;
	public final boolean commandBlocks;
	public final boolean forceGameMode;
	public final int difficulty;
	public final int gameMode;
	public final boolean hardcore;
	private final String slotName;
	public final String version;
	public final RealmsServer.Compatibility compatibility;
	public long templateId;
	@Nullable
	public String templateImage;
	public boolean empty;
	private static final boolean DEFAULT_FORCE_GAME_MODE = false;
	private static final boolean DEFAULT_PVP = true;
	private static final boolean DEFAULT_SPAWN_MONSTERS = true;
	private static final int DEFAULT_SPAWN_PROTECTION = 0;
	private static final boolean DEFAULT_COMMAND_BLOCKS = false;
	private static final int DEFAULT_DIFFICULTY = 2;
	private static final int DEFAULT_GAME_MODE = 0;
	private static final boolean DEFAULT_HARDCORE_MODE = false;
	private static final String DEFAULT_SLOT_NAME = "";
	private static final String DEFAULT_VERSION = "";
	private static final RealmsServer.Compatibility DEFAULT_COMPATIBILITY = RealmsServer.Compatibility.UNVERIFIABLE;
	private static final long DEFAULT_TEMPLATE_ID = -1L;
	private static final String DEFAULT_TEMPLATE_IMAGE = null;

	public RealmsWorldOptions(
		boolean bl, boolean bl2, int i, boolean bl3, int j, int k, boolean bl4, boolean bl5, String string, String string2, RealmsServer.Compatibility compatibility
	) {
		this.pvp = bl;
		this.spawnMonsters = bl2;
		this.spawnProtection = i;
		this.commandBlocks = bl3;
		this.difficulty = j;
		this.gameMode = k;
		this.hardcore = bl4;
		this.forceGameMode = bl5;
		this.slotName = string;
		this.version = string2;
		this.compatibility = compatibility;
	}

	public static RealmsWorldOptions createDefaults() {
		return new RealmsWorldOptions(true, true, 0, false, 2, 0, false, false, "", "", DEFAULT_COMPATIBILITY);
	}

	public static RealmsWorldOptions createDefaultsWith(GameType gameType, Difficulty difficulty, boolean bl, String string, String string2) {
		return new RealmsWorldOptions(true, true, 0, false, difficulty.getId(), gameType.getId(), bl, false, string2, string, DEFAULT_COMPATIBILITY);
	}

	public static RealmsWorldOptions createFromSettings(LevelSettings levelSettings, String string) {
		return createDefaultsWith(levelSettings.gameType(), levelSettings.difficulty(), levelSettings.hardcore(), string, levelSettings.levelName());
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
			JsonUtils.getBooleanOr("spawnMonsters", jsonObject, true),
			JsonUtils.getIntOr("spawnProtection", jsonObject, 0),
			JsonUtils.getBooleanOr("commandBlocks", jsonObject, false),
			JsonUtils.getIntOr("difficulty", jsonObject, 2),
			JsonUtils.getIntOr("gameMode", jsonObject, 0),
			JsonUtils.getBooleanOr("hardcore", jsonObject, false),
			JsonUtils.getBooleanOr("forceGameMode", jsonObject, false),
			JsonUtils.getRequiredStringOr("slotName", jsonObject, ""),
			JsonUtils.getRequiredStringOr("version", jsonObject, ""),
			RealmsServer.getCompatibility(JsonUtils.getRequiredStringOr("compatibility", jsonObject, RealmsServer.Compatibility.UNVERIFIABLE.name()))
		);
		realmsWorldOptions.templateId = JsonUtils.getLongOr("worldTemplateId", jsonObject, -1L);
		realmsWorldOptions.templateImage = JsonUtils.getStringOr("worldTemplateImage", jsonObject, DEFAULT_TEMPLATE_IMAGE);
		return realmsWorldOptions;
	}

	public String getSlotName(int i) {
		if (StringUtil.isBlank(this.slotName)) {
			return this.empty ? I18n.get("mco.configure.world.slot.empty") : this.getDefaultSlotName(i);
		} else {
			return this.slotName;
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

		if (!this.spawnMonsters) {
			jsonObject.addProperty("spawnMonsters", this.spawnMonsters);
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

		if (this.hardcore) {
			jsonObject.addProperty("hardcore", this.hardcore);
		}

		if (this.forceGameMode) {
			jsonObject.addProperty("forceGameMode", this.forceGameMode);
		}

		if (!Objects.equals(this.slotName, "")) {
			jsonObject.addProperty("slotName", this.slotName);
		}

		if (!Objects.equals(this.version, "")) {
			jsonObject.addProperty("version", this.version);
		}

		if (this.compatibility != DEFAULT_COMPATIBILITY) {
			jsonObject.addProperty("compatibility", this.compatibility.name());
		}

		return jsonObject.toString();
	}

	public RealmsWorldOptions clone() {
		return new RealmsWorldOptions(
			this.pvp,
			this.spawnMonsters,
			this.spawnProtection,
			this.commandBlocks,
			this.difficulty,
			this.gameMode,
			this.hardcore,
			this.forceGameMode,
			this.slotName,
			this.version,
			this.compatibility
		);
	}
}
