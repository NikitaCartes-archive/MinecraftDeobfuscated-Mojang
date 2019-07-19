/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum DoubleBlockHalf implements StringRepresentable
{
    UPPER,
    LOWER;


    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this == UPPER ? "upper" : "lower";
    }
}

