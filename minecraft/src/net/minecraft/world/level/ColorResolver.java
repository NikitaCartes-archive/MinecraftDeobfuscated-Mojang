package net.minecraft.world.level;

import net.minecraft.world.level.biome.Biome;

public interface ColorResolver {
	int getColor(Biome biome, double d, double e);
}
