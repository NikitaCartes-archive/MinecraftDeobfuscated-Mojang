package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public interface BlockAndBiomeGetter extends BlockGetter {
	Biome getBiome(BlockPos blockPos);

	int getBrightness(LightLayer lightLayer, BlockPos blockPos);

	default boolean canSeeSky(BlockPos blockPos) {
		return this.getBrightness(LightLayer.SKY, blockPos) >= this.getMaxLightLevel();
	}

	@Environment(EnvType.CLIENT)
	default int getLightColor(BlockPos blockPos, int i) {
		int j = this.getBrightness(LightLayer.SKY, blockPos);
		int k = this.getBrightness(LightLayer.BLOCK, blockPos);
		if (k < i) {
			k = i;
		}

		return j << 20 | k << 4;
	}
}
