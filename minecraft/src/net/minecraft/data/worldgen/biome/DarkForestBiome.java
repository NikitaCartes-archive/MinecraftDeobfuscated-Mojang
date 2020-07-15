package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;

public final class DarkForestBiome extends Biome {
	public DarkForestBiome(Biome.BiomeBuilder biomeBuilder) {
		super(biomeBuilder);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getGrassColor(double d, double e) {
		int i = super.getGrassColor(d, e);
		return (i & 16711422) + 2634762 >> 1;
	}
}
