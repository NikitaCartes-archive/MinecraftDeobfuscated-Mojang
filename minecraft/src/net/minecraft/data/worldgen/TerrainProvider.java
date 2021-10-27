package net.minecraft.data.worldgen;

import net.minecraft.world.level.biome.TerrainShaper;

public class TerrainProvider {
	public static TerrainShaper overworld() {
		return new TerrainShaper();
	}

	public static TerrainShaper nether() {
		return new TerrainShaper();
	}

	public static TerrainShaper end() {
		return new TerrainShaper();
	}
}
