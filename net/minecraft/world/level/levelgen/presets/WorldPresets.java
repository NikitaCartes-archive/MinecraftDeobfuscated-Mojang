/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldPresets {
    public static final ResourceKey<WorldPreset> NORMAL = WorldPresets.register("normal");
    public static final ResourceKey<WorldPreset> FLAT = WorldPresets.register("flat");
    public static final ResourceKey<WorldPreset> LARGE_BIOMES = WorldPresets.register("large_biomes");
    public static final ResourceKey<WorldPreset> AMPLIFIED = WorldPresets.register("amplified");
    public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = WorldPresets.register("single_biome_surface");
    public static final ResourceKey<WorldPreset> DEBUG = WorldPresets.register("debug_all_block_states");

    public static void bootstrap(BootstapContext<WorldPreset> bootstapContext) {
        new Bootstrap(bootstapContext).run();
    }

    private static ResourceKey<WorldPreset> register(String string) {
        return ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(string));
    }

    public static Optional<ResourceKey<WorldPreset>> fromSettings(Registry<LevelStem> registry) {
        return registry.getOptional(LevelStem.OVERWORLD).flatMap(levelStem -> {
            ChunkGenerator chunkGenerator = levelStem.generator();
            if (chunkGenerator instanceof FlatLevelSource) {
                return Optional.of(FLAT);
            }
            if (chunkGenerator instanceof DebugLevelSource) {
                return Optional.of(DEBUG);
            }
            return Optional.empty();
        });
    }

    public static WorldDimensions createNormalWorldDimensions(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().createWorldDimensions();
    }

    public static LevelStem getNormalOverworld(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().overworld().orElseThrow();
    }

    static class Bootstrap {
        private final BootstapContext<WorldPreset> context;
        private final HolderGetter<NoiseGeneratorSettings> noiseSettings;
        private final HolderGetter<Biome> biomes;
        private final HolderGetter<PlacedFeature> placedFeatures;
        private final HolderGetter<StructureSet> structureSets;
        private final Holder<DimensionType> overworldDimensionType;
        private final LevelStem netherStem;
        private final LevelStem endStem;

        Bootstrap(BootstapContext<WorldPreset> bootstapContext) {
            this.context = bootstapContext;
            HolderGetter<DimensionType> holderGetter = bootstapContext.lookup(Registries.DIMENSION_TYPE);
            this.noiseSettings = bootstapContext.lookup(Registries.NOISE_SETTINGS);
            this.biomes = bootstapContext.lookup(Registries.BIOME);
            this.placedFeatures = bootstapContext.lookup(Registries.PLACED_FEATURE);
            this.structureSets = bootstapContext.lookup(Registries.STRUCTURE_SET);
            this.overworldDimensionType = holderGetter.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            Holder.Reference<DimensionType> holder = holderGetter.getOrThrow(BuiltinDimensionTypes.NETHER);
            Holder.Reference<NoiseGeneratorSettings> holder2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
            this.netherStem = new LevelStem(holder, new NoiseBasedChunkGenerator((BiomeSource)MultiNoiseBiomeSource.Preset.NETHER.biomeSource(this.biomes), holder2));
            Holder.Reference<DimensionType> holder3 = holderGetter.getOrThrow(BuiltinDimensionTypes.END);
            Holder.Reference<NoiseGeneratorSettings> holder4 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
            this.endStem = new LevelStem(holder3, new NoiseBasedChunkGenerator((BiomeSource)TheEndBiomeSource.create(this.biomes), holder4));
        }

        private LevelStem makeOverworld(ChunkGenerator chunkGenerator) {
            return new LevelStem(this.overworldDimensionType, chunkGenerator);
        }

        private LevelStem makeNoiseBasedOverworld(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
            return this.makeOverworld(new NoiseBasedChunkGenerator(biomeSource, holder));
        }

        private WorldPreset createPresetWithCustomOverworld(LevelStem levelStem) {
            return new WorldPreset(Map.of(LevelStem.OVERWORLD, levelStem, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
        }

        private void registerCustomOverworldPreset(ResourceKey<WorldPreset> resourceKey, LevelStem levelStem) {
            this.context.register(resourceKey, this.createPresetWithCustomOverworld(levelStem));
        }

        public void run() {
            MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(this.biomes);
            Holder.Reference<NoiseGeneratorSettings> holder = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.registerCustomOverworldPreset(NORMAL, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder));
            Holder.Reference<NoiseGeneratorSettings> holder2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
            this.registerCustomOverworldPreset(LARGE_BIOMES, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder2));
            Holder.Reference<NoiseGeneratorSettings> holder3 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
            this.registerCustomOverworldPreset(AMPLIFIED, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder3));
            Holder.Reference<Biome> reference = this.biomes.getOrThrow(Biomes.PLAINS);
            this.registerCustomOverworldPreset(SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new FixedBiomeSource(reference), holder));
            this.registerCustomOverworldPreset(FLAT, this.makeOverworld(new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets, this.placedFeatures))));
            this.registerCustomOverworldPreset(DEBUG, this.makeOverworld(new DebugLevelSource(reference)));
        }
    }
}

