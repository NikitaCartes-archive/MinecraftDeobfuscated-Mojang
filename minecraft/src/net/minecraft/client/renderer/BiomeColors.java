package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeColors {
	private static final BiomeColors.ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
	private static final BiomeColors.ColorResolver FOLIAGE_COLOR_RESOLVER = Biome::getFoliageColor;
	private static final BiomeColors.ColorResolver WATER_COLOR_RESOLVER = (biome, blockPos) -> biome.getWaterColor();
	private static final BiomeColors.ColorResolver WATER_FOG_COLOR_RESOLVER = (biome, blockPos) -> biome.getWaterFogColor();

	private static int getAverageColor(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos, BiomeColors.ColorResolver colorResolver) {
		int i = 0;
		int j = 0;
		int k = 0;
		int l = Minecraft.getInstance().options.biomeBlendRadius;
		if (l == 0) {
			return colorResolver.getColor(blockAndBiomeGetter.getBiome(blockPos), blockPos);
		} else {
			int m = (l * 2 + 1) * (l * 2 + 1);
			Cursor3D cursor3D = new Cursor3D(blockPos.getX() - l, blockPos.getY(), blockPos.getZ() - l, blockPos.getX() + l, blockPos.getY(), blockPos.getZ() + l);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			while (cursor3D.advance()) {
				mutableBlockPos.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
				int n = colorResolver.getColor(blockAndBiomeGetter.getBiome(mutableBlockPos), mutableBlockPos);
				i += (n & 0xFF0000) >> 16;
				j += (n & 0xFF00) >> 8;
				k += n & 0xFF;
			}

			return (i / m & 0xFF) << 16 | (j / m & 0xFF) << 8 | k / m & 0xFF;
		}
	}

	public static int getAverageGrassColor(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
		return getAverageColor(blockAndBiomeGetter, blockPos, GRASS_COLOR_RESOLVER);
	}

	public static int getAverageFoliageColor(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
		return getAverageColor(blockAndBiomeGetter, blockPos, FOLIAGE_COLOR_RESOLVER);
	}

	public static int getAverageWaterColor(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
		return getAverageColor(blockAndBiomeGetter, blockPos, WATER_COLOR_RESOLVER);
	}

	@Environment(EnvType.CLIENT)
	interface ColorResolver {
		int getColor(Biome biome, BlockPos blockPos);
	}
}
