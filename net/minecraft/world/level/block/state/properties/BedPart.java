/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum BedPart implements StringRepresentable
{
    HEAD("head"),
    FOOT("foot");

    private final String name;

    private BedPart(String string2) {
        this.name = string2;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}

