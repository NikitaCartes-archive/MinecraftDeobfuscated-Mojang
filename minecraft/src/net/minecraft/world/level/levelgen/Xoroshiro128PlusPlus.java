package net.minecraft.world.level.levelgen;

public class Xoroshiro128PlusPlus {
	private long seedLo;
	private long seedHi;

	public Xoroshiro128PlusPlus(RandomSupport.Seed128bit seed128bit) {
		this(seed128bit.seedLo(), seed128bit.seedHi());
	}

	public Xoroshiro128PlusPlus(long l, long m) {
		this.seedLo = l;
		this.seedHi = m;
		if ((this.seedLo | this.seedHi) == 0L) {
			this.seedLo = -7046029254386353131L;
			this.seedHi = 7640891576956012809L;
		}
	}

	public long nextLong() {
		long l = this.seedLo;
		long m = this.seedHi;
		long n = Long.rotateLeft(l + m, 17) + l;
		m ^= l;
		this.seedLo = Long.rotateLeft(l, 49) ^ m ^ m << 21;
		this.seedHi = Long.rotateLeft(m, 28);
		return n;
	}
}
