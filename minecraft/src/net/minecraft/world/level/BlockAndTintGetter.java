package net.minecraft.world.level;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndTintGetter extends BlockGetter {
	@Environment(EnvType.CLIENT)
	float getShade(Direction direction, boolean bl);

	LevelLightEngine getLightEngine();

	@Environment(EnvType.CLIENT)
	int getBlockTint(BlockPos blockPos, ColorResolver colorResolver);

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
	Vector3f getExtraTint(BlockState blockState, BlockPos blockPos);
}
