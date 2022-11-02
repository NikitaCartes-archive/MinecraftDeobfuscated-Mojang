/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
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
import net.minecraft.world.level.storage.PrimaryLevelData;

public record WorldDimensions(Registry<LevelStem> dimensions) {
    public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.fullCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC).fieldOf("dimensions")).forGetter(WorldDimensions::dimensions)).apply((Applicative<WorldDimensions, ?>)instance, instance.stable(WorldDimensions::new)));
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);
    private static final int VANILLA_DIMENSION_COUNT = BUILTIN_ORDER.size();

    public WorldDimensions {
        LevelStem levelStem = registry.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> stream) {
        return Stream.concat(BUILTIN_ORDER.stream(), stream.filter(resourceKey -> !BUILTIN_ORDER.contains(resourceKey)));
    }

    public WorldDimensions replaceOverworldGenerator(RegistryAccess registryAccess, ChunkGenerator chunkGenerator) {
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<LevelStem> registry2 = WorldDimensions.withOverworld(registry, this.dimensions, chunkGenerator);
        return new WorldDimensions(registry2);
    }

    public static Registry<LevelStem> withOverworld(Registry<DimensionType> registry, Registry<LevelStem> registry2, ChunkGenerator chunkGenerator) {
        LevelStem levelStem = registry2.get(LevelStem.OVERWORLD);
        Holder<DimensionType> holder = levelStem == null ? registry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : levelStem.type();
        return WorldDimensions.withOverworld(registry2, holder, chunkGenerator);
    }

    public static Registry<LevelStem> withOverworld(Registry<LevelStem> registry, Holder<DimensionType> holder, ChunkGenerator chunkGenerator) {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        ((WritableRegistry)writableRegistry).register(LevelStem.OVERWORLD, new LevelStem(holder, chunkGenerator), Lifecycle.stable());
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == LevelStem.OVERWORLD) continue;
            ((WritableRegistry)writableRegistry).register(resourceKey, entry.getValue(), registry.lifecycle(entry.getValue()));
        }
        return ((Registry)writableRegistry).freeze();
    }

    public ChunkGenerator overworld() {
        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
        return levelStem.generator();
    }

    public Optional<LevelStem> get(ResourceKey<LevelStem> resourceKey) {
        return this.dimensions.getOptional(resourceKey);
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.dimensions().entrySet().stream().map(Map.Entry::getKey).map(Registry::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(Registry<LevelStem> registry) {
        return registry.getOptional(LevelStem.OVERWORLD).map(levelStem -> {
            ChunkGenerator chunkGenerator = levelStem.generator();
            if (chunkGenerator instanceof DebugLevelSource) {
                return PrimaryLevelData.SpecialWorldProperty.DEBUG;
            }
            if (chunkGenerator instanceof FlatLevelSource) {
                return PrimaryLevelData.SpecialWorldProperty.FLAT;
            }
            return PrimaryLevelData.SpecialWorldProperty.NONE;
        }).orElse(PrimaryLevelData.SpecialWorldProperty.NONE);
    }

    static Lifecycle checkStability(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
        return WorldDimensions.isVanillaLike(resourceKey, levelStem) ? Lifecycle.stable() : Lifecycle.experimental();
    }

    private static boolean isVanillaLike(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
        if (resourceKey == LevelStem.OVERWORLD) {
            return WorldDimensions.isStableOverworld(levelStem);
        }
        if (resourceKey == LevelStem.NETHER) {
            return WorldDimensions.isStableNether(levelStem);
        }
        if (resourceKey == LevelStem.END) {
            return WorldDimensions.isStableEnd(levelStem);
        }
        return false;
    }

    private static boolean isStableOverworld(LevelStem levelStem) {
        MultiNoiseBiomeSource multiNoiseBiomeSource;
        Holder<DimensionType> holder = levelStem.type();
        if (!holder.is(BuiltinDimensionTypes.OVERWORLD) && !holder.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            return false;
        }
        BiomeSource biomeSource = levelStem.generator().getBiomeSource();
        return !(biomeSource instanceof MultiNoiseBiomeSource) || (multiNoiseBiomeSource = (MultiNoiseBiomeSource)biomeSource).stable(MultiNoiseBiomeSource.Preset.OVERWORLD);
    }

    private static boolean isStableNether(LevelStem levelStem) {
        MultiNoiseBiomeSource multiNoiseBiomeSource;
        NoiseBasedChunkGenerator noiseBasedChunkGenerator;
        Object object;
        return levelStem.type().is(BuiltinDimensionTypes.NETHER) && (object = levelStem.generator()) instanceof NoiseBasedChunkGenerator && (noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)object).stable(NoiseGeneratorSettings.NETHER) && (object = noiseBasedChunkGenerator.getBiomeSource()) instanceof MultiNoiseBiomeSource && (multiNoiseBiomeSource = (MultiNoiseBiomeSource)object).stable(MultiNoiseBiomeSource.Preset.NETHER);
    }

    private static boolean isStableEnd(LevelStem levelStem) {
        NoiseBasedChunkGenerator noiseBasedChunkGenerator;
        ChunkGenerator chunkGenerator;
        return levelStem.type().is(BuiltinDimensionTypes.END) && (chunkGenerator = levelStem.generator()) instanceof NoiseBasedChunkGenerator && (noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)chunkGenerator).stable(NoiseGeneratorSettings.END) && noiseBasedChunkGenerator.getBiomeSource() instanceof TheEndBiomeSource;
    }

    public Complete bake(Registry<LevelStem> registry) {
        record Entry(ResourceKey<LevelStem> key, LevelStem value) {
            Lifecycle lifecycle() {
                return WorldDimensions.checkStability(this.key, this.value);
            }
        }
        Stream<ResourceKey<LevelStem>> stream = Stream.concat(registry.registryKeySet().stream(), this.dimensions.registryKeySet().stream()).distinct();
        ArrayList list = new ArrayList();
        WorldDimensions.keysInOrder(stream).forEach(resourceKey -> registry.getOptional((ResourceKey<LevelStem>)resourceKey).or(() -> this.dimensions.getOptional((ResourceKey<LevelStem>)resourceKey)).ifPresent(levelStem -> list.add(new Entry((ResourceKey<LevelStem>)resourceKey, (LevelStem)levelStem))));
        Lifecycle lifecycle = list.size() == VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, lifecycle);
        list.forEach(arg -> writableRegistry.register(arg.key, arg.value, arg.lifecycle()));
        Registry<LevelStem> registry2 = ((Registry)writableRegistry).freeze();
        PrimaryLevelData.SpecialWorldProperty specialWorldProperty = WorldDimensions.specialWorldProperty(registry2);
        return new Complete(registry2.freeze(), specialWorldProperty);
    }

    public record Complete(Registry<LevelStem> dimensions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty) {
        public Lifecycle lifecycle() {
            return this.dimensions.elementsLifecycle();
        }

        public RegistryAccess.Frozen dimensionsRegistryAccess() {
            return new RegistryAccess.ImmutableRegistryAccess(List.of(this.dimensions)).freeze();
        }
    }
}

