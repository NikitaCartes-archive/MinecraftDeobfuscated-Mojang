/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum RiverLayer implements CastleTransformer
{
    INSTANCE;

    public static final int RIVER;

    @Override
    public int apply(Context context, int i, int j, int k, int l, int m) {
        int n = RiverLayer.riverFilter(m);
        if (n == RiverLayer.riverFilter(l) && n == RiverLayer.riverFilter(i) && n == RiverLayer.riverFilter(j) && n == RiverLayer.riverFilter(k)) {
            return -1;
        }
        return RIVER;
    }

    private static int riverFilter(int i) {
        if (i >= 2) {
            return 2 + (i & 1);
        }
        return i;
    }

    static {
        RIVER = Registry.BIOME.getId(Biomes.RIVER);
    }
}

