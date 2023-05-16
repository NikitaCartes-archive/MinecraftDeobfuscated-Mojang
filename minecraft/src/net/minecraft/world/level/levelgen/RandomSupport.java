package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSupport {
	public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
	public static final long SILVER_RATIO_64 = 7640891576956012809L;
	private static final HashFunction MD5_128 = Hashing.md5();
	private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

	@VisibleForTesting
	public static long mixStafford13(long l) {
		l = (l ^ l >>> 30) * -4658895280553007687L;
		l = (l ^ l >>> 27) * -7723592293110705685L;
		return l ^ l >>> 31;
	}

	public static RandomSupport.Seed128bit upgradeSeedTo128bit(long l) {
		long m = l ^ 7640891576956012809L;
		long n = m + -7046029254386353131L;
		return new RandomSupport.Seed128bit(mixStafford13(m), mixStafford13(n));
	}

	public static RandomSupport.Seed128bit seedFromHashOf(String string) {
		byte[] bs = MD5_128.hashString(string, Charsets.UTF_8).asBytes();
		long l = Longs.fromBytes(bs[0], bs[1], bs[2], bs[3], bs[4], bs[5], bs[6], bs[7]);
		long m = Longs.fromBytes(bs[8], bs[9], bs[10], bs[11], bs[12], bs[13], bs[14], bs[15]);
		return new RandomSupport.Seed128bit(l, m);
	}

	public static long generateUniqueSeed() {
		return SEED_UNIQUIFIER.updateAndGet(l -> l * 1181783497276652981L) ^ System.nanoTime();
	}

	public static record Seed128bit(long seedLo, long seedHi) {
		public RandomSupport.Seed128bit xor(long l, long m) {
			return new RandomSupport.Seed128bit(this.seedLo ^ l, this.seedHi ^ m);
		}

		public RandomSupport.Seed128bit xor(RandomSupport.Seed128bit seed128bit) {
			return this.xor(seed128bit.seedLo, seed128bit.seedHi);
		}
	}
}
