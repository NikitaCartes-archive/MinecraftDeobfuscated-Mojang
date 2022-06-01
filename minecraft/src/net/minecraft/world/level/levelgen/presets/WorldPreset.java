package net.minecraft.world.level.levelgen.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class WorldPreset {
	public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.unboundedMap(ResourceKey.codec(Registry.LEVEL_STEM_REGISTRY), LevelStem.CODEC)
							.fieldOf("dimensions")
							.forGetter(worldPreset -> worldPreset.dimensions)
					)
					.apply(instance, WorldPreset::new)
		)
		.flatXmap(WorldPreset::requireOverworld, WorldPreset::requireOverworld);
	public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registry.WORLD_PRESET_REGISTRY, DIRECT_CODEC);
	private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

	public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map) {
		this.dimensions = map;
	}

	private Registry<LevelStem> createRegistry() {
		WritableRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
		LevelStem.keysInOrder(this.dimensions.keySet().stream()).forEach(resourceKey -> {
			LevelStem levelStem = (LevelStem)this.dimensions.get(resourceKey);
			if (levelStem != null) {
				writableRegistry.register(resourceKey, levelStem, Lifecycle.stable());
			}
		});
		return writableRegistry.freeze();
	}

	public WorldGenSettings createWorldGenSettings(long l, boolean bl, boolean bl2) {
		return new WorldGenSettings(l, bl, bl2, this.createRegistry());
	}

	public WorldGenSettings recreateWorldGenSettings(WorldGenSettings worldGenSettings) {
		return this.createWorldGenSettings(worldGenSettings.seed(), worldGenSettings.generateStructures(), worldGenSettings.generateBonusChest());
	}

	public Optional<LevelStem> overworld() {
		return Optional.ofNullable((LevelStem)this.dimensions.get(LevelStem.OVERWORLD));
	}

	public LevelStem overworldOrThrow() {
		return (LevelStem)this.overworld().orElseThrow(() -> new IllegalStateException("Can't find overworld in this preset"));
	}

	private static DataResult<WorldPreset> requireOverworld(WorldPreset worldPreset) {
		return worldPreset.overworld().isEmpty() ? DataResult.error("Missing overworld dimension") : DataResult.success(worldPreset, Lifecycle.stable());
	}
}
