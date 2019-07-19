/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.phys.shapes.IndexMerger;

public final class IndirectMerger
implements IndexMerger {
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
            double e;
            boolean bl4;
            boolean bl3 = i < k;
            boolean bl5 = bl4 = j < l;
            if (!bl3 && !bl4) break;
            boolean bl52 = bl3 && (!bl4 || doubleList.getDouble(i) < doubleList2.getDouble(j) + 1.0E-7);
            double d2 = e = bl52 ? doubleList.getDouble(i++) : doubleList2.getDouble(j++);
            if ((i == 0 || !bl3) && !bl52 && !bl2 || (j == 0 || !bl4) && bl52 && !bl) continue;
            if (!(d >= e - 1.0E-7)) {
                this.firstIndices.add(i - 1);
                this.secondIndices.add(j - 1);
                this.result.add(e);
                d = e;
                continue;
            }
            if (this.result.isEmpty()) continue;
            this.firstIndices.set(this.firstIndices.size() - 1, i - 1);
            this.secondIndices.set(this.secondIndices.size() - 1, j - 1);
        }
        if (this.result.isEmpty()) {
            this.result.add(Math.min(doubleList.getDouble(k - 1), doubleList2.getDouble(l - 1)));
        }
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        for (int i = 0; i < this.result.size() - 1; ++i) {
            if (indexConsumer.merge(this.firstIndices.getInt(i), this.secondIndices.getInt(i), i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public DoubleList getList() {
        return this.result;
    }
}

