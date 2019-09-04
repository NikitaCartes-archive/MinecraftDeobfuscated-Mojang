package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndBiomeGetter extends BlockGetter {
	BiomeManager getBiomeManager();

	LevelLightEngine getLightEngine();

	default Biome getBiome(BlockPos blockPos) {
		return this.getBiomeManager().getBiome(blockPos);
	}

	default int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
		return this.getLightEngine().getLayerListener(lightLayer).getLightValue(blockPos);
	}

	default int getRawBrightness(BlockPos blockPos, int i) {
		return this.getLightEngine().getRawBrightness(blockPos, i);
	}

	default boolean canSeeSky(BlockPos blockPos) {
		return this.getBrightness(LightLayer.SKY, blockPos) >= this.getMaxLightLevel();
	}

	@Environment(EnvType.CLIENT)
	default int getLightColor(BlockPos blockPos) {
		return this.getLightColor(this.getBlockState(blockPos), blockPos);
	}

	@Environment(EnvType.CLIENT)
	default int getLightColor(BlockState blockState, BlockPos blockPos) {
		if (blockState.emissiveRendering()) {
			return 15728880;
		} else {
			int i = this.getBrightness(LightLayer.SKY, blockPos);
			int j = this.getBrightness(LightLayer.BLOCK, blockPos);
			int k = blockState.getLightEmission();
			if (j < k) {
				j = k;
			}

			return i << 20 | j << 4;
		}
	}
}
