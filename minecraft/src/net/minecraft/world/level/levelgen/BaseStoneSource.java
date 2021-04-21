package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BaseStoneSource {
	default BlockState getBaseBlock(BlockPos blockPos) {
		return this.getBaseBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	BlockState getBaseBlock(int i, int j, int k);
}
