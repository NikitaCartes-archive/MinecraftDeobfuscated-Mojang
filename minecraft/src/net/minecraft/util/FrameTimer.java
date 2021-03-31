package net.minecraft.util;

public class FrameTimer {
	public static final int LOGGING_LENGTH = 240;
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

	public long getAverageDuration(int i) {
		int j = (this.logStart + i) % 240;
		int k = this.logStart;

		long l;
		for (l = 0L; k != j; k++) {
			l += this.loggedTimes[k];
		}

		return l / (long)i;
	}

	public int scaleAverageDurationTo(int i, int j) {
		return this.scaleSampleTo(this.getAverageDuration(i), j, 60);
	}

	public int scaleSampleTo(long l, int i, int j) {
		double d = (double)l / (double)(1000000000L / (long)j);
		return (int)(d * (double)i);
	}

	public int getLogStart() {
		return this.logStart;
	}

	public int getLogEnd() {
		return this.logEnd;
	}

	public int wrapIndex(int i) {
		return i % 240;
	}

	public long[] getLog() {
		return this.loggedTimes;
	}
}
