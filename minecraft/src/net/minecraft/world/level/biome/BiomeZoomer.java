package net.minecraft.world.level.biome;

public interface BiomeZoomer {
	Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource);
}
