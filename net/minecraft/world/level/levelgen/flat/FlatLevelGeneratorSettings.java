/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.flat;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class FlatLevelGeneratorSettings
extends ChunkGeneratorSettings {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConfiguredFeature<?, ?> MINESHAFT_COMPOSITE_FEATURE = Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> VILLAGE_COMPOSITE_FEATURE = Feature.VILLAGE.configured(new JigsawConfiguration("village/plains/town_centers", 6)).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> STRONGHOLD_COMPOSITE_FEATURE = Feature.STRONGHOLD.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> SWAMPHUT_COMPOSITE_FEATURE = Feature.SWAMP_HUT.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> DESERT_PYRAMID_COMPOSITE_FEATURE = Feature.DESERT_PYRAMID.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> JUNGLE_PYRAMID_COMPOSITE_FEATURE = Feature.JUNGLE_TEMPLE.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> IGLOO_COMPOSITE_FEATURE = Feature.IGLOO.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> RUINED_PORTAL_COMPOSITE_FEATURE = Feature.RUINED_PORTAL.configured(new RuinedPortalConfiguration()).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> SHIPWRECK_COMPOSITE_FEATURE = Feature.SHIPWRECK.configured(new ShipwreckConfiguration(false)).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> OCEAN_MONUMENT_COMPOSITE_FEATURE = Feature.OCEAN_MONUMENT.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> WATER_LAKE_COMPOSITE_FEATURE = Feature.LAKE.configured(new BlockStateConfiguration(Blocks.WATER.defaultBlockState())).decorated(FeatureDecorator.WATER_LAKE.configured(new ChanceDecoratorConfiguration(4)));
    private static final ConfiguredFeature<?, ?> LAVA_LAKE_COMPOSITE_FEATURE = Feature.LAKE.configured(new BlockStateConfiguration(Blocks.LAVA.defaultBlockState())).decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)));
    private static final ConfiguredFeature<?, ?> ENDCITY_COMPOSITE_FEATURE = Feature.END_CITY.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> WOOLAND_MANSION_COMPOSITE_FEATURE = Feature.WOODLAND_MANSION.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> FORTRESS_COMPOSITE_FEATURE = Feature.NETHER_BRIDGE.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> OCEAN_RUIN_COMPOSITE_FEATURE = Feature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3f, 0.1f)).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    private static final ConfiguredFeature<?, ?> PILLAGER_OUTPOST_COMPOSITE_FEATURE = Feature.PILLAGER_OUTPOST.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE));
    public static final Map<ConfiguredFeature<?, ?>, GenerationStep.Decoration> STRUCTURE_FEATURES_STEP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(MINESHAFT_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
        hashMap.put(VILLAGE_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(STRONGHOLD_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
        hashMap.put(SWAMPHUT_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(DESERT_PYRAMID_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(JUNGLE_PYRAMID_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(IGLOO_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(RUINED_PORTAL_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(SHIPWRECK_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(OCEAN_RUIN_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(WATER_LAKE_COMPOSITE_FEATURE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
        hashMap.put(LAVA_LAKE_COMPOSITE_FEATURE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
        hashMap.put(ENDCITY_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(WOOLAND_MANSION_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(FORTRESS_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
        hashMap.put(OCEAN_MONUMENT_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        hashMap.put(PILLAGER_OUTPOST_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
    });
    public static final Map<String, ConfiguredFeature<?, ?>[]> STRUCTURE_FEATURES = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("mineshaft", new ConfiguredFeature[]{MINESHAFT_COMPOSITE_FEATURE});
        hashMap.put("village", new ConfiguredFeature[]{VILLAGE_COMPOSITE_FEATURE});
        hashMap.put("stronghold", new ConfiguredFeature[]{STRONGHOLD_COMPOSITE_FEATURE});
        hashMap.put("biome_1", new ConfiguredFeature[]{SWAMPHUT_COMPOSITE_FEATURE, DESERT_PYRAMID_COMPOSITE_FEATURE, JUNGLE_PYRAMID_COMPOSITE_FEATURE, IGLOO_COMPOSITE_FEATURE, OCEAN_RUIN_COMPOSITE_FEATURE, SHIPWRECK_COMPOSITE_FEATURE});
        hashMap.put("oceanmonument", new ConfiguredFeature[]{OCEAN_MONUMENT_COMPOSITE_FEATURE});
        hashMap.put("lake", new ConfiguredFeature[]{WATER_LAKE_COMPOSITE_FEATURE});
        hashMap.put("lava_lake", new ConfiguredFeature[]{LAVA_LAKE_COMPOSITE_FEATURE});
        hashMap.put("endcity", new ConfiguredFeature[]{ENDCITY_COMPOSITE_FEATURE});
        hashMap.put("mansion", new ConfiguredFeature[]{WOOLAND_MANSION_COMPOSITE_FEATURE});
        hashMap.put("fortress", new ConfiguredFeature[]{FORTRESS_COMPOSITE_FEATURE});
        hashMap.put("pillager_outpost", new ConfiguredFeature[]{PILLAGER_OUTPOST_COMPOSITE_FEATURE});
        hashMap.put("ruined_portal", new ConfiguredFeature[]{RUINED_PORTAL_COMPOSITE_FEATURE});
    });
    public static final Map<ConfiguredFeature<?, ?>, FeatureConfiguration> STRUCTURE_FEATURES_DEFAULT = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(MINESHAFT_COMPOSITE_FEATURE, new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL));
        hashMap.put(VILLAGE_COMPOSITE_FEATURE, new JigsawConfiguration("village/plains/town_centers", 6));
        hashMap.put(STRONGHOLD_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(SWAMPHUT_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(DESERT_PYRAMID_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(JUNGLE_PYRAMID_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(IGLOO_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(OCEAN_RUIN_COMPOSITE_FEATURE, new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3f, 0.9f));
        hashMap.put(SHIPWRECK_COMPOSITE_FEATURE, new ShipwreckConfiguration(false));
        hashMap.put(OCEAN_MONUMENT_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(ENDCITY_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(WOOLAND_MANSION_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(FORTRESS_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        hashMap.put(PILLAGER_OUTPOST_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
    });
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private final Map<String, Map<String, String>> structuresOptions = Maps.newHashMap();
    private Biome biome;
    private final BlockState[] layers = new BlockState[256];
    private boolean voidGen;
    private int seaLevel;

    @Nullable
    public static Block byString(String string) {
        try {
            ResourceLocation resourceLocation = new ResourceLocation(string);
            return Registry.BLOCK.getOptional(resourceLocation).orElse(null);
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.warn("Invalid blockstate: {}", (Object)string, (Object)illegalArgumentException);
            return null;
        }
    }

    public Biome getBiome() {
        return this.biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public Map<String, Map<String, String>> getStructuresOptions() {
        return this.structuresOptions;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public void updateLayers() {
        int i = 0;
        for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
            flatLayerInfo.setStart(i);
            i += flatLayerInfo.getHeight();
        }
        this.seaLevel = 0;
        this.voidGen = true;
        i = 0;
        for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
            for (int j = flatLayerInfo.getStart(); j < flatLayerInfo.getStart() + flatLayerInfo.getHeight(); ++j) {
                BlockState blockState = flatLayerInfo.getBlockState();
                if (blockState.getBlock() == Blocks.AIR) continue;
                this.voidGen = false;
                this.layers[j] = blockState;
            }
            if (flatLayerInfo.getBlockState().getBlock() == Blocks.AIR) {
                i += flatLayerInfo.getHeight();
                continue;
            }
            this.seaLevel += flatLayerInfo.getHeight() + i;
            i = 0;
        }
    }

    public String toString() {
        int i;
        StringBuilder stringBuilder = new StringBuilder();
        for (i = 0; i < this.layersInfo.size(); ++i) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(this.layersInfo.get(i));
        }
        stringBuilder.append(";");
        stringBuilder.append(Registry.BIOME.getKey(this.biome));
        stringBuilder.append(";");
        if (!this.structuresOptions.isEmpty()) {
            i = 0;
            for (Map.Entry<String, Map<String, String>> entry : this.structuresOptions.entrySet()) {
                if (i++ > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(entry.getKey().toLowerCase(Locale.ROOT));
                Map<String, String> map = entry.getValue();
                if (map.isEmpty()) continue;
                stringBuilder.append("(");
                int j = 0;
                for (Map.Entry<String, String> entry2 : map.entrySet()) {
                    if (j++ > 0) {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append(entry2.getKey());
                    stringBuilder.append("=");
                    stringBuilder.append(entry2.getValue());
                }
                stringBuilder.append(")");
            }
        }
        return stringBuilder.toString();
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    private static FlatLayerInfo getLayerInfoFromString(String string, int i) {
        Block block;
        int j;
        String[] strings = string.split("\\*", 2);
        if (strings.length == 2) {
            try {
                j = Math.max(Integer.parseInt(strings[0]), 0);
            } catch (NumberFormatException numberFormatException) {
                LOGGER.error("Error while parsing flat world string => {}", (Object)numberFormatException.getMessage());
                return null;
            }
        } else {
            j = 1;
        }
        int k = Math.min(i + j, 256);
        int l = k - i;
        try {
            block = FlatLevelGeneratorSettings.byString(strings[strings.length - 1]);
        } catch (Exception exception) {
            LOGGER.error("Error while parsing flat world string => {}", (Object)exception.getMessage());
            return null;
        }
        if (block == null) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)strings[strings.length - 1]);
            return null;
        }
        FlatLayerInfo flatLayerInfo = new FlatLayerInfo(l, block);
        flatLayerInfo.setStart(i);
        return flatLayerInfo;
    }

    @Environment(value=EnvType.CLIENT)
    private static List<FlatLayerInfo> getLayersInfoFromString(String string) {
        ArrayList<FlatLayerInfo> list = Lists.newArrayList();
        String[] strings = string.split(",");
        int i = 0;
        for (String string2 : strings) {
            FlatLayerInfo flatLayerInfo = FlatLevelGeneratorSettings.getLayerInfoFromString(string2, i);
            if (flatLayerInfo == null) {
                return Collections.emptyList();
            }
            list.add(flatLayerInfo);
            i += flatLayerInfo.getHeight();
        }
        return list;
    }

    @Environment(value=EnvType.CLIENT)
    public <T> Dynamic<T> toObject(DynamicOps<T> dynamicOps) {
        Object object = dynamicOps.createList(this.layersInfo.stream().map(flatLayerInfo -> dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("height"), dynamicOps.createInt(flatLayerInfo.getHeight()), dynamicOps.createString("block"), dynamicOps.createString(Registry.BLOCK.getKey(flatLayerInfo.getBlockState().getBlock()).toString())))));
        Object object2 = dynamicOps.createMap(this.structuresOptions.entrySet().stream().map(entry2 -> Pair.of(dynamicOps.createString(((String)entry2.getKey()).toLowerCase(Locale.ROOT)), dynamicOps.createMap(((Map)entry2.getValue()).entrySet().stream().map(entry -> Pair.of(dynamicOps.createString((String)entry.getKey()), dynamicOps.createString((String)entry.getValue()))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("layers"), object, dynamicOps.createString("biome"), dynamicOps.createString(Registry.BIOME.getKey(this.biome).toString()), dynamicOps.createString("structures"), object2)));
    }

    public static FlatLevelGeneratorSettings fromObject(Dynamic<?> dynamic2) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = ChunkGeneratorType.FLAT.createSettings();
        List<Pair> list = dynamic2.get("layers").asList(dynamic -> Pair.of(dynamic.get("height").asInt(1), FlatLevelGeneratorSettings.byString(dynamic.get("block").asString(""))));
        if (list.stream().anyMatch(pair -> pair.getSecond() == null)) {
            return FlatLevelGeneratorSettings.getDefault();
        }
        List list2 = list.stream().map(pair -> new FlatLayerInfo((Integer)pair.getFirst(), (Block)pair.getSecond())).collect(Collectors.toList());
        if (list2.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault();
        }
        flatLevelGeneratorSettings.getLayersInfo().addAll(list2);
        flatLevelGeneratorSettings.updateLayers();
        flatLevelGeneratorSettings.setBiome(Registry.BIOME.get(new ResourceLocation(dynamic2.get("biome").asString(""))));
        dynamic2.get("structures").flatMap(Dynamic::getMapValues).ifPresent(map -> map.keySet().forEach(dynamic -> dynamic.asString().map(string -> flatLevelGeneratorSettings.getStructuresOptions().put((String)string, Maps.newHashMap()))));
        return flatLevelGeneratorSettings;
    }

    @Environment(value=EnvType.CLIENT)
    public static FlatLevelGeneratorSettings fromString(String string) {
        Iterator<String> iterator = Splitter.on(';').split(string).iterator();
        if (!iterator.hasNext()) {
            return FlatLevelGeneratorSettings.getDefault();
        }
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = ChunkGeneratorType.FLAT.createSettings();
        List<FlatLayerInfo> list = FlatLevelGeneratorSettings.getLayersInfoFromString(iterator.next());
        if (list.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault();
        }
        flatLevelGeneratorSettings.getLayersInfo().addAll(list);
        flatLevelGeneratorSettings.updateLayers();
        Biome biome = Biomes.PLAINS;
        if (iterator.hasNext()) {
            try {
                ResourceLocation resourceLocation = new ResourceLocation(iterator.next());
                biome = Registry.BIOME.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + resourceLocation));
            } catch (Exception exception) {
                LOGGER.error("Error while parsing flat world string => {}", (Object)exception.getMessage());
            }
        }
        flatLevelGeneratorSettings.setBiome(biome);
        if (iterator.hasNext()) {
            String[] strings;
            for (String string2 : strings = iterator.next().toLowerCase(Locale.ROOT).split(",")) {
                String[] strings3;
                String[] strings2 = string2.split("\\(", 2);
                if (strings2[0].isEmpty()) continue;
                flatLevelGeneratorSettings.addStructure(strings2[0]);
                if (strings2.length <= 1 || !strings2[1].endsWith(")") || strings2[1].length() <= 1) continue;
                for (String string3 : strings3 = strings2[1].substring(0, strings2[1].length() - 1).split(" ")) {
                    String[] strings4 = string3.split("=", 2);
                    if (strings4.length != 2) continue;
                    flatLevelGeneratorSettings.addStructureOption(strings2[0], strings4[0], strings4[1]);
                }
            }
        } else {
            flatLevelGeneratorSettings.getStructuresOptions().put("village", Maps.newHashMap());
        }
        return flatLevelGeneratorSettings;
    }

    @Environment(value=EnvType.CLIENT)
    private void addStructure(String string) {
        HashMap map = Maps.newHashMap();
        this.structuresOptions.put(string, map);
    }

    @Environment(value=EnvType.CLIENT)
    private void addStructureOption(String string, String string2, String string3) {
        this.structuresOptions.get(string).put(string2, string3);
        if ("village".equals(string) && "distance".equals(string2)) {
            this.villagesSpacing = Mth.getInt(string3, this.villagesSpacing, 9);
        }
        if ("biome_1".equals(string) && "distance".equals(string2)) {
            this.templesSpacing = Mth.getInt(string3, this.templesSpacing, 9);
        }
        if ("stronghold".equals(string)) {
            if ("distance".equals(string2)) {
                this.strongholdsDistance = Mth.getInt(string3, this.strongholdsDistance, 1);
            } else if ("count".equals(string2)) {
                this.strongholdsCount = Mth.getInt(string3, this.strongholdsCount, 1);
            } else if ("spread".equals(string2)) {
                this.strongholdsSpread = Mth.getInt(string3, this.strongholdsSpread, 1);
            }
        }
        if ("oceanmonument".equals(string)) {
            if ("separation".equals(string2)) {
                this.monumentsSeparation = Mth.getInt(string3, this.monumentsSeparation, 1);
            } else if ("spacing".equals(string2)) {
                this.monumentsSpacing = Mth.getInt(string3, this.monumentsSpacing, 1);
            }
        }
        if ("endcity".equals(string) && "distance".equals(string2)) {
            this.endCitySpacing = Mth.getInt(string3, this.endCitySpacing, 1);
        }
        if ("mansion".equals(string) && "distance".equals(string2)) {
            this.woodlandMansionSpacing = Mth.getInt(string3, this.woodlandMansionSpacing, 1);
        }
    }

    public static FlatLevelGeneratorSettings getDefault() {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = ChunkGeneratorType.FLAT.createSettings();
        flatLevelGeneratorSettings.setBiome(Biomes.PLAINS);
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        flatLevelGeneratorSettings.updateLayers();
        flatLevelGeneratorSettings.getStructuresOptions().put("village", Maps.newHashMap());
        return flatLevelGeneratorSettings;
    }

    public boolean isVoidGen() {
        return this.voidGen;
    }

    public BlockState[] getLayers() {
        return this.layers;
    }

    public void deleteLayer(int i) {
        this.layers[i] = null;
    }
}

