/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import net.minecraft.world.phys.shapes.IndexMerger;

public class IndirectMerger
implements IndexMerger {
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
            double e;
            boolean bl7;
            boolean bl6;
            boolean bl5 = m >= i;
            boolean bl8 = bl6 = n >= j;
            if (bl5 && bl6) break;
            boolean bl9 = bl7 = !bl5 && (bl6 || doubleList.getDouble(m) < doubleList2.getDouble(n) + 1.0E-7);
            if (bl7) {
                ++m;
                if (bl3 && (n == 0 || bl6)) {
                    continue;
                }
            } else {
                ++n;
                if (bl4 && (m == 0 || bl5)) continue;
            }
            int o = m - 1;
            int p = n - 1;
            double d2 = e = bl7 ? doubleList.getDouble(o) : doubleList2.getDouble(p);
            if (!(d >= e - 1.0E-7)) {
                this.firstIndices[l] = o;
                this.secondIndices[l] = p;
                this.result[l] = e;
                ++l;
                d = e;
                continue;
            }
            this.firstIndices[l - 1] = o;
            this.secondIndices[l - 1] = p;
        }
        this.resultLength = Math.max(1, l);
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int i = this.resultLength - 1;
        for (int j = 0; j < i; ++j) {
            if (indexConsumer.merge(this.firstIndices[j], this.secondIndices[j], j)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.resultLength;
    }

    @Override
    public DoubleList getList() {
        return this.resultLength <= 1 ? EMPTY : DoubleArrayList.wrap(this.result, this.resultLength);
    }
}

