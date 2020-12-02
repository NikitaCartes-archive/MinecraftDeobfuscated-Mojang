package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NoiseColumn {
	private final int minY;
	private final BlockState[] column;

	public NoiseColumn(int i, BlockState[] blockStates) {
		this.minY = i;
		this.column = blockStates;
	}

	public BlockState getBlockState(BlockPos blockPos) {
		int i = blockPos.getY() - this.minY;
		return i >= 0 && i < this.column.length ? this.column[i] : Blocks.AIR.defaultBlockState();
	}
}
