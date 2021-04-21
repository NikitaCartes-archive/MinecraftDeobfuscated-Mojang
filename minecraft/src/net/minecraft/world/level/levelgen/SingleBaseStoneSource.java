package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;

public class SingleBaseStoneSource implements BaseStoneSource {
	private final BlockState state;

	public SingleBaseStoneSource(BlockState blockState) {
		this.state = blockState;
	}

	@Override
	public BlockState getBaseBlock(int i, int j, int k) {
		return this.state;
	}
}
