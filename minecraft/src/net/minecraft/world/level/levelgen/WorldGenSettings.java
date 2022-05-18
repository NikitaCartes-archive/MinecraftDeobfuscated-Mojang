package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Map.Entry;
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
import org.apache.commons.lang3.StringUtils;

public class WorldGenSettings {
	public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
						Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateStructures),
						Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest),
						RegistryCodecs.dataPackAwareCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC)
							.xmap(LevelStem::sortMap, Function.identity())
							.fieldOf("dimensions")
							.forGetter(WorldGenSettings::dimensions),
						Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)
					)
					.apply(instance, instance.stable(WorldGenSettings::new))
		)
		.comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
	private final long seed;
	private final boolean generateStructures;
	private final boolean generateBonusChest;
	private final Registry<LevelStem> dimensions;
	private final Optional<String> legacyCustomOptions;

	private DataResult<WorldGenSettings> guardExperimental() {
		LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
		if (levelStem == null) {
			return DataResult.error("Overworld settings missing");
		} else {
			return this.stable() ? DataResult.success(this, Lifecycle.stable()) : DataResult.success(this);
		}
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
		Registry<LevelStem> registry2 = withOverworld(registry, worldGenSettings.dimensions(), chunkGenerator);
		return new WorldGenSettings(worldGenSettings.seed(), worldGenSettings.generateStructures(), worldGenSettings.generateBonusChest(), registry2);
	}

	public static Registry<LevelStem> withOverworld(Registry<DimensionType> registry, Registry<LevelStem> registry2, ChunkGenerator chunkGenerator) {
		LevelStem levelStem = registry2.get(LevelStem.OVERWORLD);
		Holder<DimensionType> holder = levelStem == null ? registry.getOrCreateHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : levelStem.typeHolder();
		return withOverworld(registry2, holder, chunkGenerator);
	}

	public static Registry<LevelStem> withOverworld(Registry<LevelStem> registry, Holder<DimensionType> holder, ChunkGenerator chunkGenerator) {
		WritableRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
		writableRegistry.register(LevelStem.OVERWORLD, new LevelStem(holder, chunkGenerator), Lifecycle.stable());

		for (Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
			ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
			if (resourceKey != LevelStem.OVERWORLD) {
				writableRegistry.register(resourceKey, (LevelStem)entry.getValue(), registry.lifecycle((LevelStem)entry.getValue()));
			}
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
		} else {
			return levelStem.generator();
		}
	}

	public ImmutableSet<ResourceKey<Level>> levels() {
		return (ImmutableSet<ResourceKey<Level>>)this.dimensions()
			.entrySet()
			.stream()
			.map(Entry::getKey)
			.map(WorldGenSettings::levelStemToLevel)
			.collect(ImmutableSet.toImmutableSet());
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
		long l = optionalLong.orElse(this.seed);
		Registry<LevelStem> registry;
		if (optionalLong.isPresent()) {
			WritableRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);

			for (Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
				ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
				writableRegistry.register(
					resourceKey,
					new LevelStem(((LevelStem)entry.getValue()).typeHolder(), ((LevelStem)entry.getValue()).generator()),
					this.dimensions.lifecycle((LevelStem)entry.getValue())
				);
			}

			registry = writableRegistry;
		} else {
			registry = this.dimensions;
		}

		WorldGenSettings worldGenSettings;
		if (this.isDebug()) {
			worldGenSettings = new WorldGenSettings(l, false, false, registry);
		} else {
			worldGenSettings = new WorldGenSettings(l, this.generateStructures(), this.generateBonusChest() && !bl, registry);
		}

		return worldGenSettings;
	}

	public static OptionalLong parseSeed(String string) {
		string = string.trim();
		if (StringUtils.isEmpty(string)) {
			return OptionalLong.empty();
		} else {
			try {
				return OptionalLong.of(Long.parseLong(string));
			} catch (NumberFormatException var2) {
				return OptionalLong.of((long)string.hashCode());
			}
		}
	}
}
