/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(WorldGenSettings::seed), ((MapCodec)Codec.BOOL.fieldOf("generate_features")).withDefault(true).stable().forGetter(WorldGenSettings::generateFeatures), ((MapCodec)Codec.BOOL.fieldOf("bonus_chest")).withDefault(false).stable().forGetter(WorldGenSettings::generateBonusChest), ((MapCodec)Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_REGISTRY), ResourceKey::location), Codec.mapPair(DimensionType.CODEC.fieldOf("type"), ChunkGenerator.CODEC.fieldOf("generator")).codec()).xmap(DimensionType::sortMap, Function.identity()).fieldOf("dimensions")).forGetter(WorldGenSettings::dimensions), Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)).apply((Applicative<WorldGenSettings, ?>)instance, instance.stable(WorldGenSettings::new))).comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DEMO_SEED = "North Carolina".hashCode();
    public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(DEMO_SEED, true, true, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(DEMO_SEED), WorldGenSettings.makeDefaultOverworld(DEMO_SEED)));
    public static final WorldGenSettings TEST_SETTINGS = new WorldGenSettings(0L, false, false, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(0L), new FlatLevelSource(FlatLevelGeneratorSettings.getDefault())));
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> dimensions;
    private final Optional<String> legacyCustomOptions;

    private DataResult<WorldGenSettings> guardExperimental() {
        if (this.stable()) {
            return DataResult.success(this, Lifecycle.stable());
        }
        return DataResult.success(this);
    }

    private boolean stable() {
        return DimensionType.stable(this.seed, this.dimensions);
    }

    public WorldGenSettings(long l, boolean bl, boolean bl2, LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap) {
        this(l, bl, bl2, linkedHashMap, Optional.empty());
    }

    private WorldGenSettings(long l, boolean bl, boolean bl2, LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap, Optional<String> optional) {
        this.seed = l;
        this.generateFeatures = bl;
        this.generateBonusChest = bl2;
        this.dimensions = linkedHashMap;
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

    public static LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> withOverworld(LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap, ChunkGenerator chunkGenerator) {
        LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap2 = Maps.newLinkedHashMap();
        Pair<DimensionType, ChunkGenerator> pair = linkedHashMap.get(DimensionType.OVERWORLD_LOCATION);
        DimensionType dimensionType = pair == null ? DimensionType.makeDefaultOverworld() : pair.getFirst();
        linkedHashMap2.put(Level.OVERWORLD, Pair.of(dimensionType, chunkGenerator));
        for (Map.Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry : linkedHashMap.entrySet()) {
            if (entry.getKey() == Level.OVERWORLD) continue;
            linkedHashMap2.put(entry.getKey(), entry.getValue());
        }
        return linkedHashMap2;
    }

    public LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> dimensions() {
        return this.dimensions;
    }

    public ChunkGenerator overworld() {
        Pair<DimensionType, ChunkGenerator> pair = this.dimensions.get(DimensionType.OVERWORLD_LOCATION);
        if (pair == null) {
            return WorldGenSettings.makeDefaultOverworld(new Random().nextLong());
        }
        return pair.getSecond();
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
        LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap = DimensionType.defaultDimensions(l);
        switch (string5) {
            case "flat": {
                JsonObject jsonObject = !string2.isEmpty() ? GsonHelper.parse(string2) : new JsonObject();
                Dynamic<JsonObject> dynamic = new Dynamic<JsonObject>(JsonOps.INSTANCE, jsonObject);
                return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(linkedHashMap, new FlatLevelSource(FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElseGet(FlatLevelGeneratorSettings::getDefault))));
            }
            case "debug_all_block_states": {
                return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(linkedHashMap, DebugLevelSource.INSTANCE));
            }
        }
        return new WorldGenSettings(l, bl, false, WorldGenSettings.withOverworld(linkedHashMap, WorldGenSettings.makeDefaultOverworld(l)));
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
        LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap;
        long l = optionalLong.orElse(this.seed);
        if (optionalLong.isPresent()) {
            linkedHashMap = Maps.newLinkedHashMap();
            long m = optionalLong.getAsLong();
            for (Map.Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry : this.dimensions.entrySet()) {
                linkedHashMap.put(entry.getKey(), Pair.of(entry.getValue().getFirst(), entry.getValue().getSecond().withSeed(m)));
            }
        } else {
            linkedHashMap = this.dimensions;
        }
        WorldGenSettings worldGenSettings = this.isDebug() ? new WorldGenSettings(l, false, false, linkedHashMap) : new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, linkedHashMap);
        return worldGenSettings;
    }
}

