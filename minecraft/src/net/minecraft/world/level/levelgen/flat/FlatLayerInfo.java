package net.minecraft.world.level.levelgen.flat;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FlatLayerInfo {
	private final BlockState blockState;
	private final int height;
	private int start;

	public FlatLayerInfo(int i, Block block) {
		this.height = i;
		this.blockState = block.defaultBlockState();
	}

	public int getHeight() {
		return this.height;
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	public int getStart() {
		return this.start;
	}

	public void setStart(int i) {
		this.start = i;
	}

	public String toString() {
		return (this.height != 1 ? this.height + "*" : "") + Registry.BLOCK.getKey(this.blockState.getBlock());
	}
}
