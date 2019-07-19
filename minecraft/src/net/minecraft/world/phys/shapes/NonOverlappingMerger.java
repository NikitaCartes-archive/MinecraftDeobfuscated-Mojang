package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class NonOverlappingMerger extends AbstractDoubleList implements IndexMerger {
	private final DoubleList lower;
	private final DoubleList upper;
	private final boolean swap;

	public NonOverlappingMerger(DoubleList doubleList, DoubleList doubleList2, boolean bl) {
		this.lower = doubleList;
		this.upper = doubleList2;
		this.swap = bl;
	}

	public int size() {
		return this.lower.size() + this.upper.size();
	}

	@Override
	public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
		return this.swap ? this.forNonSwappedIndexes((i, j, k) -> indexConsumer.merge(j, i, k)) : this.forNonSwappedIndexes(indexConsumer);
	}

	private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer indexConsumer) {
		int i = this.lower.size() - 1;

		for (int j = 0; j < i; j++) {
			if (!indexConsumer.merge(j, -1, j)) {
				return false;
			}
		}

		if (!indexConsumer.merge(i, -1, i)) {
			return false;
		} else {
			for (int jx = 0; jx < this.upper.size(); jx++) {
				if (!indexConsumer.merge(i, jx, i + 1 + jx)) {
					return false;
				}
			}

			return true;
		}
	}

	@Override
	public double getDouble(int i) {
		return i < this.lower.size() ? this.lower.getDouble(i) : this.upper.getDouble(i - this.lower.size());
	}

	@Override
	public DoubleList getList() {
		return this;
	}
}
