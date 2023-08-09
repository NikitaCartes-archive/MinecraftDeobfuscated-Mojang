package net.minecraft.util;

public class SampleLogger {
	public static final int CAPACITY = 240;
	private final long[] samples = new long[240];
	private int start;
	private int size;

	public void logSample(long l) {
		int i = this.wrapIndex(this.start + this.size);
		this.samples[i] = l;
		if (this.size < 240) {
			this.size++;
		} else {
			this.start = this.wrapIndex(this.start + 1);
		}
	}

	public int capacity() {
		return this.samples.length;
	}

	public int size() {
		return this.size;
	}

	public long get(int i) {
		if (i >= 0 && i < this.size) {
			return this.samples[this.wrapIndex(this.start + i)];
		} else {
			throw new IndexOutOfBoundsException(i + " out of bounds for length " + this.size);
		}
	}

	private int wrapIndex(int i) {
		return i % 240;
	}

	public void reset() {
		this.start = 0;
		this.size = 0;
	}
}
