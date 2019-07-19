/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import net.minecraft.core.Position;

public class PositionImpl
implements Position {
    protected final double x;
    protected final double y;
    protected final double z;

    public PositionImpl(double d, double e, double f) {
        this.x = d;
        this.y = e;
        this.z = f;
    }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    @Override
    public double z() {
        return this.z;
    }
}

