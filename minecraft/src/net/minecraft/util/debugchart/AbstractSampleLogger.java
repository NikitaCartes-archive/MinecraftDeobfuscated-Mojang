package net.minecraft.util.debugchart;

public abstract class AbstractSampleLogger implements SampleLogger {
	protected final long[] defaults;
	protected final long[] sample;

	protected AbstractSampleLogger(int i, long[] ls) {
		if (ls.length != i) {
			throw new IllegalArgumentException("defaults have incorrect length of " + ls.length);
		} else {
			this.sample = new long[i];
			this.defaults = ls;
		}
	}

	@Override
	public void logFullSample(long[] ls) {
		System.arraycopy(ls, 0, this.sample, 0, ls.length);
		this.useSample();
		this.resetSample();
	}

	@Override
	public void logSample(long l) {
		this.sample[0] = l;
		this.useSample();
		this.resetSample();
	}

	@Override
	public void logPartialSample(long l, int i) {
		if (i >= 1 && i < this.sample.length) {
			this.sample[i] = l;
		} else {
			throw new IndexOutOfBoundsException(i + " out of bounds for dimensions " + this.sample.length);
		}
	}

	protected abstract void useSample();

	protected void resetSample() {
		System.arraycopy(this.defaults, 0, this.sample, 0, this.defaults.length);
	}
}
