package net.minecraft.world.level.biome;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer {
	INSTANCE;

	@Override
	public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
		return noiseBiomeSource.getNoiseBiome(i >> 2, j >> 2, k >> 2);
	}
}
