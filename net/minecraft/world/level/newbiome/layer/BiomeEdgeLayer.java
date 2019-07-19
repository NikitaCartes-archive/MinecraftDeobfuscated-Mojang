/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer
{
    INSTANCE;

    private static final int DESERT;
    private static final int MOUNTAINS;
    private static final int WOODED_MOUNTAINS;
    private static final int SNOWY_TUNDRA;
    private static final int JUNGLE;
    private static final int BAMBOO_JUNGLE;
    private static final int JUNGLE_EDGE;
    private static final int BADLANDS;
    private static final int BADLANDS_PLATEAU;
    private static final int WOODED_BADLANDS_PLATEAU;
    private static final int PLAINS;
    private static final int GIANT_TREE_TAIGA;
    private static final int MOUNTAIN_EDGE;
    private static final int SWAMP;
    private static final int TAIGA;
    private static final int SNOWY_TAIGA;

    @Override
    public int apply(Context context, int i, int j, int k, int l, int m) {
        int[] is = new int[1];
        if (this.checkEdge(is, i, j, k, l, m, MOUNTAINS, MOUNTAIN_EDGE) || this.checkEdgeStrict(is, i, j, k, l, m, WOODED_BADLANDS_PLATEAU, BADLANDS) || this.checkEdgeStrict(is, i, j, k, l, m, BADLANDS_PLATEAU, BADLANDS) || this.checkEdgeStrict(is, i, j, k, l, m, GIANT_TREE_TAIGA, TAIGA)) {
            return is[0];
        }
        if (m == DESERT && (i == SNOWY_TUNDRA || j == SNOWY_TUNDRA || l == SNOWY_TUNDRA || k == SNOWY_TUNDRA)) {
            return WOODED_MOUNTAINS;
        }
        if (m == SWAMP) {
            if (i == DESERT || j == DESERT || l == DESERT || k == DESERT || i == SNOWY_TAIGA || j == SNOWY_TAIGA || l == SNOWY_TAIGA || k == SNOWY_TAIGA || i == SNOWY_TUNDRA || j == SNOWY_TUNDRA || l == SNOWY_TUNDRA || k == SNOWY_TUNDRA) {
                return PLAINS;
            }
            if (i == JUNGLE || k == JUNGLE || j == JUNGLE || l == JUNGLE || i == BAMBOO_JUNGLE || k == BAMBOO_JUNGLE || j == BAMBOO_JUNGLE || l == BAMBOO_JUNGLE) {
                return JUNGLE_EDGE;
            }
        }
        return m;
    }

    private boolean checkEdge(int[] is, int i, int j, int k, int l, int m, int n, int o) {
        if (!Layers.isSame(m, n)) {
            return false;
        }
        is[0] = this.isValidTemperatureEdge(i, n) && this.isValidTemperatureEdge(j, n) && this.isValidTemperatureEdge(l, n) && this.isValidTemperatureEdge(k, n) ? m : o;
        return true;
    }

    private boolean checkEdgeStrict(int[] is, int i, int j, int k, int l, int m, int n, int o) {
        if (m != n) {
            return false;
        }
        is[0] = Layers.isSame(i, n) && Layers.isSame(j, n) && Layers.isSame(l, n) && Layers.isSame(k, n) ? m : o;
        return true;
    }

    private boolean isValidTemperatureEdge(int i, int j) {
        if (Layers.isSame(i, j)) {
            return true;
        }
        Biome biome = (Biome)Registry.BIOME.byId(i);
        Biome biome2 = (Biome)Registry.BIOME.byId(j);
        if (biome != null && biome2 != null) {
            Biome.BiomeTempCategory biomeTempCategory2;
            Biome.BiomeTempCategory biomeTempCategory = biome.getTemperatureCategory();
            return biomeTempCategory == (biomeTempCategory2 = biome2.getTemperatureCategory()) || biomeTempCategory == Biome.BiomeTempCategory.MEDIUM || biomeTempCategory2 == Biome.BiomeTempCategory.MEDIUM;
        }
        return false;
    }

    static {
        DESERT = Registry.BIOME.getId(Biomes.DESERT);
        MOUNTAINS = Registry.BIOME.getId(Biomes.MOUNTAINS);
        WOODED_MOUNTAINS = Registry.BIOME.getId(Biomes.WOODED_MOUNTAINS);
        SNOWY_TUNDRA = Registry.BIOME.getId(Biomes.SNOWY_TUNDRA);
        JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
        BAMBOO_JUNGLE = Registry.BIOME.getId(Biomes.BAMBOO_JUNGLE);
        JUNGLE_EDGE = Registry.BIOME.getId(Biomes.JUNGLE_EDGE);
        BADLANDS = Registry.BIOME.getId(Biomes.BADLANDS);
        BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.BADLANDS_PLATEAU);
        WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
        PLAINS = Registry.BIOME.getId(Biomes.PLAINS);
        GIANT_TREE_TAIGA = Registry.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
        MOUNTAIN_EDGE = Registry.BIOME.getId(Biomes.MOUNTAIN_EDGE);
        SWAMP = Registry.BIOME.getId(Biomes.SWAMP);
        TAIGA = Registry.BIOME.getId(Biomes.TAIGA);
        SNOWY_TAIGA = Registry.BIOME.getId(Biomes.SNOWY_TAIGA);
    }
}

