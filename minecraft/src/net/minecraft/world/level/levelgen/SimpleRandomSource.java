package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements RandomSource {
	private static final int MODULUS_BITS = 48;
	private static final long MODULUS_MASK = 281474976710655L;
	private static final long MULTIPLIER = 25214903917L;
	private static final long INCREMENT = 11L;
	private static final float FLOAT_MULTIPLIER = 5.9604645E-8F;
	private static final double DOUBLE_MULTIPLIER = 1.110223E-16F;
	private final AtomicLong seed = new AtomicLong();
	private double nextNextGaussian;
	private boolean haveNextNextGaussian;

	public SimpleRandomSource(long l) {
		this.setSeed(l);
	}

	@Override
	public void setSeed(long l) {
		if (!this.seed.compareAndSet(this.seed.get(), (l ^ 25214903917L) & 281474976710655L)) {
			throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
		}
	}

	private int next(int i) {
		long l = this.seed.get();
		long m = l * 25214903917L + 11L & 281474976710655L;
		if (!this.seed.compareAndSet(l, m)) {
			throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
		} else {
			return (int)(m >> 48 - i);
		}
	}

	@Override
	public int nextInt() {
		return this.next(32);
	}

	@Override
	public int nextInt(int i) {
		if (i <= 0) {
			throw new IllegalArgumentException("Bound must be positive");
		} else if ((i & i - 1) == 0) {
			return (int)((long)i * (long)this.next(31) >> 31);
		} else {
			int j;
			int k;
			do {
				j = this.next(31);
				k = j % i;
			} while (j - k + (i - 1) < 0);

			return k;
		}
	}

	@Override
	public long nextLong() {
		int i = this.next(32);
		int j = this.next(32);
		long l = (long)i << 32;
		return l + (long)j;
	}

	@Override
	public boolean nextBoolean() {
		return this.next(1) != 0;
	}

	@Override
	public float nextFloat() {
		return (float)this.next(24) * 5.9604645E-8F;
	}

	@Override
	public double nextDouble() {
		int i = this.next(26);
		int j = this.next(27);
		long l = ((long)i << 27) + (long)j;
		return (double)l * 1.110223E-16F;
	}

	@Override
	public double nextGaussian() {
		if (this.haveNextNextGaussian) {
			this.haveNextNextGaussian = false;
			return this.nextNextGaussian;
		} else {
			double d;
			double e;
			double f;
			do {
				d = 2.0 * this.nextDouble() - 1.0;
				e = 2.0 * this.nextDouble() - 1.0;
				f = Mth.square(d) + Mth.square(e);
			} while (f >= 1.0 || f == 0.0);

			double g = Math.sqrt(-2.0 * Math.log(f) / f);
			this.nextNextGaussian = e * g;
			this.haveNextNextGaussian = true;
			return d * g;
		}
	}
}
