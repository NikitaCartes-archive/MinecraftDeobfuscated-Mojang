package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BaseStoneSource {
	default BlockState getBaseStone(BlockPos blockPos) {
		return this.getBaseStone(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	BlockState getBaseStone(int i, int j, int k);
}
