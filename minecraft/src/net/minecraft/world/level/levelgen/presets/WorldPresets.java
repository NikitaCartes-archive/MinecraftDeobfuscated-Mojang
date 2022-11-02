package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldPresets {
	public static final ResourceKey<WorldPreset> NORMAL = register("normal");
	public static final ResourceKey<WorldPreset> FLAT = register("flat");
	public static final ResourceKey<WorldPreset> LARGE_BIOMES = register("large_biomes");
	public static final ResourceKey<WorldPreset> AMPLIFIED = register("amplified");
	public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = register("single_biome_surface");
	public static final ResourceKey<WorldPreset> DEBUG = register("debug_all_block_states");

	public static void bootstrap(BootstapContext<WorldPreset> bootstapContext) {
		new WorldPresets.Bootstrap(bootstapContext).run();
	}

	private static ResourceKey<WorldPreset> register(String string) {
		return ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, new ResourceLocation(string));
	}

	public static Optional<ResourceKey<WorldPreset>> fromSettings(Registry<LevelStem> registry) {
		return registry.getOptional(LevelStem.OVERWORLD).flatMap(levelStem -> {
			ChunkGenerator chunkGenerator = levelStem.generator();
			if (chunkGenerator instanceof FlatLevelSource) {
				return Optional.of(FLAT);
			} else {
				return chunkGenerator instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
			}
		});
	}

	public static WorldDimensions createNormalWorldDimensions(RegistryAccess registryAccess) {
		return registryAccess.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolderOrThrow(NORMAL).value().createWorldDimensions();
	}

	public static LevelStem getNormalOverworld(RegistryAccess registryAccess) {
		return (LevelStem)registryAccess.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolderOrThrow(NORMAL).value().overworld().orElseThrow();
	}

	static class Bootstrap {
		private final BootstapContext<WorldPreset> context;
		private final HolderGetter<NoiseGeneratorSettings> noiseSettings;
		private final HolderGetter<Biome> biomes;
		private final HolderGetter<PlacedFeature> placedFeatures;
		private final HolderGetter<StructureSet> structureSets;
		private final Holder<DimensionType> overworldDimensionType;
		private final LevelStem netherStem;
		private final LevelStem endStem;

		Bootstrap(BootstapContext<WorldPreset> bootstapContext) {
			this.context = bootstapContext;
			HolderGetter<DimensionType> holderGetter = bootstapContext.lookup(Registry.DIMENSION_TYPE_REGISTRY);
			this.noiseSettings = bootstapContext.lookup(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
			this.biomes = bootstapContext.lookup(Registry.BIOME_REGISTRY);
			this.placedFeatures = bootstapContext.lookup(Registry.PLACED_FEATURE_REGISTRY);
			this.structureSets = bootstapContext.lookup(Registry.STRUCTURE_SET_REGISTRY);
			this.overworldDimensionType = holderGetter.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
			Holder<DimensionType> holder = holderGetter.getOrThrow(BuiltinDimensionTypes.NETHER);
			Holder<NoiseGeneratorSettings> holder2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
			this.netherStem = new LevelStem(holder, new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(this.biomes), holder2));
			Holder<DimensionType> holder3 = holderGetter.getOrThrow(BuiltinDimensionTypes.END);
			Holder<NoiseGeneratorSettings> holder4 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
			this.endStem = new LevelStem(holder3, new NoiseBasedChunkGenerator(TheEndBiomeSource.create(this.biomes), holder4));
		}

		private LevelStem makeOverworld(ChunkGenerator chunkGenerator) {
			return new LevelStem(this.overworldDimensionType, chunkGenerator);
		}

		private LevelStem makeNoiseBasedOverworld(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
			return this.makeOverworld(new NoiseBasedChunkGenerator(biomeSource, holder));
		}

		private WorldPreset createPresetWithCustomOverworld(LevelStem levelStem) {
			return new WorldPreset(Map.of(LevelStem.OVERWORLD, levelStem, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
		}

		private void registerCustomOverworldPreset(ResourceKey<WorldPreset> resourceKey, LevelStem levelStem) {
			this.context.register(resourceKey, this.createPresetWithCustomOverworld(levelStem));
		}

		public void run() {
			MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(this.biomes);
			Holder<NoiseGeneratorSettings> holder = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
			this.registerCustomOverworldPreset(WorldPresets.NORMAL, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder));
			Holder<NoiseGeneratorSettings> holder2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
			this.registerCustomOverworldPreset(WorldPresets.LARGE_BIOMES, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder2));
			Holder<NoiseGeneratorSettings> holder3 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
			this.registerCustomOverworldPreset(WorldPresets.AMPLIFIED, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder3));
			Holder.Reference<Biome> reference = this.biomes.getOrThrow(Biomes.PLAINS);
			this.registerCustomOverworldPreset(WorldPresets.SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new FixedBiomeSource(reference), holder));
			this.registerCustomOverworldPreset(
				WorldPresets.FLAT, this.makeOverworld(new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets, this.placedFeatures)))
			);
			this.registerCustomOverworldPreset(WorldPresets.DEBUG, this.makeOverworld(new DebugLevelSource(reference)));
		}
	}
}
