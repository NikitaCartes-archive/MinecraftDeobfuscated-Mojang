/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.layer.LayerBiomes;

public interface DimensionTransformer
extends LayerBiomes {
    public int getParentX(int var1);

    public int getParentY(int var1);
}

