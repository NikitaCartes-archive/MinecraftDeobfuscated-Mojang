/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.border;

public enum BorderStatus {
    GROWING(4259712),
    SHRINKING(0xFF3030),
    STATIONARY(2138367);

    private final int color;

    private BorderStatus(int j) {
        this.color = j;
    }

    public int getColor() {
        return this.color;
    }
}

