package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import net.minecraft.util.Mth;

public class XoroshiroRandomSource implements RandomSource {
	private static final float FLOAT_UNIT = 5.9604645E-8F;
	private static final double DOUBLE_UNIT = 1.110223E-16F;
	private Xoroshiro128PlusPlus randomNumberGenerator;
	private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

	public XoroshiroRandomSource(long l) {
		this.setSeed(l);
	}

	public XoroshiroRandomSource(long l, long m) {
		this.randomNumberGenerator = new Xoroshiro128PlusPlus(l, m);
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
			return Math.abs((int)(this.randomNumberGenerator.nextLong() % (long)i));
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
		private static final HashFunction MD5_128 = Hashing.md5();
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
			byte[] bs = MD5_128.hashString(string, Charsets.UTF_8).asBytes();
			long l = Longs.fromBytes(bs[0], bs[1], bs[2], bs[3], bs[4], bs[5], bs[6], bs[7]);
			long m = Longs.fromBytes(bs[8], bs[9], bs[10], bs[11], bs[12], bs[13], bs[14], bs[15]);
			return new XoroshiroRandomSource(l ^ this.seedLo, m ^ this.seedHi);
		}

		@VisibleForTesting
		@Override
		public void parityConfigString(StringBuilder stringBuilder) {
			stringBuilder.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
		}
	}
}
