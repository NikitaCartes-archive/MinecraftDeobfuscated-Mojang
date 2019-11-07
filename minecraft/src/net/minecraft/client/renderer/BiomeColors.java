package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeColors {
	public static final ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
	public static final ColorResolver FOLIAGE_COLOR_RESOLVER = (biome, d, e) -> biome.getFoliageColor();
	public static final ColorResolver WATER_COLOR_RESOLVER = (biome, d, e) -> biome.getWaterColor();

	private static int getAverageColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, ColorResolver colorResolver) {
		return blockAndTintGetter.getBlockTint(blockPos, colorResolver);
	}

	public static int getAverageGrassColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		return getAverageColor(blockAndTintGetter, blockPos, GRASS_COLOR_RESOLVER);
	}

	public static int getAverageFoliageColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		return getAverageColor(blockAndTintGetter, blockPos, FOLIAGE_COLOR_RESOLVER);
	}

	public static int getAverageWaterColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		return getAverageColor(blockAndTintGetter, blockPos, WATER_COLOR_RESOLVER);
	}
}
