package net.minecraft.util;

import java.util.function.IntConsumer;
import net.minecraft.Util;
import org.apache.commons.lang3.Validate;

public class BitStorage {
	private final long[] data;
	private final int bits;
	private final long mask;
	private final int size;

	public BitStorage(int i, int j) {
		this(i, j, new long[Mth.roundUp(j * i, 64) / 64]);
	}

	public BitStorage(int i, int j, long[] ls) {
		Validate.inclusiveBetween(1L, 32L, (long)i);
		this.size = j;
		this.bits = i;
		this.data = ls;
		this.mask = (1L << i) - 1L;
		int k = Mth.roundUp(j * i, 64) / 64;
		if (ls.length != k) {
			throw (RuntimeException)Util.pauseInIde(new RuntimeException("Invalid length given for storage, got: " + ls.length + " but expected: " + k));
		}
	}

	public int getAndSet(int i, int j) {
		Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
		Validate.inclusiveBetween(0L, this.mask, (long)j);
		int k = i * this.bits;
		int l = k >> 6;
		int m = (i + 1) * this.bits - 1 >> 6;
		int n = k ^ l << 6;
		int o = 0;
		o |= (int)(this.data[l] >>> n & this.mask);
		this.data[l] = this.data[l] & ~(this.mask << n) | ((long)j & this.mask) << n;
		if (l != m) {
			int p = 64 - n;
			int q = this.bits - p;
			o |= (int)(this.data[m] << p & this.mask);
			this.data[m] = this.data[m] >>> q << q | ((long)j & this.mask) >> p;
		}

		return o;
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

	public int getSize() {
		return this.size;
	}

	public int getBits() {
		return this.bits;
	}

	public void getAll(IntConsumer intConsumer) {
		int i = this.data.length;
		if (i != 0) {
			int j = 0;
			long l = this.data[0];
			long m = i > 1 ? this.data[1] : 0L;

			for (int k = 0; k < this.size; k++) {
				int n = k * this.bits;
				int o = n >> 6;
				int p = (k + 1) * this.bits - 1 >> 6;
				int q = n ^ o << 6;
				if (o != j) {
					l = m;
					m = o + 1 < i ? this.data[o + 1] : 0L;
					j = o;
				}

				if (o == p) {
					intConsumer.accept((int)(l >>> q & this.mask));
				} else {
					int r = 64 - q;
					intConsumer.accept((int)((l >>> q | m << r) & this.mask));
				}
			}
		}
	}
}
