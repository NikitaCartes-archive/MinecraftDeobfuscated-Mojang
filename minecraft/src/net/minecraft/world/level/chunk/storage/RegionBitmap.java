package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;

public class RegionBitmap {
	private final BitSet used = new BitSet();

	public void force(int i, int j) {
		this.used.set(i, i + j);
	}

	public void free(int i, int j) {
		this.used.clear(i, i + j);
	}

	public int allocate(int i) {
		int j = 0;

		while (true) {
			int k = this.used.nextClearBit(j);
			int l = this.used.nextSetBit(k);
			if (l == -1 || l - k >= i) {
				this.force(k, i);
				return k;
			}

			j = l;
		}
	}

	@VisibleForTesting
	public IntSet getUsed() {
		return (IntSet)this.used.stream().collect(IntArraySet::new, IntCollection::add, IntCollection::addAll);
	}
}
