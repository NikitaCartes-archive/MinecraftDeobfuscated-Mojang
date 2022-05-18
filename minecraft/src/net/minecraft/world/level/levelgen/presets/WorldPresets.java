package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class WorldPresets {
	public static final ResourceKey<WorldPreset> NORMAL = register("normal");
	public static final ResourceKey<WorldPreset> FLAT = register("flat");
	public static final ResourceKey<WorldPreset> LARGE_BIOMES = register("large_biomes");
	public static final ResourceKey<WorldPreset> AMPLIFIED = register("amplified");
	public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = register("single_biome_surface");
	public static final ResourceKey<WorldPreset> DEBUG = register("debug_all_block_states");

	public static Holder<WorldPreset> bootstrap(Registry<WorldPreset> registry) {
		return new WorldPresets.Bootstrap(registry).run();
	}

	private static ResourceKey<WorldPreset> register(String string) {
		return ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, new ResourceLocation(string));
	}

	public static Optional<ResourceKey<WorldPreset>> fromSettings(WorldGenSettings worldGenSettings) {
		ChunkGenerator chunkGenerator = worldGenSettings.overworld();
		if (chunkGenerator instanceof FlatLevelSource) {
			return Optional.of(FLAT);
		} else {
			return chunkGenerator instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
		}
	}

	public static WorldGenSettings createNormalWorldFromPreset(RegistryAccess registryAccess, long l, boolean bl, boolean bl2) {
		return registryAccess.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolderOrThrow(NORMAL).value().createWorldGenSettings(l, bl, bl2);
	}

	public static WorldGenSettings createNormalWorldFromPreset(RegistryAccess registryAccess, long l) {
		return createNormalWorldFromPreset(registryAccess, l, true, false);
	}

	public static WorldGenSettings createNormalWorldFromPreset(RegistryAccess registryAccess) {
		return createNormalWorldFromPreset(registryAccess, RandomSource.create().nextLong());
	}

	public static WorldGenSettings demoSettings(RegistryAccess registryAccess) {
		return createNormalWorldFromPreset(registryAccess, (long)"North Carolina".hashCode(), true, true);
	}

	public static LevelStem getNormalOverworld(RegistryAccess registryAccess) {
		return registryAccess.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolderOrThrow(NORMAL).value().overworldOrThrow();
	}

	static class Bootstrap {
		private final Registry<WorldPreset> presets;
		private final Registry<DimensionType> dimensionTypes = BuiltinRegistries.DIMENSION_TYPE;
		private final Registry<Biome> biomes = BuiltinRegistries.BIOME;
		private final Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
		private final Registry<NoiseGeneratorSettings> noiseSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
		private final Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;
		private final Holder<DimensionType> overworldDimensionType = this.dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
		private final Holder<DimensionType> netherDimensionType = this.dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.NETHER);
		private final Holder<NoiseGeneratorSettings> netherNoiseSettings = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.NETHER);
		private final LevelStem netherStem = new LevelStem(
			this.netherDimensionType,
			new NoiseBasedChunkGenerator(this.structureSets, this.noises, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(this.biomes), this.netherNoiseSettings)
		);
		private final Holder<DimensionType> endDimensionType = this.dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.END);
		private final Holder<NoiseGeneratorSettings> endNoiseSettings = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.END);
		private final LevelStem endStem = new LevelStem(
			this.endDimensionType, new NoiseBasedChunkGenerator(this.structureSets, this.noises, new TheEndBiomeSource(this.biomes), this.endNoiseSettings)
		);

		Bootstrap(Registry<WorldPreset> registry) {
			this.presets = registry;
		}

		private LevelStem makeOverworld(ChunkGenerator chunkGenerator) {
			return new LevelStem(this.overworldDimensionType, chunkGenerator);
		}

		private LevelStem makeNoiseBasedOverworld(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
			return this.makeOverworld(new NoiseBasedChunkGenerator(this.structureSets, this.noises, biomeSource, holder));
		}

		private WorldPreset createPresetWithCustomOverworld(LevelStem levelStem) {
			return new WorldPreset(Map.of(LevelStem.OVERWORLD, levelStem, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
		}

		private Holder<WorldPreset> registerCustomOverworldPreset(ResourceKey<WorldPreset> resourceKey, LevelStem levelStem) {
			return BuiltinRegistries.register(this.presets, resourceKey, this.createPresetWithCustomOverworld(levelStem));
		}

		public Holder<WorldPreset> run() {
			MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(this.biomes);
			Holder<NoiseGeneratorSettings> holder = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
			this.registerCustomOverworldPreset(WorldPresets.NORMAL, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder));
			Holder<NoiseGeneratorSettings> holder2 = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
			this.registerCustomOverworldPreset(WorldPresets.LARGE_BIOMES, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder2));
			Holder<NoiseGeneratorSettings> holder3 = this.noiseSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.AMPLIFIED);
			this.registerCustomOverworldPreset(WorldPresets.AMPLIFIED, this.makeNoiseBasedOverworld(multiNoiseBiomeSource, holder3));
			this.registerCustomOverworldPreset(
				WorldPresets.SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new FixedBiomeSource(this.biomes.getOrCreateHolderOrThrow(Biomes.PLAINS)), holder)
			);
			this.registerCustomOverworldPreset(
				WorldPresets.FLAT, this.makeOverworld(new FlatLevelSource(this.structureSets, FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets)))
			);
			return this.registerCustomOverworldPreset(WorldPresets.DEBUG, this.makeOverworld(new DebugLevelSource(this.structureSets, this.biomes)));
		}
	}
}
