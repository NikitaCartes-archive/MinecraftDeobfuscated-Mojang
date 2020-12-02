package net.minecraft.world.level.biome;

import net.minecraft.core.QuartPos;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer {
	INSTANCE;

	@Override
	public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
		return noiseBiomeSource.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(j), QuartPos.fromBlock(k));
	}
}
