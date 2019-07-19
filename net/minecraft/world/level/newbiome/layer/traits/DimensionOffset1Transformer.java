/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer;

public interface DimensionOffset1Transformer
extends DimensionTransformer {
    @Override
    default public int getParentX(int i) {
        return i - 1;
    }

    @Override
    default public int getParentY(int i) {
        return i - 1;
    }
}

