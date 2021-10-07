package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class LegacyRandomSource implements BitRandomSource {
	private static final int MODULUS_BITS = 48;
	private static final long MODULUS_MASK = 281474976710655L;
	private static final long MULTIPLIER = 25214903917L;
	private static final long INCREMENT = 11L;
	private final AtomicLong seed = new AtomicLong();
	private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

	public LegacyRandomSource(long l) {
		this.setSeed(l);
	}

	@Override
	public RandomSource fork() {
		return new LegacyRandomSource(this.nextLong());
	}

	@Override
	public PositionalRandomFactory forkPositional() {
		return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
	}

	@Override
	public void setSeed(long l) {
		if (!this.seed.compareAndSet(this.seed.get(), (l ^ 25214903917L) & 281474976710655L)) {
			throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
		}
	}

	@Override
	public int next(int i) {
		long l = this.seed.get();
		long m = l * 25214903917L + 11L & 281474976710655L;
		if (!this.seed.compareAndSet(l, m)) {
			throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
		} else {
			return (int)(m >> 48 - i);
		}
	}

	@Override
	public double nextGaussian() {
		return this.gaussianSource.nextGaussian();
	}

	public static class LegacyPositionalRandomFactory implements PositionalRandomFactory {
		private final long seed;

		public LegacyPositionalRandomFactory(long l) {
			this.seed = l;
		}

		@Override
		public RandomSource at(int i, int j, int k) {
			long l = Mth.getSeed(i, j, k);
			long m = l ^ this.seed;
			return new LegacyRandomSource(m);
		}

		@Override
		public RandomSource at(String string) {
			int i = string.hashCode();
			return new LegacyRandomSource((long)i ^ this.seed);
		}
	}
}
