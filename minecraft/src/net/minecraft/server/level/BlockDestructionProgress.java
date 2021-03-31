package net.minecraft.server.level;

import net.minecraft.core.BlockPos;

public class BlockDestructionProgress implements Comparable<BlockDestructionProgress> {
	private final int id;
	private final BlockPos pos;
	private int progress;
	private int updatedRenderTick;

	public BlockDestructionProgress(int i, BlockPos blockPos) {
		this.id = i;
		this.pos = blockPos;
	}

	public int getId() {
		return this.id;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public void setProgress(int i) {
		if (i > 10) {
			i = 10;
		}

		this.progress = i;
	}

	public int getProgress() {
		return this.progress;
	}

	public void updateTick(int i) {
		this.updatedRenderTick = i;
	}

	public int getUpdatedRenderTick() {
		return this.updatedRenderTick;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)object;
			return this.id == blockDestructionProgress.id;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Integer.hashCode(this.id);
	}

	public int compareTo(BlockDestructionProgress blockDestructionProgress) {
		return this.progress != blockDestructionProgress.progress
			? Integer.compare(this.progress, blockDestructionProgress.progress)
			: Integer.compare(this.id, blockDestructionProgress.id);
	}
}
