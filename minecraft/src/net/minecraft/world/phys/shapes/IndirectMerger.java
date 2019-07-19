package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class IndirectMerger implements IndexMerger {
	private final DoubleArrayList result;
	private final IntArrayList firstIndices;
	private final IntArrayList secondIndices;

	protected IndirectMerger(DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
		int i = 0;
		int j = 0;
		double d = Double.NaN;
		int k = doubleList.size();
		int l = doubleList2.size();
		int m = k + l;
		this.result = new DoubleArrayList(m);
		this.firstIndices = new IntArrayList(m);
		this.secondIndices = new IntArrayList(m);

		while (true) {
			boolean bl3 = i < k;
			boolean bl4 = j < l;
			if (!bl3 && !bl4) {
				if (this.result.isEmpty()) {
					this.result.add(Math.min(doubleList.getDouble(k - 1), doubleList2.getDouble(l - 1)));
				}

				return;
			}

			boolean bl5 = bl3 && (!bl4 || doubleList.getDouble(i) < doubleList2.getDouble(j) + 1.0E-7);
			double e = bl5 ? doubleList.getDouble(i++) : doubleList2.getDouble(j++);
			if ((i != 0 && bl3 || bl5 || bl2) && (j != 0 && bl4 || !bl5 || bl)) {
				if (!(d >= e - 1.0E-7)) {
					this.firstIndices.add(i - 1);
					this.secondIndices.add(j - 1);
					this.result.add(e);
					d = e;
				} else if (!this.result.isEmpty()) {
					this.firstIndices.set(this.firstIndices.size() - 1, i - 1);
					this.secondIndices.set(this.secondIndices.size() - 1, j - 1);
				}
			}
		}
	}

	@Override
	public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
		for (int i = 0; i < this.result.size() - 1; i++) {
			if (!indexConsumer.merge(this.firstIndices.getInt(i), this.secondIndices.getInt(i), i)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public DoubleList getList() {
		return this.result;
	}
}
