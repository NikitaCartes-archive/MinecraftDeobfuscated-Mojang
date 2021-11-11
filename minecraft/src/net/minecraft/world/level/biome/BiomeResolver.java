package net.minecraft.world.level.biome;

public interface BiomeResolver {
	Biome getNoiseBiome(int i, int j, int k, Climate.Sampler sampler);
}
