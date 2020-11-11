package net.minecraft.world.level.material;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum FogType {
	LAVA,
	WATER,
	POWDER_SNOW,
	NONE;
}
