package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;

public final class SwampBiome extends Biome {
	public SwampBiome(Biome.BiomeBuilder biomeBuilder) {
		super(biomeBuilder);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getGrassColor(double d, double e) {
		double f = BIOME_INFO_NOISE.getValue(d * 0.0225, e * 0.0225, false);
		return f < -0.1 ? 5011004 : 6975545;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getFoliageColor() {
		return 6975545;
	}
}
