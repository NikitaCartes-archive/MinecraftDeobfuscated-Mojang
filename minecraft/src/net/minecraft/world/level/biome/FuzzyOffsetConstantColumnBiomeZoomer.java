package net.minecraft.world.level.biome;

public enum FuzzyOffsetConstantColumnBiomeZoomer implements BiomeZoomer {
	INSTANCE;

	@Override
	public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
		return FuzzyOffsetBiomeZoomer.INSTANCE.getBiome(l, i, 0, k, noiseBiomeSource);
	}
}
