package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.Mth;

public class SpatialLongSet extends LongLinkedOpenHashSet {
	private final SpatialLongSet.InternalMap map;

	public SpatialLongSet(int i, float f) {
		super(i, f);
		this.map = new SpatialLongSet.InternalMap(i / 64, f);
	}

	@Override
	public boolean add(long l) {
		return this.map.addBit(l);
	}

	@Override
	public boolean rem(long l) {
		return this.map.removeBit(l);
	}

	@Override
	public long removeFirstLong() {
		return this.map.removeFirstBit();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	protected static class InternalMap extends Long2LongLinkedOpenHashMap {
		private static final int X_BITS = Mth.log2(60000000);
		private static final int Z_BITS = Mth.log2(60000000);
		private static final int Y_BITS = 64 - X_BITS - Z_BITS;
		private static final int Y_OFFSET = 0;
		private static final int Z_OFFSET = Y_BITS;
		private static final int X_OFFSET = Y_BITS + Z_BITS;
		private static final long OUTER_MASK = 3L << X_OFFSET | 3L | 3L << Z_OFFSET;
		private int lastPos = -1;
		private long lastOuterKey;
		private final int minSize;

		public InternalMap(int i, float f) {
			super(i, f);
			this.minSize = i;
		}

		static long getOuterKey(long l) {
			return l & ~OUTER_MASK;
		}

		static int getInnerKey(long l) {
			int i = (int)(l >>> X_OFFSET & 3L);
			int j = (int)(l >>> 0 & 3L);
			int k = (int)(l >>> Z_OFFSET & 3L);
			return i << 4 | k << 2 | j;
		}

		static long getFullKey(long l, int i) {
			l |= (long)(i >>> 4 & 3) << X_OFFSET;
			l |= (long)(i >>> 2 & 3) << Z_OFFSET;
			return l | (long)(i >>> 0 & 3) << 0;
		}

		public boolean addBit(long l) {
			long m = getOuterKey(l);
			int i = getInnerKey(l);
			long n = 1L << i;
			int j;
			if (m == 0L) {
				if (this.containsNullKey) {
					return this.replaceBit(this.n, n);
				}

				this.containsNullKey = true;
				j = this.n;
			} else {
				if (this.lastPos != -1 && m == this.lastOuterKey) {
					return this.replaceBit(this.lastPos, n);
				}

				long[] ls = this.key;
				j = (int)HashCommon.mix(m) & this.mask;

				for (long o = ls[j]; o != 0L; o = ls[j]) {
					if (o == m) {
						this.lastPos = j;
						this.lastOuterKey = m;
						return this.replaceBit(j, n);
					}

					j = j + 1 & this.mask;
				}
			}

			this.key[j] = m;
			this.value[j] = n;
			if (this.size == 0) {
				this.first = this.last = j;
				this.link[j] = -1L;
			} else {
				this.link[this.last] = this.link[this.last] ^ (this.link[this.last] ^ (long)j & 4294967295L) & 4294967295L;
				this.link[j] = ((long)this.last & 4294967295L) << 32 | 4294967295L;
				this.last = j;
			}

			if (this.size++ >= this.maxFill) {
				this.rehash(HashCommon.arraySize(this.size + 1, this.f));
			}

			return false;
		}

		private boolean replaceBit(int i, long l) {
			boolean bl = (this.value[i] & l) != 0L;
			this.value[i] = this.value[i] | l;
			return bl;
		}

		public boolean removeBit(long l) {
			long m = getOuterKey(l);
			int i = getInnerKey(l);
			long n = 1L << i;
			if (m == 0L) {
				return this.containsNullKey ? this.removeFromNullEntry(n) : false;
			} else if (this.lastPos != -1 && m == this.lastOuterKey) {
				return this.removeFromEntry(this.lastPos, n);
			} else {
				long[] ls = this.key;
				int j = (int)HashCommon.mix(m) & this.mask;

				for (long o = ls[j]; o != 0L; o = ls[j]) {
					if (m == o) {
						this.lastPos = j;
						this.lastOuterKey = m;
						return this.removeFromEntry(j, n);
					}

					j = j + 1 & this.mask;
				}

				return false;
			}
		}

		private boolean removeFromNullEntry(long l) {
			if ((this.value[this.n] & l) == 0L) {
				return false;
			} else {
				this.value[this.n] = this.value[this.n] & ~l;
				if (this.value[this.n] != 0L) {
					return true;
				} else {
					this.containsNullKey = false;
					this.size--;
					this.fixPointers(this.n);
					if (this.size < this.maxFill / 4 && this.n > 16) {
						this.rehash(this.n / 2);
					}

					return true;
				}
			}
		}

		private boolean removeFromEntry(int i, long l) {
			if ((this.value[i] & l) == 0L) {
				return false;
			} else {
				this.value[i] = this.value[i] & ~l;
				if (this.value[i] != 0L) {
					return true;
				} else {
					this.lastPos = -1;
					this.size--;
					this.fixPointers(i);
					this.shiftKeys(i);
					if (this.size < this.maxFill / 4 && this.n > 16) {
						this.rehash(this.n / 2);
					}

					return true;
				}
			}
		}

		public long removeFirstBit() {
			if (this.size == 0) {
				throw new NoSuchElementException();
			} else {
				int i = this.first;
				long l = this.key[i];
				int j = Long.numberOfTrailingZeros(this.value[i]);
				this.value[i] = this.value[i] & ~(1L << j);
				if (this.value[i] == 0L) {
					this.removeFirstLong();
					this.lastPos = -1;
				}

				return getFullKey(l, j);
			}
		}

		@Override
		protected void rehash(int i) {
			if (i > this.minSize) {
				super.rehash(i);
			}
		}
	}
}
