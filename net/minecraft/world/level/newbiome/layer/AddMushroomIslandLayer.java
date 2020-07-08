/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddMushroomIslandLayer implements BishopTransformer
{
    INSTANCE;

    private static final int MUSHROOM_FIELDS;

    @Override
    public int apply(Context context, int i, int j, int k, int l, int m) {
        if (Layers.isShallowOcean(m) && Layers.isShallowOcean(l) && Layers.isShallowOcean(i) && Layers.isShallowOcean(k) && Layers.isShallowOcean(j) && context.nextRandom(100) == 0) {
            return MUSHROOM_FIELDS;
        }
        return m;
    }

    static {
        MUSHROOM_FIELDS = BuiltinRegistries.BIOME.getId(Biomes.MUSHROOM_FIELDS);
    }
}

