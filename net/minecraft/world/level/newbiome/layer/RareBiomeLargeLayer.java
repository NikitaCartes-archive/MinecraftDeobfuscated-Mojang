/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeLargeLayer implements C1Transformer
{
    INSTANCE;

    private static final int JUNGLE;
    private static final int BAMBOO_JUNGLE;

    @Override
    public int apply(Context context, int i) {
        if (context.nextRandom(10) == 0 && i == JUNGLE) {
            return BAMBOO_JUNGLE;
        }
        return i;
    }

    static {
        JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
        BAMBOO_JUNGLE = Registry.BIOME.getId(Biomes.BAMBOO_JUNGLE);
    }
}

