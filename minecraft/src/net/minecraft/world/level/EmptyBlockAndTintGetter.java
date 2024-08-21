package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public enum EmptyBlockAndTintGetter implements BlockAndTintGetter {
	INSTANCE;

	@Override
	public float getShade(Direction direction, boolean bl) {
		return 1.0F;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return LevelLightEngine.EMPTY;
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		return -1;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getMinY() {
		return 0;
	}
}
