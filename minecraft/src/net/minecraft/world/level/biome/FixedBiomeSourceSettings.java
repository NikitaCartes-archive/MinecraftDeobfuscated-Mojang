package net.minecraft.world.level.biome;

public class FixedBiomeSourceSettings implements BiomeSourceSettings {
	private Biome biome = Biomes.PLAINS;

	public FixedBiomeSourceSettings setBiome(Biome biome) {
		this.biome = biome;
		return this;
	}

	public Biome getBiome() {
		return this.biome;
	}
}
