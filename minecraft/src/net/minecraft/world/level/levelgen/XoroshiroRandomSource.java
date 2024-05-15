package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class XoroshiroRandomSource implements RandomSource {
	private static final float FLOAT_UNIT = 5.9604645E-8F;
	private static final double DOUBLE_UNIT = 1.110223E-16F;
	public static final Codec<XoroshiroRandomSource> CODEC = Xoroshiro128PlusPlus.CODEC
		.xmap(xoroshiro128PlusPlus -> new XoroshiroRandomSource(xoroshiro128PlusPlus), xoroshiroRandomSource -> xoroshiroRandomSource.randomNumberGenerator);
	private Xoroshiro128PlusPlus randomNumberGenerator;
	private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

	public XoroshiroRandomSource(long l) {
		this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(l));
	}

	public XoroshiroRandomSource(RandomSupport.Seed128bit seed128bit) {
		this.randomNumberGenerator = new Xoroshiro128PlusPlus(seed128bit);
	}

	public XoroshiroRandomSource(long l, long m) {
		this.randomNumberGenerator = new Xoroshiro128PlusPlus(l, m);
	}

	private XoroshiroRandomSource(Xoroshiro128PlusPlus xoroshiro128PlusPlus) {
		this.randomNumberGenerator = xoroshiro128PlusPlus;
	}

	@Override
	public RandomSource fork() {
		return new XoroshiroRandomSource(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
	}

	@Override
	public PositionalRandomFactory forkPositional() {
		return new XoroshiroRandomSource.XoroshiroPositionalRandomFactory(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
	}

	@Override
	public void setSeed(long l) {
		this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(l));
		this.gaussianSource.reset();
	}

	@Override
	public int nextInt() {
		return (int)this.randomNumberGenerator.nextLong();
	}

	@Override
	public int nextInt(int i) {
		if (i <= 0) {
			throw new IllegalArgumentException("Bound must be positive");
		} else {
			long l = Integer.toUnsignedLong(this.nextInt());
			long m = l * (long)i;
			long n = m & 4294967295L;
			if (n < (long)i) {
				for (int j = Integer.remainderUnsigned(~i + 1, i); n < (long)j; n = m & 4294967295L) {
					l = Integer.toUnsignedLong(this.nextInt());
					m = l * (long)i;
				}
			}

			long o = m >> 32;
			return (int)o;
		}
	}

	@Override
	public long nextLong() {
		return this.randomNumberGenerator.nextLong();
	}

	@Override
	public boolean nextBoolean() {
		return (this.randomNumberGenerator.nextLong() & 1L) != 0L;
	}

	@Override
	public float nextFloat() {
		return (float)this.nextBits(24) * 5.9604645E-8F;
	}

	@Override
	public double nextDouble() {
		return (double)this.nextBits(53) * 1.110223E-16F;
	}

	@Override
	public double nextGaussian() {
		return this.gaussianSource.nextGaussian();
	}

	@Override
	public void consumeCount(int i) {
		for (int j = 0; j < i; j++) {
			this.randomNumberGenerator.nextLong();
		}
	}

	private long nextBits(int i) {
		return this.randomNumberGenerator.nextLong() >>> 64 - i;
	}

	public static class XoroshiroPositionalRandomFactory implements PositionalRandomFactory {
		private final long seedLo;
		private final long seedHi;

		public XoroshiroPositionalRandomFactory(long l, long m) {
			this.seedLo = l;
			this.seedHi = m;
		}

		@Override
		public RandomSource at(int i, int j, int k) {
			long l = Mth.getSeed(i, j, k);
			long m = l ^ this.seedLo;
			return new XoroshiroRandomSource(m, this.seedHi);
		}

		@Override
		public RandomSource fromHashOf(String string) {
			RandomSupport.Seed128bit seed128bit = RandomSupport.seedFromHashOf(string);
			return new XoroshiroRandomSource(seed128bit.xor(this.seedLo, this.seedHi));
		}

		@Override
		public RandomSource fromSeed(long l) {
			return new XoroshiroRandomSource(l ^ this.seedLo, l ^ this.seedHi);
		}

		@VisibleForTesting
		@Override
		public void parityConfigString(StringBuilder stringBuilder) {
			stringBuilder.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
		}
	}
}
