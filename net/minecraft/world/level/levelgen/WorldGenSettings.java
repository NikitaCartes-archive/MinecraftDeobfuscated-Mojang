/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import org.apache.commons.lang3.StringUtils;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(WorldGenSettings::seed), ((MapCodec)Codec.BOOL.fieldOf("generate_features")).orElse(true).stable().forGetter(WorldGenSettings::generateStructures), ((MapCodec)Codec.BOOL.fieldOf("bonus_chest")).orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest), ((MapCodec)RegistryCodecs.dataPackAwareCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC).xmap(LevelStem::sortMap, Function.identity()).fieldOf("dimensions")).forGetter(WorldGenSettings::dimensions), Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)).apply((Applicative<WorldGenSettings, ?>)instance, instance.stable(WorldGenSettings::new))).comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private final long seed;
    private final boolean generateStructures;
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
        return LevelStem.stable(this.dimensions);
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
        this.generateStructures = bl;
        this.generateBonusChest = bl2;
        this.dimensions = registry;
        this.legacyCustomOptions = optional;
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateStructures() {
        return this.generateStructures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public static WorldGenSettings replaceOverworldGenerator(RegistryAccess registryAccess, WorldGenSettings worldGenSettings, ChunkGenerator chunkGenerator) {
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<LevelStem> registry2 = WorldGenSettings.withOverworld(registry, worldGenSettings.dimensions(), chunkGenerator);
        return new WorldGenSettings(worldGenSettings.seed(), worldGenSettings.generateStructures(), worldGenSettings.generateBonusChest(), registry2);
    }

    public static Registry<LevelStem> withOverworld(Registry<DimensionType> registry, Registry<LevelStem> registry2, ChunkGenerator chunkGenerator) {
        LevelStem levelStem = registry2.get(LevelStem.OVERWORLD);
        Holder<DimensionType> holder = levelStem == null ? registry.getOrCreateHolder(BuiltinDimensionTypes.OVERWORLD) : levelStem.typeHolder();
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
        return new WorldGenSettings(this.seed, this.generateStructures, true, this.dimensions, this.legacyCustomOptions);
    }

    public WorldGenSettings withStructuresToggled() {
        return new WorldGenSettings(this.seed, !this.generateStructures, this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateStructures, !this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
        Registry<LevelStem> registry;
        long l = optionalLong.orElse(this.seed);
        if (optionalLong.isPresent()) {
            MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
            for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> resourceKey = entry.getKey();
                ((WritableRegistry)writableRegistry).register(resourceKey, new LevelStem(entry.getValue().typeHolder(), entry.getValue().generator()), this.dimensions.lifecycle(entry.getValue()));
            }
            registry = writableRegistry;
        } else {
            registry = this.dimensions;
        }
        WorldGenSettings worldGenSettings = this.isDebug() ? new WorldGenSettings(l, false, false, registry) : new WorldGenSettings(l, this.generateStructures(), this.generateBonusChest() && !bl, registry);
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

