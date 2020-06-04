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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.OverworldBiomeSource;
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
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(WorldGenSettings::seed), ((MapCodec)Codec.BOOL.fieldOf("generate_features")).withDefault(true).stable().forGetter(WorldGenSettings::generateFeatures), ((MapCodec)Codec.BOOL.fieldOf("bonus_chest")).withDefault(false).stable().forGetter(WorldGenSettings::generateBonusChest), ((MapCodec)MappedRegistry.dataPackCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC).xmap(LevelStem::sortMap, Function.identity()).fieldOf("dimensions")).forGetter(WorldGenSettings::dimensions), Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)).apply((Applicative<WorldGenSettings, ?>)instance, instance.stable(WorldGenSettings::new))).comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DEMO_SEED = "North Carolina".hashCode();
    public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(DEMO_SEED, true, true, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(DEMO_SEED), WorldGenSettings.makeDefaultOverworld(DEMO_SEED)));
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final MappedRegistry<LevelStem> dimensions;
    private final Optional<String> legacyCustomOptions;

    private DataResult<WorldGenSettings> guardExperimental() {
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
    }

    private WorldGenSettings(long l, boolean bl, boolean bl2, MappedRegistry<LevelStem> mappedRegistry, Optional<String> optional) {
        this.seed = l;
        this.generateFeatures = bl;
        this.generateBonusChest = bl2;
        this.dimensions = mappedRegistry;
        this.legacyCustomOptions = optional;
    }

    public static WorldGenSettings makeDefault() {
        long l = new Random().nextLong();
        return new WorldGenSettings(l, true, false, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(l), WorldGenSettings.makeDefaultOverworld(l)));
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(long l) {
        return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
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

    public static MappedRegistry<LevelStem> withOverworld(MappedRegistry<LevelStem> mappedRegistry, ChunkGenerator chunkGenerator) {
        MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
        DimensionType dimensionType = levelStem == null ? DimensionType.defaultOverworld() : levelStem.type();
        mappedRegistry2.register(LevelStem.OVERWORLD, new LevelStem(() -> dimensionType, chunkGenerator));
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == LevelStem.OVERWORLD) continue;
            mappedRegistry2.register(resourceKey, entry.getValue());
            if (!mappedRegistry.persistent(resourceKey)) continue;
            mappedRegistry2.setPersistent(resourceKey);
        }
        return mappedRegistry2;
    }

    public MappedRegistry<LevelStem> dimensions() {
        return this.dimensions;
    }

    public ChunkGenerator overworld() {
        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            return WorldGenSettings.makeDefaultOverworld(new Random().nextLong());
        }
        return levelStem.generator();
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.dimensions().entrySet().stream().map(entry -> ResourceKey.create(Registry.DIMENSION_REGISTRY, ((ResourceKey)entry.getKey()).location())).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    public boolean isFlatWorld() {
        return this.overworld() instanceof FlatLevelSource;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withDimensions(MappedRegistry<LevelStem> mappedRegistry) {
        return new WorldGenSettings(this.seed, this.generateFeatures, this.generateBonusChest, mappedRegistry);
    }

    public static WorldGenSettings create(Properties properties) {
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
        MappedRegistry<LevelStem> mappedRegistry = DimensionType.defaultDimensions(l);
        switch (string5) {
            case "flat": {
                JsonObject jsonObject = !string2.isEmpty() ? GsonHelper.parse(string2) : new JsonObject();
                Dynamic<JsonObject> dynamic = new Dynamic<JsonObject>(JsonOps.INSTANCE, jsonObject);
                return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(mappedRegistry, new FlatLevelSource(FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElseGet(FlatLevelGeneratorSettings::getDefault))));
            }
            case "debug_all_block_states": {
                return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(mappedRegistry, DebugLevelSource.INSTANCE));
            }
        }
        return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(mappedRegistry, WorldGenSettings.makeDefaultOverworld(l)));
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
        MappedRegistry<LevelStem> mappedRegistry;
        long l = optionalLong.orElse(this.seed);
        if (optionalLong.isPresent()) {
            mappedRegistry = new MappedRegistry(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
            long m = optionalLong.getAsLong();
            for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> resourceKey = entry.getKey();
                mappedRegistry.register(resourceKey, new LevelStem(entry.getValue().typeSupplier(), entry.getValue().generator().withSeed(m)));
                if (!this.dimensions.persistent(resourceKey)) continue;
                mappedRegistry.setPersistent(resourceKey);
            }
        } else {
            mappedRegistry = this.dimensions;
        }
        WorldGenSettings worldGenSettings = this.isDebug() ? new WorldGenSettings(l, false, false, mappedRegistry) : new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, mappedRegistry);
        return worldGenSettings;
    }
}

