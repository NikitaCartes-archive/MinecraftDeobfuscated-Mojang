/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum SmoothLayer implements CastleTransformer
{
    INSTANCE;


    @Override
    public int apply(Context context, int i, int j, int k, int l, int m) {
        boolean bl2;
        boolean bl = j == l;
        boolean bl3 = bl2 = i == k;
        if (bl == bl2) {
            if (bl) {
                return context.nextRandom(2) == 0 ? l : i;
            }
            return m;
        }
        return bl ? l : i;
    }
}

