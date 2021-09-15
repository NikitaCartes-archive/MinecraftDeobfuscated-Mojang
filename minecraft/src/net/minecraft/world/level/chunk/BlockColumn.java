package net.minecraft.world.level.chunk;

import net.minecraft.world.level.block.state.BlockState;

public interface BlockColumn {
	BlockState getBlock(int i);

	void setBlock(int i, BlockState blockState);
}
