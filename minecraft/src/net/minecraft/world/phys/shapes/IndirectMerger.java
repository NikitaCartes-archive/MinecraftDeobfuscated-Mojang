package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;

public class IndirectMerger implements IndexMerger {
	private static final DoubleList EMPTY = DoubleLists.unmodifiable(DoubleArrayList.wrap(new double[]{0.0}));
	private final double[] result;
	private final int[] firstIndices;
	private final int[] secondIndices;
	private final int resultLength;

	public IndirectMerger(DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
		double d = Double.NaN;
		int i = doubleList.size();
		int j = doubleList2.size();
		int k = i + j;
		this.result = new double[k];
		this.firstIndices = new int[k];
		this.secondIndices = new int[k];
		boolean bl3 = !bl;
		boolean bl4 = !bl2;
		int l = 0;
		int m = 0;
		int n = 0;

		while (true) {
			boolean bl5 = m >= i;
			boolean bl6 = n >= j;
			if (bl5 && bl6) {
				this.resultLength = Math.max(1, l);
				return;
			}

			boolean bl7 = !bl5 && (bl6 || doubleList.getDouble(m) < doubleList2.getDouble(n) + 1.0E-7);
			if (bl7) {
				m++;
				if (bl3 && (n == 0 || bl6)) {
					continue;
				}
			} else {
				n++;
				if (bl4 && (m == 0 || bl5)) {
					continue;
				}
			}

			int o = m - 1;
			int p = n - 1;
			double e = bl7 ? doubleList.getDouble(o) : doubleList2.getDouble(p);
			if (!(d >= e - 1.0E-7)) {
				this.firstIndices[l] = o;
				this.secondIndices[l] = p;
				this.result[l] = e;
				l++;
				d = e;
			} else {
				this.firstIndices[l - 1] = o;
				this.secondIndices[l - 1] = p;
			}
		}
	}

	@Override
	public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
		int i = this.resultLength - 1;

		for (int j = 0; j < i; j++) {
			if (!indexConsumer.merge(this.firstIndices[j], this.secondIndices[j], j)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int size() {
		return this.resultLength;
	}

	@Override
	public DoubleList getList() {
		return (DoubleList)(this.resultLength <= 1 ? EMPTY : DoubleArrayList.wrap(this.result, this.resultLength));
	}
}
