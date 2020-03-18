package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class NoiseColumn implements BlockGetter {
	private final BlockState[] column;

	public NoiseColumn(BlockState[] blockStates) {
		this.column = blockStates;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		int i = blockPos.getY();
		return i >= 0 && i < this.column.length ? this.column[i] : Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.getBlockState(blockPos).getFluidState();
	}
}
