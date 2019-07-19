/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

public enum BlockLayer {
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String name;

    private BlockLayer(String string2) {
        this.name = string2;
    }

    public String toString() {
        return this.name;
    }
}

