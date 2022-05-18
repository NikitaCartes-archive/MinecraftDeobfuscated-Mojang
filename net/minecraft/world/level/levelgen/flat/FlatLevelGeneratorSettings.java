/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class FlatLevelGeneratorSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.biomes), RegistryCodecs.homogeneousList(Registry.STRUCTURE_SET_REGISTRY).optionalFieldOf("structure_overrides").forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.structureOverrides), ((MapCodec)FlatLayerInfo.CODEC.listOf().fieldOf("layers")).forGetter(FlatLevelGeneratorSettings::getLayersInfo), ((MapCodec)Codec.BOOL.fieldOf("lakes")).orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.addLakes), ((MapCodec)Codec.BOOL.fieldOf("features")).orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.decoration), Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(flatLevelGeneratorSettings -> Optional.of(flatLevelGeneratorSettings.biome))).apply((Applicative<FlatLevelGeneratorSettings, ?>)instance, FlatLevelGeneratorSettings::new)).comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity()).stable();
    private final Registry<Biome> biomes;
    private final Optional<HolderSet<StructureSet>> structureOverrides;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Holder<Biome> biome;
    private final List<BlockState> layers;
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

    private FlatLevelGeneratorSettings(Registry<Biome> registry, Optional<HolderSet<StructureSet>> optional, List<FlatLayerInfo> list, boolean bl, boolean bl2, Optional<Holder<Biome>> optional2) {
        this(optional, registry);
        if (bl) {
            this.setAddLakes();
        }
        if (bl2) {
            this.setDecoration();
        }
        this.layersInfo.addAll(list);
        this.updateLayers();
        if (optional2.isEmpty()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            this.biome = registry.getOrCreateHolderOrThrow(Biomes.PLAINS);
        } else {
            this.biome = optional2.get();
        }
    }

    public FlatLevelGeneratorSettings(Optional<HolderSet<StructureSet>> optional, Registry<Biome> registry) {
        this.biomes = registry;
        this.structureOverrides = optional;
        this.biome = registry.getOrCreateHolderOrThrow(Biomes.PLAINS);
        this.layers = Lists.newArrayList();
    }

    public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> list, Optional<HolderSet<StructureSet>> optional) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(optional, this.biomes);
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

    public BiomeGenerationSettings adjustGenerationSettings(Holder<Biome> holder) {
        int i;
        List<Object> list;
        boolean bl;
        if (!holder.equals(this.biome)) {
            return holder.value().getGenerationSettings();
        }
        BiomeGenerationSettings biomeGenerationSettings = this.getBiome().value().getGenerationSettings();
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
        if (this.addLakes) {
            builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND);
            builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE);
        }
        boolean bl2 = bl = (!this.voidGen || holder.is(Biomes.THE_VOID)) && this.decoration;
        if (bl) {
            list = biomeGenerationSettings.features();
            for (i = 0; i < list.size(); ++i) {
                if (i == GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() || i == GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) continue;
                HolderSet holderSet = (HolderSet)list.get(i);
                for (Holder holder2 : holderSet) {
                    builder.addFeature(i, (Holder<PlacedFeature>)holder2);
                }
            }
        }
        list = this.getLayers();
        for (i = 0; i < list.size(); ++i) {
            BlockState blockState = (BlockState)list.get(i);
            if (Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) continue;
            list.set(i, null);
            builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(i, blockState), new PlacementModifier[0]));
        }
        return builder.build();
    }

    public Optional<HolderSet<StructureSet>> structureOverrides() {
        return this.structureOverrides;
    }

    public Holder<Biome> getBiome() {
        return this.biome;
    }

    public void setBiome(Holder<Biome> holder) {
        this.biome = holder;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public List<BlockState> getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        this.layers.clear();
        for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
            for (int i = 0; i < flatLayerInfo.getHeight(); ++i) {
                this.layers.add(flatLayerInfo.getBlockState());
            }
        }
        this.voidGen = this.layers.stream().allMatch(blockState -> blockState.is(Blocks.AIR));
    }

    public static FlatLevelGeneratorSettings getDefault(Registry<Biome> registry, Registry<StructureSet> registry2) {
        HolderSet.Direct holderSet = HolderSet.direct(registry2.getHolderOrThrow(BuiltinStructureSets.STRONGHOLDS), registry2.getHolderOrThrow(BuiltinStructureSets.VILLAGES));
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(Optional.of(holderSet), registry);
        flatLevelGeneratorSettings.biome = registry.getOrCreateHolderOrThrow(Biomes.PLAINS);
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        flatLevelGeneratorSettings.updateLayers();
        return flatLevelGeneratorSettings;
    }
}

