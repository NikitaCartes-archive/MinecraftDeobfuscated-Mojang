package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface WorldlyContainerHolder {
	WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos);
}
