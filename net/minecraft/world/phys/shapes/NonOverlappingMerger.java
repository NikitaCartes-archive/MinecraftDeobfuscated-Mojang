/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

public class NonOverlappingMerger
extends AbstractDoubleList
implements IndexMerger {
    private final DoubleList lower;
    private final DoubleList upper;
    private final boolean swap;

    public NonOverlappingMerger(DoubleList doubleList, DoubleList doubleList2, boolean bl) {
        this.lower = doubleList;
        this.upper = doubleList2;
        this.swap = bl;
    }

    @Override
    public int size() {
        return this.lower.size() + this.upper.size();
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        if (this.swap) {
            return this.forNonSwappedIndexes((i, j, k) -> indexConsumer.merge(j, i, k));
        }
        return this.forNonSwappedIndexes(indexConsumer);
    }

    private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int j;
        int i = this.lower.size() - 1;
        for (j = 0; j < i; ++j) {
            if (indexConsumer.merge(j, -1, j)) continue;
            return false;
        }
        if (!indexConsumer.merge(i, -1, i)) {
            return false;
        }
        for (j = 0; j < this.upper.size(); ++j) {
            if (indexConsumer.merge(i, j, i + 1 + j)) continue;
            return false;
        }
        return true;
    }

    @Override
    public double getDouble(int i) {
        if (i < this.lower.size()) {
            return this.lower.getDouble(i);
        }
        return this.upper.getDouble(i - this.lower.size());
    }

    @Override
    public DoubleList getList() {
        return this;
    }
}

