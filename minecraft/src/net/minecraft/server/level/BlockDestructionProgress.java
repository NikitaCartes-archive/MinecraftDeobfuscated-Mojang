package net.minecraft.server.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class BlockDestructionProgress {
	private final int id;
	private final BlockPos pos;
	private int progress;
	private int updatedRenderTick;

	public BlockDestructionProgress(int i, BlockPos blockPos) {
		this.id = i;
		this.pos = blockPos;
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
}
