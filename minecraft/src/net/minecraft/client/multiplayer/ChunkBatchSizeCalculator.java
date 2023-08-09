package net.minecraft.client.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ChunkBatchSizeCalculator {
	private static final int MAX_OLD_SAMPLES_WEIGHT = 49;
	private static final int CLAMP_COEFFICIENT = 3;
	private double aggregatedNanosPerChunk = 2000000.0;
	private int oldSamplesWeight = 1;
	private volatile long chunkBatchStartTime = Util.getNanos();

	public void onBatchStart() {
		this.chunkBatchStartTime = Util.getNanos();
	}

	public void onBatchFinished(int i) {
		if (i > 0) {
			double d = (double)(Util.getNanos() - this.chunkBatchStartTime);
			double e = d / (double)i;
			double f = Mth.clamp(e, this.aggregatedNanosPerChunk / 3.0, this.aggregatedNanosPerChunk * 3.0);
			this.aggregatedNanosPerChunk = (this.aggregatedNanosPerChunk * (double)this.oldSamplesWeight + f) / (double)(this.oldSamplesWeight + 1);
			this.oldSamplesWeight = Math.min(49, this.oldSamplesWeight + 1);
		}
	}

	public float getDesiredChunksPerTick() {
		return (float)(7000000.0 / this.aggregatedNanosPerChunk);
	}
}
