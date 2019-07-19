/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeSpotLayer implements C1Transformer
{
    INSTANCE;

    private static final int PLAINS;
    private static final int SUNFLOWER_PLAINS;

    @Override
    public int apply(Context context, int i) {
        if (context.nextRandom(57) == 0 && i == PLAINS) {
            return SUNFLOWER_PLAINS;
        }
        return i;
    }

    static {
        PLAINS = Registry.BIOME.getId(Biomes.PLAINS);
        SUNFLOWER_PLAINS = Registry.BIOME.getId(Biomes.SUNFLOWER_PLAINS);
    }
}

