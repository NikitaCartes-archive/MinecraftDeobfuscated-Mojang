package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements BitRandomSource {
	private static final int MODULUS_BITS = 48;
	private static final long MODULUS_MASK = 281474976710655L;
	private static final long MULTIPLIER = 25214903917L;
	private static final long INCREMENT = 11L;
	private final AtomicLong seed = new AtomicLong();
	private double nextNextGaussian;
	private boolean haveNextNextGaussian;

	public SimpleRandomSource(long l) {
		this.setSeed(l);
	}

	@Override
	public RandomSource fork() {
		return new SimpleRandomSource(this.nextLong());
	}

	@Override
	public void setSeed(long l) {
		if (!this.seed.compareAndSet(this.seed.get(), (l ^ 25214903917L) & 281474976710655L)) {
			throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
		}
	}

	@Override
	public int next(int i) {
		long l = this.seed.get();
		long m = l * 25214903917L + 11L & 281474976710655L;
		if (!this.seed.compareAndSet(l, m)) {
			throw ThreadingDetector.makeThreadingException("SimpleRandomSource", null);
		} else {
			return (int)(m >> 48 - i);
		}
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
