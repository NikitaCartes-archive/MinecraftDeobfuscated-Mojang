package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class BlockEventData {
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

	public BlockPos getPos() {
		return this.pos;
	}

	public Block getBlock() {
		return this.block;
	}

	public int getParamA() {
		return this.paramA;
	}

	public int getParamB() {
		return this.paramB;
	}

	public boolean equals(Object object) {
		if (!(object instanceof BlockEventData)) {
			return false;
		} else {
			BlockEventData blockEventData = (BlockEventData)object;
			return this.pos.equals(blockEventData.pos)
				&& this.paramA == blockEventData.paramA
				&& this.paramB == blockEventData.paramB
				&& this.block == blockEventData.block;
		}
	}

	public int hashCode() {
		int i = this.pos.hashCode();
		i = 31 * i + this.block.hashCode();
		i = 31 * i + this.paramA;
		return 31 * i + this.paramB;
	}

	public String toString() {
		return "TE(" + this.pos + ")," + this.paramA + "," + this.paramB + "," + this.block;
	}
}
