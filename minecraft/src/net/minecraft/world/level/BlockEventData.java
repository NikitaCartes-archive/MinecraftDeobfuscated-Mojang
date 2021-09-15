package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public record BlockEventData() {
	private final BlockPos pos;
	private final Block block;
	private final int paramA;
	private final int paramB;

	public BlockEventData(BlockPos blockPos, Block block, int i, int j) {
		this.pos = blockPos;
		this.block = block;
		this.paramA = i;
		this.paramB = j;
	}
}
