package net.minecraft.world.level.biome;

import net.minecraft.world.level.storage.LevelData;

public class FixedBiomeSourceSettings implements BiomeSourceSettings {
	private Biome biome = Biomes.PLAINS;

	public FixedBiomeSourceSettings(LevelData levelData) {
	}

	public FixedBiomeSourceSettings setBiome(Biome biome) {
		this.biome = biome;
		return this;
	}

	public Biome getBiome() {
		return this.biome;
	}
}
