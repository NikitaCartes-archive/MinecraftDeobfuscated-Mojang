package net.minecraft.world.level;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

@FunctionalInterface
public interface ColorResolver {
	int getColor(Holder<Biome> holder, double d, double e);
}
