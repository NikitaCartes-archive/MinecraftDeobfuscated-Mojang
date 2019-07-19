/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

public enum LightLayer {
    SKY(15),
    BLOCK(0);

    public final int surrounding;

    private LightLayer(int j) {
        this.surrounding = j;
    }
}

