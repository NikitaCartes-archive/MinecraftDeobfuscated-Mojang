package net.minecraft.util.debugchart;

public class LocalSampleLogger extends AbstractSampleLogger implements SampleStorage {
	public static final int CAPACITY = 240;
	private final long[][] samples;
	private int start;
	private int size;

	public LocalSampleLogger(int i) {
		this(i, new long[i]);
	}

	public LocalSampleLogger(int i, long[] ls) {
		super(i, ls);
		this.samples = new long[240][i];
	}

	@Override
	protected void useSample() {
		int i = this.wrapIndex(this.start + this.size);
		System.arraycopy(this.sample, 0, this.samples[i], 0, this.sample.length);
		if (this.size < 240) {
			this.size++;
		} else {
			this.start = this.wrapIndex(this.start + 1);
		}
	}

	@Override
	public int capacity() {
		return this.samples.length;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public long get(int i) {
		return this.get(i, 0);
	}

	@Override
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

	@Override
	public void reset() {
		this.start = 0;
		this.size = 0;
	}
}
