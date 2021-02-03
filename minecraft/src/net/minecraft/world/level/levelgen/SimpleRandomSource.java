package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements RandomSource {
	private final AtomicLong seed = new AtomicLong();
	private boolean haveNextNextGaussian = false;

	public SimpleRandomSource(long l) {
		this.setSeed(l);
	}

	public void setSeed(long l) {
		if (!this.seed.compareAndSet(this.seed.get(), (l ^ 25214903917L) & 281474976710655L)) {
			throw ThreadingDetector.makeThreadingException("SimpleRandomSource");
		}
	}

	private int next(int i) {
		long l = this.seed.get();
		long m = l * 25214903917L + 11L & 281474976710655L;
		if (!this.seed.compareAndSet(l, m)) {
			throw ThreadingDetector.makeThreadingException("SimpleRandomSource");
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
	public double nextDouble() {
		return (double)(((long)this.next(26) << 27) + (long)this.next(27)) * 1.110223E-16F;
	}
}
