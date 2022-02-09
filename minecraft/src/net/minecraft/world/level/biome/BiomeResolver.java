package net.minecraft.world.level.biome;

import net.minecraft.core.Holder;

public interface BiomeResolver {
	Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler);
}
