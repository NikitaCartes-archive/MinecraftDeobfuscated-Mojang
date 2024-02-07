package net.minecraft.world.level.levelgen.presets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

public class WorldPreset {
	public static final Codec<WorldPreset> DIRECT_CODEC = ExtraCodecs.validate(
		RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter(worldPreset -> worldPreset.dimensions)
					)
					.apply(instance, WorldPreset::new)
		),
		WorldPreset::requireOverworld
	);
	public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registries.WORLD_PRESET, DIRECT_CODEC);
	private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

	public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map) {
		this.dimensions = map;
	}

	private ImmutableMap<ResourceKey<LevelStem>, LevelStem> dimensionsInOrder() {
		Builder<ResourceKey<LevelStem>, LevelStem> builder = ImmutableMap.builder();
		WorldDimensions.keysInOrder(this.dimensions.keySet().stream()).forEach(resourceKey -> {
			LevelStem levelStem = (LevelStem)this.dimensions.get(resourceKey);
			if (levelStem != null) {
				builder.put(resourceKey, levelStem);
			}
		});
		return builder.build();
	}

	public WorldDimensions createWorldDimensions() {
		return new WorldDimensions(this.dimensionsInOrder());
	}

	public Optional<LevelStem> overworld() {
		return Optional.ofNullable((LevelStem)this.dimensions.get(LevelStem.OVERWORLD));
	}

	private static DataResult<WorldPreset> requireOverworld(WorldPreset worldPreset) {
		return worldPreset.overworld().isEmpty() ? DataResult.error(() -> "Missing overworld dimension") : DataResult.success(worldPreset, Lifecycle.stable());
	}
}
