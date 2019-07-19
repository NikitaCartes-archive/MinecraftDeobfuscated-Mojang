package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public interface BucketPickup {
	Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState);
}
