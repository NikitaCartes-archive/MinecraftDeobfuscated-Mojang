package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;

public final class BadlandsBiome extends Biome {
	public BadlandsBiome(Biome.BiomeBuilder biomeBuilder) {
		super(biomeBuilder);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getFoliageColor() {
		return 10387789;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getGrassColor(double d, double e) {
		return 9470285;
	}
}
