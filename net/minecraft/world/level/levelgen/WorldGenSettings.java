/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(WorldGenSettings::seed), ((MapCodec)Codec.BOOL.fieldOf("generate_features")).orElse(true).stable().forGetter(WorldGenSettings::generateFeatures), ((MapCodec)Codec.BOOL.fieldOf("bonus_chest")).orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest), ((MapCodec)MappedRegistry.dataPackCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC).xmap(LevelStem::sortMap, Function.identity()).fieldOf("dimensions")).forGetter(WorldGenSettings::dimensions), Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)).apply((Applicative<WorldGenSettings, ?>)instance, instance.stable(WorldGenSettings::new))).comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final MappedRegistry<LevelStem> dimensions;
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

    public WorldGenSettings(long l, boolean bl, boolean bl2, MappedRegistry<LevelStem> mappedRegistry) {
        this(l, bl, bl2, mappedRegistry, Optional.empty());
        LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    private WorldGenSettings(long l, boolean bl, boolean bl2, MappedRegistry<LevelStem> mappedRegistry, Optional<String> optional) {
        this.seed = l;
        this.generateFeatures = bl;
        this.generateBonusChest = bl2;
        this.dimensions = mappedRegistry;
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
        return new NoiseBasedChunkGenerator(registryAccess.registryOrThrow(Registry.NOISE_REGISTRY), (BiomeSource)MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY)), l, () -> registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
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

    public static MappedRegistry<LevelStem> withOverworld(Registry<DimensionType> registry, MappedRegistry<LevelStem> mappedRegistry, ChunkGenerator chunkGenerator) {
        LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
        Supplier<DimensionType> supplier = () -> levelStem == null ? registry.getOrThrow(DimensionType.OVERWORLD_LOCATION) : levelStem.type();
        return WorldGenSettings.withOverworld(mappedRegistry, supplier, chunkGenerator);
    }

    public static MappedRegistry<LevelStem> withOverworld(MappedRegistry<LevelStem> mappedRegistry, Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
        MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        mappedRegistry2.register(LevelStem.OVERWORLD, new LevelStem(supplier, chunkGenerator), Lifecycle.stable());
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == LevelStem.OVERWORLD) continue;
            mappedRegistry2.register(resourceKey, entry.getValue(), mappedRegistry.lifecycle(entry.getValue()));
        }
        return mappedRegistry2;
    }

    public MappedRegistry<LevelStem> dimensions() {
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

    public static WorldGenSettings create(RegistryAccess registryAccess, Properties properties) {
        String string2 = MoreObjects.firstNonNull((String)properties.get("generator-settings"), "");
        properties.put("generator-settings", string2);
        String string22 = MoreObjects.firstNonNull((String)properties.get("level-seed"), "");
        properties.put("level-seed", string22);
        String string3 = (String)properties.get("generate-structures");
        boolean bl = string3 == null || Boolean.parseBoolean(string3);
        properties.put("generate-structures", Objects.toString(bl));
        String string4 = (String)properties.get("level-type");
        String string5 = Optional.ofNullable(string4).map(string -> string.toLowerCase(Locale.ROOT)).orElse("default");
        properties.put("level-type", string5);
        long l = new Random().nextLong();
        if (!string22.isEmpty()) {
            try {
                long m = Long.parseLong(string22);
                if (m != 0L) {
                    l = m;
                }
            } catch (NumberFormatException numberFormatException) {
                l = string22.hashCode();
            }
        }
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        MappedRegistry<LevelStem> mappedRegistry = DimensionType.defaultDimensions(registryAccess, l);
        switch (string5) {
            case "flat": {
                JsonObject jsonObject = !string2.isEmpty() ? GsonHelper.parse(string2) : new JsonObject();
                Dynamic<JsonObject> dynamic = new Dynamic<JsonObject>(JsonOps.INSTANCE, jsonObject);
                return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(registry, mappedRegistry, (ChunkGenerator)new FlatLevelSource(FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElseGet(() -> FlatLevelGeneratorSettings.getDefault(registry2)))));
            }
            case "debug_all_block_states": {
                return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(registry, mappedRegistry, (ChunkGenerator)new DebugLevelSource(registry2)));
            }
        }
        return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(registry, mappedRegistry, (ChunkGenerator)WorldGenSettings.makeDefaultOverworld(registryAccess, l)));
    }

    public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
        MappedRegistry<LevelStem> mappedRegistry;
        long l = optionalLong.orElse(this.seed);
        if (optionalLong.isPresent()) {
            mappedRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
            long m = optionalLong.getAsLong();
            for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> resourceKey = entry.getKey();
                mappedRegistry.register(resourceKey, new LevelStem(entry.getValue().typeSupplier(), entry.getValue().generator().withSeed(m)), this.dimensions.lifecycle(entry.getValue()));
            }
        } else {
            mappedRegistry = this.dimensions;
        }
        WorldGenSettings worldGenSettings = this.isDebug() ? new WorldGenSettings(l, false, false, mappedRegistry) : new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, mappedRegistry);
        return worldGenSettings;
    }
}

