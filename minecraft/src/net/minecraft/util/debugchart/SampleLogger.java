package net.minecraft.util.debugchart;

public class SampleLogger {
	public static final int CAPACITY = 240;
	private final long[][] samples;
	private int start;
	private int size;

	public SampleLogger(int i) {
		this.samples = new long[240][i];
	}

	public void logSample(long l) {
		int i = this.wrapIndex(this.start + this.size);
		this.samples[i][0] = l;
		if (this.size < 240) {
			this.size++;
		} else {
			this.start = this.wrapIndex(this.start + 1);
		}
	}

	public void logPartialSample(long l, int i) {
		int j = this.wrapIndex(this.start + this.size);
		long[] ls = this.samples[j];
		if (i >= 1 && i < ls.length) {
			ls[i] = l;
		} else {
			throw new IndexOutOfBoundsException(i + " out of bounds for dimensions " + ls.length);
		}
	}

	public int capacity() {
		return this.samples.length;
	}

	public int size() {
		return this.size;
	}

	public long get(int i) {
		return this.get(i, 0);
	}

	public long get(int i, int j) {
		if (i >= 0 && i < this.size) {
			long[] ls = this.samples[this.wrapIndex(this.start + i)];
			if (j >= 0 && j < ls.length) {
				return ls[j];
			} else {
				throw new IndexOutOfBoundsException(j + " out of bounds for dimensions " + ls.length);
			}
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
