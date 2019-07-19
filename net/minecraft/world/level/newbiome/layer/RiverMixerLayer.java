/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum RiverMixerLayer implements AreaTransformer2,
DimensionOffset0Transformer
{
    INSTANCE;

    private static final int FROZEN_RIVER;
    private static final int SNOWY_TUNDRA;
    private static final int MUSHROOM_FIELDS;
    private static final int MUSHROOM_FIELD_SHORE;
    private static final int RIVER;

    @Override
    public int applyPixel(Context context, Area area, Area area2, int i, int j) {
        int k = area.get(this.getParentX(i), this.getParentY(j));
        int l = area2.get(this.getParentX(i), this.getParentY(j));
        if (Layers.isOcean(k)) {
            return k;
        }
        if (l == RIVER) {
            if (k == SNOWY_TUNDRA) {
                return FROZEN_RIVER;
            }
            if (k == MUSHROOM_FIELDS || k == MUSHROOM_FIELD_SHORE) {
                return MUSHROOM_FIELD_SHORE;
            }
            return l & 0xFF;
        }
        return k;
    }

    static {
        FROZEN_RIVER = Registry.BIOME.getId(Biomes.FROZEN_RIVER);
        SNOWY_TUNDRA = Registry.BIOME.getId(Biomes.SNOWY_TUNDRA);
        MUSHROOM_FIELDS = Registry.BIOME.getId(Biomes.MUSHROOM_FIELDS);
        MUSHROOM_FIELD_SHORE = Registry.BIOME.getId(Biomes.MUSHROOM_FIELD_SHORE);
        RIVER = Registry.BIOME.getId(Biomes.RIVER);
    }
}

