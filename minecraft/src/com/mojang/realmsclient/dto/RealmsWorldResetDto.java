package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsWorldResetDto extends ValueObject implements ReflectionBasedSerialization {
	@SerializedName("seed")
	private final String seed;
	@SerializedName("worldTemplateId")
	private final long worldTemplateId;
	@SerializedName("levelType")
	private final int levelType;
	@SerializedName("generateStructures")
	private final boolean generateStructures;

	public RealmsWorldResetDto(String string, long l, int i, boolean bl) {
		this.seed = string;
		this.worldTemplateId = l;
		this.levelType = i;
		this.generateStructures = bl;
	}
}
