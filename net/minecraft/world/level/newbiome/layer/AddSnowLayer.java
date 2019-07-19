/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum AddSnowLayer implements C1Transformer
{
    INSTANCE;


    @Override
    public int apply(Context context, int i) {
        if (Layers.isShallowOcean(i)) {
            return i;
        }
        int j = context.nextRandom(6);
        if (j == 0) {
            return 4;
        }
        if (j == 1) {
            return 3;
        }
        return 1;
    }
}

