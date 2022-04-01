package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

@Environment(EnvType.CLIENT)
public abstract class WorldPreset {
	public static final WorldPreset NORMAL = new WorldPreset("default") {
		@Override
		protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
			return WorldGenSettings.makeDefaultOverworld(registryAccess, l);
		}
	};
	private static final WorldPreset FLAT = new WorldPreset("flat") {
		@Override
		protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
			Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
			Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
			return new FlatLevelSource(registry2, FlatLevelGeneratorSettings.getDefault(registry, registry2));
		}
	};
	public static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes") {
		@Override
		protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
			return WorldGenSettings.makeOverworld(registryAccess, l, NoiseGeneratorSettings.LARGE_BIOMES);
		}
	};
	public static final WorldPreset AMPLIFIED = new WorldPreset("amplified") {
		@Override
		protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
			return WorldGenSettings.makeOverworld(registryAccess, l, NoiseGeneratorSettings.AMPLIFIED);
		}
	};
	private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface") {
		@Override
		protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
			return WorldPreset.fixedBiomeGenerator(registryAccess, l, NoiseGeneratorSettings.OVERWORLD);
		}
	};
	private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states") {
		@Override
		protected ChunkGenerator generator(RegistryAccess registryAccess, long l) {
			return new DebugLevelSource(registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY), registryAccess.registryOrThrow(Registry.BIOME_REGISTRY));
		}
	};
	protected static final List<WorldPreset> PRESETS = Lists.<WorldPreset>newArrayList(NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, SINGLE_BIOME_SURFACE, DEBUG);
	protected static final Map<Optional<WorldPreset>, WorldPreset.PresetEditor> EDITORS = ImmutableMap.of(
		Optional.of(FLAT),
		(createWorldScreen, worldGenSettings) -> {
			ChunkGenerator chunkGenerator = worldGenSettings.overworld();
			RegistryAccess registryAccess = createWorldScreen.worldGenSettingsComponent.registryHolder();
			Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
			Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
			Registry<DimensionType> registry3 = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
			return new CreateFlatWorldScreen(
				createWorldScreen,
				flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(
							new WorldGenSettings(
								worldGenSettings.seed(),
								worldGenSettings.generateFeatures(),
								worldGenSettings.generateBonusChest(),
								WorldGenSettings.withOverworld(registry3, worldGenSettings.dimensions(), new FlatLevelSource(registry2, flatLevelGeneratorSettings))
							)
						),
				chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault(registry, registry2)
			);
		},
		Optional.of(SINGLE_BIOME_SURFACE),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				createWorldScreen.worldGenSettingsComponent.registryHolder(),
				holder -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(fromBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings, holder)),
				parseBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings)
			)
	);
	private final Component description;

	static NoiseBasedChunkGenerator fixedBiomeGenerator(RegistryAccess registryAccess, long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
		Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		Registry<NormalNoise.NoiseParameters> registry3 = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);
		Registry<NoiseGeneratorSettings> registry4 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		return new NoiseBasedChunkGenerator(
			registry2, registry3, new FixedBiomeSource(registry.getOrCreateHolder(Biomes.PLAINS)), l, registry4.getOrCreateHolder(resourceKey)
		);
	}

	WorldPreset(String string) {
		this.description = new TranslatableComponent("generator." + string);
	}

	private static WorldGenSettings fromBuffetSettings(RegistryAccess registryAccess, WorldGenSettings worldGenSettings, Holder<Biome> holder) {
		BiomeSource biomeSource = new FixedBiomeSource(holder);
		Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		Registry<NoiseGeneratorSettings> registry3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		Holder<NoiseGeneratorSettings> holder2 = registry3.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD);
		return new WorldGenSettings(
			worldGenSettings.seed(),
			worldGenSettings.generateFeatures(),
			worldGenSettings.generateBonusChest(),
			WorldGenSettings.withOverworld(
				registry,
				worldGenSettings.dimensions(),
				new NoiseBasedChunkGenerator(registry2, registryAccess.registryOrThrow(Registry.NOISE_REGISTRY), biomeSource, worldGenSettings.seed(), holder2)
			)
		);
	}

	private static Holder<Biome> parseBuffetSettings(RegistryAccess registryAccess, WorldGenSettings worldGenSettings) {
		return (Holder<Biome>)worldGenSettings.overworld()
			.getBiomeSource()
			.possibleBiomes()
			.stream()
			.findFirst()
			.orElse(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY).getOrCreateHolder(Biomes.PLAINS));
	}

	public static Optional<WorldPreset> of(WorldGenSettings worldGenSettings) {
		ChunkGenerator chunkGenerator = worldGenSettings.overworld();
		if (chunkGenerator instanceof FlatLevelSource) {
			return Optional.of(FLAT);
		} else {
			return chunkGenerator instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
		}
	}

	public Component description() {
		return this.description;
	}

	public WorldGenSettings create(RegistryAccess registryAccess, long l, boolean bl, boolean bl2) {
		return new WorldGenSettings(
			l,
			bl,
			bl2,
			WorldGenSettings.withOverworld(
				registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), DimensionType.defaultDimensions(registryAccess, l), this.generator(registryAccess, l)
			)
		);
	}

	protected abstract ChunkGenerator generator(RegistryAccess registryAccess, long l);

	public static boolean isVisibleByDefault(WorldPreset worldPreset) {
		return worldPreset != DEBUG;
	}

	@Environment(EnvType.CLIENT)
	public interface PresetEditor {
		Screen createEditScreen(CreateWorldScreen createWorldScreen, WorldGenSettings worldGenSettings);
	}
}
