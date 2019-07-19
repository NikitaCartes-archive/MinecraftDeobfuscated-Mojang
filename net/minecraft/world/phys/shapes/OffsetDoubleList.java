/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList
extends AbstractDoubleList {
    private final DoubleList delegate;
    private final double offset;

    public OffsetDoubleList(DoubleList doubleList, double d) {
        this.delegate = doubleList;
        this.offset = d;
    }

    @Override
    public double getDouble(int i) {
        return this.delegate.getDouble(i) + this.offset;
    }

    @Override
    public int size() {
        return this.delegate.size();
    }
}

