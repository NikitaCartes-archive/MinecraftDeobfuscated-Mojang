/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum BambooLeaves implements StringRepresentable
{
    NONE("none"),
    SMALL("small"),
    LARGE("large");

    private final String name;

    private BambooLeaves(String string2) {
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

