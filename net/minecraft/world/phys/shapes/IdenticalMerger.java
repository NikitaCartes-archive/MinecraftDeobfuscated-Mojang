/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

public class IdenticalMerger
implements IndexMerger {
    private final DoubleList coords;

    public IdenticalMerger(DoubleList doubleList) {
        this.coords = doubleList;
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        for (int i = 0; i <= this.coords.size(); ++i) {
            if (indexConsumer.merge(i, i, i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public DoubleList getList() {
        return this.coords;
    }
}

