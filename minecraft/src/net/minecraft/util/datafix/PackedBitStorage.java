package net.minecraft.util.datafix;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

public class PackedBitStorage {
	private final long[] data;
	private final int bits;
	private final long mask;
	private final int size;

	public PackedBitStorage(int i, int j) {
		this(i, j, new long[Mth.roundUp(j * i, 64) / 64]);
	}

	public PackedBitStorage(int i, int j, long[] ls) {
		Validate.inclusiveBetween(1L, 32L, (long)i);
		this.size = j;
		this.bits = i;
		this.data = ls;
		this.mask = (1L << i) - 1L;
		int k = Mth.roundUp(j * i, 64) / 64;
		if (ls.length != k) {
			throw new IllegalArgumentException("Invalid length given for storage, got: " + ls.length + " but expected: " + k);
		}
	}

	public void set(int i, int j) {
		Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
		Validate.inclusiveBetween(0L, this.mask, (long)j);
		int k = i * this.bits;
		int l = k >> 6;
		int m = (i + 1) * this.bits - 1 >> 6;
		int n = k ^ l << 6;
		this.data[l] = this.data[l] & ~(this.mask << n) | ((long)j & this.mask) << n;
		if (l != m) {
			int o = 64 - n;
			int p = this.bits - o;
			this.data[m] = this.data[m] >>> p << p | ((long)j & this.mask) >> o;
		}
	}

	public int get(int i) {
		Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
		int j = i * this.bits;
		int k = j >> 6;
		int l = (i + 1) * this.bits - 1 >> 6;
		int m = j ^ k << 6;
		if (k == l) {
			return (int)(this.data[k] >>> m & this.mask);
		} else {
			int n = 64 - m;
			return (int)((this.data[k] >>> m | this.data[l] << n) & this.mask);
		}
	}

	public long[] getRaw() {
		return this.data;
	}

	public int getBits() {
		return this.bits;
	}
}
