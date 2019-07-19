package net.minecraft.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class FrameTimer {
	private final long[] loggedTimes = new long[240];
	private int logStart;
	private int logLength;
	private int logEnd;

	public void logFrameDuration(long l) {
		this.loggedTimes[this.logEnd] = l;
		this.logEnd++;
		if (this.logEnd == 240) {
			this.logEnd = 0;
		}

		if (this.logLength < 240) {
			this.logStart = 0;
			this.logLength++;
		} else {
			this.logStart = this.wrapIndex(this.logEnd + 1);
		}
	}

	@Environment(EnvType.CLIENT)
	public int scaleSampleTo(long l, int i, int j) {
		double d = (double)l / (double)(1000000000L / (long)j);
		return (int)(d * (double)i);
	}

	@Environment(EnvType.CLIENT)
	public int getLogStart() {
		return this.logStart;
	}

	@Environment(EnvType.CLIENT)
	public int getLogEnd() {
		return this.logEnd;
	}

	public int wrapIndex(int i) {
		return i % 240;
	}

	@Environment(EnvType.CLIENT)
	public long[] getLog() {
		return this.loggedTimes;
	}
}
