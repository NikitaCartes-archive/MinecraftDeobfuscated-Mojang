package net.minecraft.client.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ChunkReceiveSpeedAccumulator {
	private final int[] batchSizes;
	private final int[] batchDurations;
	private int index;
	private int filledSize;

	public ChunkReceiveSpeedAccumulator(int i) {
		this.batchSizes = new int[i];
		this.batchDurations = new int[i];
	}

	public void accumulate(int i, long l) {
		this.batchSizes[this.index] = i;
		this.batchDurations[this.index] = (int)Mth.clamp((float)l, 0.0F, 15000.0F);
		this.index = (this.index + 1) % this.batchSizes.length;
		if (this.filledSize < this.batchSizes.length) {
			this.filledSize++;
		}
	}

	public double getMillisPerChunk() {
		int i = 0;
		int j = 0;

		for (int k = 0; k < Math.min(this.filledSize, this.batchSizes.length); k++) {
			i += this.batchSizes[k];
			j += this.batchDurations[k];
		}

		return (double)j * 1.0 / (double)i;
	}
}
