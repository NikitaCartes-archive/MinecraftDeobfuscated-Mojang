/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import java.util.function.LongFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.AddDeepOceanLayer;
import net.minecraft.world.level.newbiome.layer.AddEdgeLayer;
import net.minecraft.world.level.newbiome.layer.AddIslandLayer;
import net.minecraft.world.level.newbiome.layer.AddMushroomIslandLayer;
import net.minecraft.world.level.newbiome.layer.AddSnowLayer;
import net.minecraft.world.level.newbiome.layer.BiomeEdgeLayer;
import net.minecraft.world.level.newbiome.layer.BiomeInitLayer;
import net.minecraft.world.level.newbiome.layer.IslandLayer;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.OceanLayer;
import net.minecraft.world.level.newbiome.layer.OceanMixerLayer;
import net.minecraft.world.level.newbiome.layer.RareBiomeLargeLayer;
import net.minecraft.world.level.newbiome.layer.RareBiomeSpotLayer;
import net.minecraft.world.level.newbiome.layer.RegionHillsLayer;
import net.minecraft.world.level.newbiome.layer.RemoveTooMuchOceanLayer;
import net.minecraft.world.level.newbiome.layer.RiverInitLayer;
import net.minecraft.world.level.newbiome.layer.RiverLayer;
import net.minecraft.world.level.newbiome.layer.RiverMixerLayer;
import net.minecraft.world.level.newbiome.layer.ShoreLayer;
import net.minecraft.world.level.newbiome.layer.SmoothLayer;
import net.minecraft.world.level.newbiome.layer.ZoomLayer;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public class Layers {
    protected static final int WARM_OCEAN = Registry.BIOME.getId(Biomes.WARM_OCEAN);
    protected static final int LUKEWARM_OCEAN = Registry.BIOME.getId(Biomes.LUKEWARM_OCEAN);
    protected static final int OCEAN = Registry.BIOME.getId(Biomes.OCEAN);
    protected static final int COLD_OCEAN = Registry.BIOME.getId(Biomes.COLD_OCEAN);
    protected static final int FROZEN_OCEAN = Registry.BIOME.getId(Biomes.FROZEN_OCEAN);
    protected static final int DEEP_WARM_OCEAN = Registry.BIOME.getId(Biomes.DEEP_WARM_OCEAN);
    protected static final int DEEP_LUKEWARM_OCEAN = Registry.BIOME.getId(Biomes.DEEP_LUKEWARM_OCEAN);
    protected static final int DEEP_OCEAN = Registry.BIOME.getId(Biomes.DEEP_OCEAN);
    protected static final int DEEP_COLD_OCEAN = Registry.BIOME.getId(Biomes.DEEP_COLD_OCEAN);
    protected static final int DEEP_FROZEN_OCEAN = Registry.BIOME.getId(Biomes.DEEP_FROZEN_OCEAN);

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> zoom(long l, AreaTransformer1 areaTransformer1, AreaFactory<T> areaFactory, int i, LongFunction<C> longFunction) {
        AreaFactory<T> areaFactory2 = areaFactory;
        for (int j = 0; j < i; ++j) {
            areaFactory2 = areaTransformer1.run((BigContext)longFunction.apply(l + (long)j), areaFactory2);
        }
        return areaFactory2;
    }

    public static <T extends Area, C extends BigContext<T>> AreaFactory<T> getDefaultLayer(LevelType levelType, OverworldGeneratorSettings overworldGeneratorSettings, LongFunction<C> longFunction) {
        AreaFactory areaFactory = IslandLayer.INSTANCE.run((BigContext)longFunction.apply(1L));
        areaFactory = ZoomLayer.FUZZY.run((BigContext)longFunction.apply(2000L), areaFactory);
        areaFactory = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(1L), areaFactory);
        areaFactory = ZoomLayer.NORMAL.run((BigContext)longFunction.apply(2001L), areaFactory);
        areaFactory = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(2L), areaFactory);
        areaFactory = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(50L), areaFactory);
        areaFactory = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(70L), areaFactory);
        areaFactory = RemoveTooMuchOceanLayer.INSTANCE.run((BigContext)longFunction.apply(2L), areaFactory);
        AreaFactory areaFactory2 = OceanLayer.INSTANCE.run((BigContext)longFunction.apply(2L));
        areaFactory2 = Layers.zoom(2001L, ZoomLayer.NORMAL, areaFactory2, 6, longFunction);
        areaFactory = AddSnowLayer.INSTANCE.run((BigContext)longFunction.apply(2L), areaFactory);
        areaFactory = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(3L), areaFactory);
        areaFactory = AddEdgeLayer.CoolWarm.INSTANCE.run((BigContext)longFunction.apply(2L), areaFactory);
        areaFactory = AddEdgeLayer.HeatIce.INSTANCE.run((BigContext)longFunction.apply(2L), areaFactory);
        areaFactory = AddEdgeLayer.IntroduceSpecial.INSTANCE.run((BigContext)longFunction.apply(3L), areaFactory);
        areaFactory = ZoomLayer.NORMAL.run((BigContext)longFunction.apply(2002L), areaFactory);
        areaFactory = ZoomLayer.NORMAL.run((BigContext)longFunction.apply(2003L), areaFactory);
        areaFactory = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(4L), areaFactory);
        areaFactory = AddMushroomIslandLayer.INSTANCE.run((BigContext)longFunction.apply(5L), areaFactory);
        areaFactory = AddDeepOceanLayer.INSTANCE.run((BigContext)longFunction.apply(4L), areaFactory);
        areaFactory = Layers.zoom(1000L, ZoomLayer.NORMAL, areaFactory, 0, longFunction);
        int i = levelType == LevelType.LARGE_BIOMES ? 6 : overworldGeneratorSettings.getBiomeSize();
        int j = overworldGeneratorSettings.getRiverSize();
        AreaFactory areaFactory3 = areaFactory;
        areaFactory3 = Layers.zoom(1000L, ZoomLayer.NORMAL, areaFactory3, 0, longFunction);
        areaFactory3 = RiverInitLayer.INSTANCE.run((BigContext)longFunction.apply(100L), areaFactory3);
        AreaFactory areaFactory4 = areaFactory;
        areaFactory4 = new BiomeInitLayer(levelType, overworldGeneratorSettings.getFixedBiome()).run((BigContext)longFunction.apply(200L), areaFactory4);
        areaFactory4 = RareBiomeLargeLayer.INSTANCE.run((BigContext)longFunction.apply(1001L), areaFactory4);
        areaFactory4 = Layers.zoom(1000L, ZoomLayer.NORMAL, areaFactory4, 2, longFunction);
        areaFactory4 = BiomeEdgeLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4);
        AreaFactory areaFactory5 = areaFactory3;
        areaFactory5 = Layers.zoom(1000L, ZoomLayer.NORMAL, areaFactory5, 2, longFunction);
        areaFactory4 = RegionHillsLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4, areaFactory5);
        areaFactory3 = Layers.zoom(1000L, ZoomLayer.NORMAL, areaFactory3, 2, longFunction);
        areaFactory3 = Layers.zoom(1000L, ZoomLayer.NORMAL, areaFactory3, j, longFunction);
        areaFactory3 = RiverLayer.INSTANCE.run((BigContext)longFunction.apply(1L), areaFactory3);
        areaFactory3 = SmoothLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory3);
        areaFactory4 = RareBiomeSpotLayer.INSTANCE.run((BigContext)longFunction.apply(1001L), areaFactory4);
        for (int k = 0; k < i; ++k) {
            areaFactory4 = ZoomLayer.NORMAL.run((BigContext)longFunction.apply(1000 + k), areaFactory4);
            if (k == 0) {
                areaFactory4 = AddIslandLayer.INSTANCE.run((BigContext)longFunction.apply(3L), areaFactory4);
            }
            if (k != 1 && i != 1) continue;
            areaFactory4 = ShoreLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4);
        }
        areaFactory4 = SmoothLayer.INSTANCE.run((BigContext)longFunction.apply(1000L), areaFactory4);
        areaFactory4 = RiverMixerLayer.INSTANCE.run((BigContext)longFunction.apply(100L), areaFactory4, areaFactory3);
        areaFactory4 = OceanMixerLayer.INSTANCE.run((BigContext)longFunction.apply(100L), areaFactory4, areaFactory2);
        return areaFactory4;
    }

    public static Layer getDefaultLayer(long l, LevelType levelType, OverworldGeneratorSettings overworldGeneratorSettings) {
        int i = 25;
        AreaFactory<LazyArea> areaFactory = Layers.getDefaultLayer(levelType, overworldGeneratorSettings, (long m) -> new LazyAreaContext(25, l, m));
        return new Layer(areaFactory);
    }

    public static boolean isSame(int i, int j) {
        if (i == j) {
            return true;
        }
        Biome biome = (Biome)Registry.BIOME.byId(i);
        Biome biome2 = (Biome)Registry.BIOME.byId(j);
        if (biome == null || biome2 == null) {
            return false;
        }
        if (biome == Biomes.WOODED_BADLANDS_PLATEAU || biome == Biomes.BADLANDS_PLATEAU) {
            return biome2 == Biomes.WOODED_BADLANDS_PLATEAU || biome2 == Biomes.BADLANDS_PLATEAU;
        }
        if (biome.getBiomeCategory() != Biome.BiomeCategory.NONE && biome2.getBiomeCategory() != Biome.BiomeCategory.NONE && biome.getBiomeCategory() == biome2.getBiomeCategory()) {
            return true;
        }
        return biome == biome2;
    }

    protected static boolean isOcean(int i) {
        return i == WARM_OCEAN || i == LUKEWARM_OCEAN || i == OCEAN || i == COLD_OCEAN || i == FROZEN_OCEAN || i == DEEP_WARM_OCEAN || i == DEEP_LUKEWARM_OCEAN || i == DEEP_OCEAN || i == DEEP_COLD_OCEAN || i == DEEP_FROZEN_OCEAN;
    }

    protected static boolean isShallowOcean(int i) {
        return i == WARM_OCEAN || i == LUKEWARM_OCEAN || i == OCEAN || i == COLD_OCEAN || i == FROZEN_OCEAN;
    }
}

