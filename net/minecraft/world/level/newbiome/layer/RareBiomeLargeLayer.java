/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeLargeLayer implements C1Transformer
{
    INSTANCE;


    @Override
    public int apply(Context context, int i) {
        if (context.nextRandom(10) == 0 && i == 21) {
            return 168;
        }
        return i;
    }
}

