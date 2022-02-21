/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(WorldGenSettings::seed), ((MapCodec)Codec.BOOL.fieldOf("generate_features")).orElse(true).stable().forGetter(WorldGenSettings::generateFeatures), ((MapCodec)Codec.BOOL.fieldOf("bonus_chest")).orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest), ((MapCodec)RegistryCodecs.dataPackAwareCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC).xmap(LevelStem::sortMap, Function.identity()).fieldOf("dimensions")).forGetter(WorldGenSettings::dimensions), Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)).apply((Applicative<WorldGenSettings, ?>)instance, instance.stable(WorldGenSettings::new))).comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final Registry<LevelStem> dimensions;
    private final Optional<String> legacyCustomOptions;

    private DataResult<WorldGenSettings> guardExperimental() {
        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            return DataResult.error("Overworld settings missing");
        }
        if (this.stable()) {
            return DataResult.success(this, Lifecycle.stable());
        }
        return DataResult.success(this);
    }

    private boolean stable() {
        return LevelStem.stable(this.seed, this.dimensions);
    }

    public WorldGenSettings(long l, boolean bl, boolean bl2, Registry<LevelStem> registry) {
        this(l, bl, bl2, registry, Optional.empty());
        LevelStem levelStem = registry.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    private WorldGenSettings(long l, boolean bl, boolean bl2, Registry<LevelStem> registry, Optional<String> optional) {
        this.seed = l;
        this.generateFeatures = bl;
        this.generateBonusChest = bl2;
        this.dimensions = registry;
        this.legacyCustomOptions = optional;
    }

    public static WorldGenSettings demoSettings(RegistryAccess registryAccess) {
        int i = "North Carolina".hashCode();
        return new WorldGenSettings(i, true, true, WorldGenSettings.withOverworld(registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), DimensionType.defaultDimensions(registryAccess, i), (ChunkGenerator)WorldGenSettings.makeDefaultOverworld(registryAccess, i)));
    }

    public static WorldGenSettings makeDefault(RegistryAccess registryAccess) {
        long l = new Random().nextLong();
        return new WorldGenSettings(l, true, false, WorldGenSettings.withOverworld(registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), DimensionType.defaultDimensions(registryAccess, l), (ChunkGenerator)WorldGenSettings.makeDefaultOverworld(registryAccess, l)));
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(RegistryAccess registryAccess, long l) {
        return WorldGenSettings.makeDefaultOverworld(registryAccess, l, true);
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(RegistryAccess registryAccess, long l, boolean bl) {
        return WorldGenSettings.makeOverworld(registryAccess, l, NoiseGeneratorSettings.OVERWORLD, bl);
    }

    public static NoiseBasedChunkGenerator makeOverworld(RegistryAccess registryAccess, long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return WorldGenSettings.makeOverworld(registryAccess, l, resourceKey, true);
    }

    public static NoiseBasedChunkGenerator makeOverworld(RegistryAccess registryAccess, long l, ResourceKey<NoiseGeneratorSettings> resourceKey, boolean bl) {
        Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NoiseGeneratorSettings> registry3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> registry4 = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);
        return new NoiseBasedChunkGenerator(registry2, registry4, (BiomeSource)MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(registry, bl), l, registry3.getOrCreateHolder(resourceKey));
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateFeatures() {
        return this.generateFeatures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public static Registry<LevelStem> withOverworld(Registry<DimensionType> registry, Registry<LevelStem> registry2, ChunkGenerator chunkGenerator) {
        LevelStem levelStem = registry2.get(LevelStem.OVERWORLD);
        Holder<DimensionType> holder = levelStem == null ? registry.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION) : levelStem.typeHolder();
        return WorldGenSettings.withOverworld(registry2, holder, chunkGenerator);
    }

    public static Registry<LevelStem> withOverworld(Registry<LevelStem> registry, Holder<DimensionType> holder, ChunkGenerator chunkGenerator) {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        ((WritableRegistry)writableRegistry).register(LevelStem.OVERWORLD, new LevelStem(holder, chunkGenerator), Lifecycle.stable());
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == LevelStem.OVERWORLD) continue;
            ((WritableRegistry)writableRegistry).register(resourceKey, entry.getValue(), registry.lifecycle(entry.getValue()));
        }
        return writableRegistry;
    }

    public Registry<LevelStem> dimensions() {
        return this.dimensions;
    }

    public ChunkGenerator overworld() {
        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
        return levelStem.generator();
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.dimensions().entrySet().stream().map(Map.Entry::getKey).map(WorldGenSettings::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
    }

    public static ResourceKey<Level> levelStemToLevel(ResourceKey<LevelStem> resourceKey) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceKey.location());
    }

    public static ResourceKey<LevelStem> levelToLevelStem(ResourceKey<Level> resourceKey) {
        return ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, resourceKey.location());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    public boolean isFlatWorld() {
        return this.overworld() instanceof FlatLevelSource;
    }

    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
    }

    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
    }

    public static WorldGenSettings create(RegistryAccess registryAccess, DedicatedServerProperties.WorldGenProperties worldGenProperties) {
        long l = WorldGenSettings.parseSeed(worldGenProperties.levelSeed()).orElse(new Random().nextLong());
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> registry3 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<LevelStem> registry4 = DimensionType.defaultDimensions(registryAccess, l);
        switch (worldGenProperties.levelType()) {
            case "flat": {
                Dynamic<JsonObject> dynamic = new Dynamic<JsonObject>(JsonOps.INSTANCE, worldGenProperties.generatorSettings());
                return new WorldGenSettings(l, worldGenProperties.generateStructures(), false, WorldGenSettings.withOverworld(registry, registry4, (ChunkGenerator)new FlatLevelSource(registry3, FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElseGet(() -> FlatLevelGeneratorSettings.getDefault(registry2, registry3)))));
            }
            case "debug_all_block_states": {
                return new WorldGenSettings(l, worldGenProperties.generateStructures(), false, WorldGenSettings.withOverworld(registry, registry4, (ChunkGenerator)new DebugLevelSource(registry3, registry2)));
            }
            case "amplified": {
                return new WorldGenSettings(l, worldGenProperties.generateStructures(), false, WorldGenSettings.withOverworld(registry, registry4, (ChunkGenerator)WorldGenSettings.makeOverworld(registryAccess, l, NoiseGeneratorSettings.AMPLIFIED)));
            }
            case "largebiomes": {
                return new WorldGenSettings(l, worldGenProperties.generateStructures(), false, WorldGenSettings.withOverworld(registry, registry4, (ChunkGenerator)WorldGenSettings.makeOverworld(registryAccess, l, NoiseGeneratorSettings.LARGE_BIOMES)));
            }
        }
        return new WorldGenSettings(l, worldGenProperties.generateStructures(), false, WorldGenSettings.withOverworld(registry, registry4, (ChunkGenerator)WorldGenSettings.makeDefaultOverworld(registryAccess, l)));
    }

    public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
        Registry<LevelStem> registry;
        long l = optionalLong.orElse(this.seed);
        if (optionalLong.isPresent()) {
            MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
            long m = optionalLong.getAsLong();
            for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> resourceKey = entry.getKey();
                ((WritableRegistry)writableRegistry).register(resourceKey, new LevelStem(entry.getValue().typeHolder(), entry.getValue().generator().withSeed(m)), this.dimensions.lifecycle(entry.getValue()));
            }
            registry = writableRegistry;
        } else {
            registry = this.dimensions;
        }
        WorldGenSettings worldGenSettings = this.isDebug() ? new WorldGenSettings(l, false, false, registry) : new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, registry);
        return worldGenSettings;
    }

    public static OptionalLong parseSeed(String string) {
        if (StringUtils.isEmpty(string = string.trim())) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException numberFormatException) {
            return OptionalLong.of(string.hashCode());
        }
    }
}

