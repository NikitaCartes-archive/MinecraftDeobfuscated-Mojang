package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndTintGetter extends BlockGetter {
	float getShade(Direction direction, boolean bl);

	LevelLightEngine getLightEngine();

	int getBlockTint(BlockPos blockPos, ColorResolver colorResolver);

	default int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
		return this.getLightEngine().getLayerListener(lightLayer).getLightValue(blockPos);
	}

	default int getRawBrightness(BlockPos blockPos, int i) {
		return this.getLightEngine().getRawBrightness(blockPos, i);
	}

	default boolean canSeeSky(BlockPos blockPos) {
		return this.getBrightness(LightLayer.SKY, blockPos) >= 15;
	}
}
