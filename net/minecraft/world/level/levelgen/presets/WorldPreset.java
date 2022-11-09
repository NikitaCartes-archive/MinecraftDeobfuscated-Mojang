/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.presets;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

public class WorldPreset {
    public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions")).forGetter(worldPreset -> worldPreset.dimensions)).apply((Applicative<WorldPreset, ?>)instance, WorldPreset::new)).flatXmap(WorldPreset::requireOverworld, WorldPreset::requireOverworld);
    public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registries.WORLD_PRESET, DIRECT_CODEC);
    private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

    public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map) {
        this.dimensions = map;
    }

    private Registry<LevelStem> createRegistry() {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.experimental());
        WorldDimensions.keysInOrder(this.dimensions.keySet().stream()).forEach(resourceKey -> {
            LevelStem levelStem = this.dimensions.get(resourceKey);
            if (levelStem != null) {
                writableRegistry.register((ResourceKey<LevelStem>)resourceKey, levelStem, Lifecycle.stable());
            }
        });
        return writableRegistry.freeze();
    }

    public WorldDimensions createWorldDimensions() {
        return new WorldDimensions(this.createRegistry());
    }

    public Optional<LevelStem> overworld() {
        return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
    }

    private static DataResult<WorldPreset> requireOverworld(WorldPreset worldPreset) {
        if (worldPreset.overworld().isEmpty()) {
            return DataResult.error("Missing overworld dimension");
        }
        return DataResult.success(worldPreset, Lifecycle.stable());
    }
}

