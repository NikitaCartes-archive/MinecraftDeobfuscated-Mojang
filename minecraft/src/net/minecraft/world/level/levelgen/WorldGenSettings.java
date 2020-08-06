package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
	public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
						Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateFeatures),
						Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest),
						MappedRegistry.dataPackCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC)
							.xmap(LevelStem::sortMap, Function.identity())
							.fieldOf("dimensions")
							.forGetter(WorldGenSettings::dimensions),
						Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)
					)
					.apply(instance, instance.stable(WorldGenSettings::new))
		)
		.comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
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
		} else {
			return this.stable() ? DataResult.success(this, Lifecycle.stable()) : DataResult.success(this);
		}
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
		Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		int i = "North Carolina".hashCode();
		Registry<DimensionType> registry2 = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<NoiseGeneratorSettings> registry3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		return new WorldGenSettings(
			(long)i,
			true,
			true,
			withOverworld(registry2, DimensionType.defaultDimensions(registry2, registry, registry3, (long)i), makeDefaultOverworld(registry, registry3, (long)i))
		);
	}

	public static WorldGenSettings makeDefault(Registry<DimensionType> registry, Registry<Biome> registry2, Registry<NoiseGeneratorSettings> registry3) {
		long l = new Random().nextLong();
		return new WorldGenSettings(
			l, true, false, withOverworld(registry, DimensionType.defaultDimensions(registry, registry2, registry3, l), makeDefaultOverworld(registry2, registry3, l))
		);
	}

	public static NoiseBasedChunkGenerator makeDefaultOverworld(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
		return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false, registry), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
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

	public static MappedRegistry<LevelStem> withOverworld(
		Registry<DimensionType> registry, MappedRegistry<LevelStem> mappedRegistry, ChunkGenerator chunkGenerator
	) {
		LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
		Supplier<DimensionType> supplier = () -> levelStem == null ? registry.getOrThrow(DimensionType.OVERWORLD_LOCATION) : levelStem.type();
		return withOverworld(mappedRegistry, supplier, chunkGenerator);
	}

	public static MappedRegistry<LevelStem> withOverworld(
		MappedRegistry<LevelStem> mappedRegistry, Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator
	) {
		MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		mappedRegistry2.register(LevelStem.OVERWORLD, new LevelStem(supplier, chunkGenerator), Lifecycle.stable());

		for (Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
			ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
			if (resourceKey != LevelStem.OVERWORLD) {
				mappedRegistry2.register(resourceKey, entry.getValue(), mappedRegistry.lifecycle((LevelStem)entry.getValue()));
			}
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
		} else {
			return levelStem.generator();
		}
	}

	public ImmutableSet<ResourceKey<Level>> levels() {
		return (ImmutableSet<ResourceKey<Level>>)this.dimensions()
			.entrySet()
			.stream()
			.map(entry -> ResourceKey.create(Registry.DIMENSION_REGISTRY, ((ResourceKey)entry.getKey()).location()))
			.collect(ImmutableSet.toImmutableSet());
	}

	public boolean isDebug() {
		return this.overworld() instanceof DebugLevelSource;
	}

	public boolean isFlatWorld() {
		return this.overworld() instanceof FlatLevelSource;
	}

	@Environment(EnvType.CLIENT)
	public boolean isOldCustomizedWorld() {
		return this.legacyCustomOptions.isPresent();
	}

	public WorldGenSettings withBonusChest() {
		return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withFeaturesToggled() {
		return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withBonusChestToggled() {
		return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
	}

	public static WorldGenSettings create(RegistryAccess registryAccess, Properties properties) {
		String string = MoreObjects.firstNonNull((String)properties.get("generator-settings"), "");
		properties.put("generator-settings", string);
		String string2 = MoreObjects.firstNonNull((String)properties.get("level-seed"), "");
		properties.put("level-seed", string2);
		String string3 = (String)properties.get("generate-structures");
		boolean bl = string3 == null || Boolean.parseBoolean(string3);
		properties.put("generate-structures", Objects.toString(bl));
		String string4 = (String)properties.get("level-type");
		String string5 = (String)Optional.ofNullable(string4).map(stringx -> stringx.toLowerCase(Locale.ROOT)).orElse("default");
		properties.put("level-type", string5);
		long l = new Random().nextLong();
		if (!string2.isEmpty()) {
			try {
				long m = Long.parseLong(string2);
				if (m != 0L) {
					l = m;
				}
			} catch (NumberFormatException var18) {
				l = (long)string2.hashCode();
			}
		}

		Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<NoiseGeneratorSettings> registry3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		MappedRegistry<LevelStem> mappedRegistry = DimensionType.defaultDimensions(registry, registry2, registry3, l);
		switch (string5) {
			case "flat":
				JsonObject jsonObject = !string.isEmpty() ? GsonHelper.parse(string) : new JsonObject();
				Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonObject);
				return new WorldGenSettings(
					l,
					bl,
					false,
					withOverworld(
						registry,
						mappedRegistry,
						new FlatLevelSource(
							(FlatLevelGeneratorSettings)FlatLevelGeneratorSettings.CODEC
								.parse(dynamic)
								.resultOrPartial(LOGGER::error)
								.orElseGet(() -> FlatLevelGeneratorSettings.getDefault(registry2))
						)
					)
				);
			case "debug_all_block_states":
				return new WorldGenSettings(l, bl, false, withOverworld(registry, mappedRegistry, new DebugLevelSource(registry2)));
			case "amplified":
				return new WorldGenSettings(
					l,
					bl,
					false,
					withOverworld(
						registry,
						mappedRegistry,
						new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false, registry2), l, () -> registry3.getOrThrow(NoiseGeneratorSettings.AMPLIFIED))
					)
				);
			case "largebiomes":
				return new WorldGenSettings(
					l,
					bl,
					false,
					withOverworld(
						registry,
						mappedRegistry,
						new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, true, registry2), l, () -> registry3.getOrThrow(NoiseGeneratorSettings.OVERWORLD))
					)
				);
			default:
				return new WorldGenSettings(l, bl, false, withOverworld(registry, mappedRegistry, makeDefaultOverworld(registry2, registry3, l)));
		}
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
		long l = optionalLong.orElse(this.seed);
		MappedRegistry<LevelStem> mappedRegistry;
		if (optionalLong.isPresent()) {
			mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
			long m = optionalLong.getAsLong();

			for (Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
				ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
				mappedRegistry.register(
					resourceKey,
					new LevelStem(((LevelStem)entry.getValue()).typeSupplier(), ((LevelStem)entry.getValue()).generator().withSeed(m)),
					this.dimensions.lifecycle((LevelStem)entry.getValue())
				);
			}
		} else {
			mappedRegistry = this.dimensions;
		}

		WorldGenSettings worldGenSettings;
		if (this.isDebug()) {
			worldGenSettings = new WorldGenSettings(l, false, false, mappedRegistry);
		} else {
			worldGenSettings = new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, mappedRegistry);
		}

		return worldGenSettings;
	}
}
