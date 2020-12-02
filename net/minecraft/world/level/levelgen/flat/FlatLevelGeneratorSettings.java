/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings
implements LevelHeightAccessor {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.biomes), ((MapCodec)StructureSettings.CODEC.fieldOf("structures")).forGetter(FlatLevelGeneratorSettings::structureSettings), ((MapCodec)FlatLayerInfo.CODEC.listOf().fieldOf("layers")).forGetter(FlatLevelGeneratorSettings::getLayersInfo), ((MapCodec)Codec.BOOL.fieldOf("lakes")).orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.addLakes), ((MapCodec)Codec.BOOL.fieldOf("features")).orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.decoration), Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(flatLevelGeneratorSettings -> Optional.of(flatLevelGeneratorSettings.biome))).apply((Applicative<FlatLevelGeneratorSettings, ?>)instance, FlatLevelGeneratorSettings::new)).comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity()).stable();
    private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(StructureFeature.MINESHAFT, StructureFeatures.MINESHAFT);
        hashMap.put(StructureFeature.VILLAGE, StructureFeatures.VILLAGE_PLAINS);
        hashMap.put(StructureFeature.STRONGHOLD, StructureFeatures.STRONGHOLD);
        hashMap.put(StructureFeature.SWAMP_HUT, StructureFeatures.SWAMP_HUT);
        hashMap.put(StructureFeature.DESERT_PYRAMID, StructureFeatures.DESERT_PYRAMID);
        hashMap.put(StructureFeature.JUNGLE_TEMPLE, StructureFeatures.JUNGLE_TEMPLE);
        hashMap.put(StructureFeature.IGLOO, StructureFeatures.IGLOO);
        hashMap.put(StructureFeature.OCEAN_RUIN, StructureFeatures.OCEAN_RUIN_COLD);
        hashMap.put(StructureFeature.SHIPWRECK, StructureFeatures.SHIPWRECK);
        hashMap.put(StructureFeature.OCEAN_MONUMENT, StructureFeatures.OCEAN_MONUMENT);
        hashMap.put(StructureFeature.END_CITY, StructureFeatures.END_CITY);
        hashMap.put(StructureFeature.WOODLAND_MANSION, StructureFeatures.WOODLAND_MANSION);
        hashMap.put(StructureFeature.NETHER_BRIDGE, StructureFeatures.NETHER_BRIDGE);
        hashMap.put(StructureFeature.PILLAGER_OUTPOST, StructureFeatures.PILLAGER_OUTPOST);
        hashMap.put(StructureFeature.RUINED_PORTAL, StructureFeatures.RUINED_PORTAL_STANDARD);
        hashMap.put(StructureFeature.BASTION_REMNANT, StructureFeatures.BASTION_REMNANT);
    });
    private final Registry<Biome> biomes;
    private final StructureSettings structureSettings;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Supplier<Biome> biome;
    private final BlockState[] layers;
    private boolean voidGen;
    private boolean decoration;
    private boolean addLakes;

    private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        int i = flatLevelGeneratorSettings.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
        if (i > DimensionType.Y_SIZE) {
            return DataResult.error("Sum of layer heights is > " + DimensionType.Y_SIZE, flatLevelGeneratorSettings);
        }
        return DataResult.success(flatLevelGeneratorSettings);
    }

    private FlatLevelGeneratorSettings(Registry<Biome> registry, StructureSettings structureSettings, List<FlatLayerInfo> list, boolean bl, boolean bl2, Optional<Supplier<Biome>> optional) {
        this(structureSettings, registry);
        if (bl) {
            this.setAddLakes();
        }
        if (bl2) {
            this.setDecoration();
        }
        this.layersInfo.addAll(list);
        this.updateLayers();
        if (!optional.isPresent()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            this.biome = () -> registry.getOrThrow(Biomes.PLAINS);
        } else {
            this.biome = optional.get();
        }
    }

    public FlatLevelGeneratorSettings(StructureSettings structureSettings, Registry<Biome> registry) {
        this.biomes = registry;
        this.structureSettings = structureSettings;
        this.biome = () -> registry.getOrThrow(Biomes.PLAINS);
        this.layers = new BlockState[this.getHeight()];
    }

    @Environment(value=EnvType.CLIENT)
    public FlatLevelGeneratorSettings withStructureSettings(StructureSettings structureSettings) {
        return this.withLayers(this.layersInfo, structureSettings);
    }

    @Environment(value=EnvType.CLIENT)
    public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> list, StructureSettings structureSettings) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, this.biomes);
        for (FlatLayerInfo flatLayerInfo : list) {
            flatLevelGeneratorSettings.layersInfo.add(new FlatLayerInfo(flatLayerInfo.getHeight(), flatLayerInfo.getBlockState().getBlock()));
            flatLevelGeneratorSettings.updateLayers();
        }
        flatLevelGeneratorSettings.setBiome(this.biome);
        if (this.decoration) {
            flatLevelGeneratorSettings.setDecoration();
        }
        if (this.addLakes) {
            flatLevelGeneratorSettings.setAddLakes();
        }
        return flatLevelGeneratorSettings;
    }

    public void setDecoration() {
        this.decoration = true;
    }

    public void setAddLakes() {
        this.addLakes = true;
    }

    public Biome getBiomeFromSettings() {
        int i;
        boolean bl;
        Biome biome = this.getBiome();
        BiomeGenerationSettings biomeGenerationSettings = biome.getGenerationSettings();
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder().surfaceBuilder(biomeGenerationSettings.getSurfaceBuilder());
        if (this.addLakes) {
            builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
            builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
        }
        for (Map.Entry<StructureFeature<?>, StructureFeatureConfiguration> entry : this.structureSettings.structureConfig().entrySet()) {
            builder.addStructureStart(biomeGenerationSettings.withBiomeConfig(STRUCTURE_FEATURES.get(entry.getKey())));
        }
        boolean bl2 = bl = (!this.voidGen || this.biomes.getResourceKey(biome).equals(Optional.of(Biomes.THE_VOID))) && this.decoration;
        if (bl) {
            List<List<Supplier<ConfiguredFeature<?, ?>>>> list = biomeGenerationSettings.features();
            for (i = 0; i < list.size(); ++i) {
                if (i == GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() || i == GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) continue;
                List<Supplier<ConfiguredFeature<?, ?>>> list2 = list.get(i);
                for (Supplier<ConfiguredFeature<?, ?>> supplier : list2) {
                    builder.addFeature(i, supplier);
                }
            }
        }
        BlockState[] blockStates = this.getLayers();
        for (i = 0; i < blockStates.length; ++i) {
            BlockState blockState = blockStates[i];
            if (blockState == null || Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) continue;
            this.layers[i] = null;
            int j = this.getMinBuildHeight() + i;
            builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(j, blockState)));
        }
        return new Biome.BiomeBuilder().precipitation(biome.getPrecipitation()).biomeCategory(biome.getBiomeCategory()).depth(biome.getDepth()).scale(biome.getScale()).temperature(biome.getBaseTemperature()).downfall(biome.getDownfall()).specialEffects(biome.getSpecialEffects()).generationSettings(builder.build()).mobSpawnSettings(biome.getMobSettings()).build();
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public Biome getBiome() {
        return this.biome.get();
    }

    @Environment(value=EnvType.CLIENT)
    public void setBiome(Supplier<Biome> supplier) {
        this.biome = supplier;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public BlockState[] getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        Arrays.fill(this.layers, 0, this.layers.length, null);
        int i = this.getMinBuildHeight();
        for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
            flatLayerInfo.setStart(i);
            i += flatLayerInfo.getHeight();
        }
        this.voidGen = true;
        for (FlatLayerInfo flatLayerInfo2 : this.layersInfo) {
            for (int j = flatLayerInfo2.getStart(); j < flatLayerInfo2.getStart() + flatLayerInfo2.getHeight(); ++j) {
                BlockState blockState = flatLayerInfo2.getBlockState();
                if (blockState.is(Blocks.AIR)) continue;
                this.voidGen = false;
                this.layers[this.getLayerIndex((int)j)] = blockState;
            }
        }
    }

    public static FlatLevelGeneratorSettings getDefault(Registry<Biome> registry) {
        StructureSettings structureSettings = new StructureSettings(Optional.of(StructureSettings.DEFAULT_STRONGHOLD), Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE))));
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, registry);
        flatLevelGeneratorSettings.biome = () -> registry.getOrThrow(Biomes.PLAINS);
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        flatLevelGeneratorSettings.updateLayers();
        return flatLevelGeneratorSettings;
    }

    public int getLayerIndex(int i) {
        return i - this.getMinBuildHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 256;
    }
}

