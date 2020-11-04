/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;

public class CubePointRange
extends AbstractDoubleList {
    private final int parts;

    CubePointRange(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("Need at least 1 part");
        }
        this.parts = i;
    }

    @Override
    public double getDouble(int i) {
        return (double)i / (double)this.parts;
    }

    @Override
    public int size() {
        return this.parts + 1;
    }
}

