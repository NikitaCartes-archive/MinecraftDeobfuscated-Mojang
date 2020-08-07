package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

@Environment(EnvType.CLIENT)
public abstract class WorldPreset {
	public static final WorldPreset NORMAL = new WorldPreset("default") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false, registry), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
		}
	};
	private static final WorldPreset FLAT = new WorldPreset("flat") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(registry));
		}
	};
	private static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, true, registry), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
		}
	};
	public static final WorldPreset AMPLIFIED = new WorldPreset("amplified") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false, registry), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.AMPLIFIED));
		}
	};
	private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new NoiseBasedChunkGenerator(
				new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS)), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
			);
		}
	};
	private static final WorldPreset SINGLE_BIOME_CAVES = new WorldPreset("single_biome_caves") {
		@Override
		public WorldGenSettings create(RegistryAccess.RegistryHolder registryHolder, long l, boolean bl, boolean bl2) {
			Registry<Biome> registry = registryHolder.registryOrThrow(Registry.BIOME_REGISTRY);
			Registry<DimensionType> registry2 = registryHolder.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
			Registry<NoiseGeneratorSettings> registry3 = registryHolder.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
			return new WorldGenSettings(
				l,
				bl,
				bl2,
				WorldGenSettings.withOverworld(
					DimensionType.defaultDimensions(registry2, registry, registry3, l),
					() -> registry2.getOrThrow(DimensionType.OVERWORLD_CAVES_LOCATION),
					this.generator(registry, registry3, l)
				)
			);
		}

		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new NoiseBasedChunkGenerator(new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS)), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.CAVES));
		}
	};
	private static final WorldPreset SINGLE_BIOME_FLOATING_ISLANDS = new WorldPreset("single_biome_floating_islands") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new NoiseBasedChunkGenerator(
				new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS)), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS)
			);
		}
	};
	private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states") {
		@Override
		protected ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
			return new DebugLevelSource(registry);
		}
	};
	protected static final List<WorldPreset> PRESETS = Lists.<WorldPreset>newArrayList(
		NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, SINGLE_BIOME_SURFACE, SINGLE_BIOME_CAVES, SINGLE_BIOME_FLOATING_ISLANDS, DEBUG
	);
	protected static final Map<Optional<WorldPreset>, WorldPreset.PresetEditor> EDITORS = ImmutableMap.of(
		Optional.of(FLAT),
		(createWorldScreen, worldGenSettings) -> {
			ChunkGenerator chunkGenerator = worldGenSettings.overworld();
			return new CreateFlatWorldScreen(
				createWorldScreen,
				flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(
							new WorldGenSettings(
								worldGenSettings.seed(),
								worldGenSettings.generateFeatures(),
								worldGenSettings.generateBonusChest(),
								WorldGenSettings.withOverworld(
									createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),
									worldGenSettings.dimensions(),
									new FlatLevelSource(flatLevelGeneratorSettings)
								)
							)
						),
				chunkGenerator instanceof FlatLevelSource
					? ((FlatLevelSource)chunkGenerator).settings()
					: FlatLevelGeneratorSettings.getDefault(createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY))
			);
		},
		Optional.of(SINGLE_BIOME_SURFACE),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				createWorldScreen.worldGenSettingsComponent.registryHolder(),
				biome -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(fromBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings, SINGLE_BIOME_SURFACE, biome)),
				parseBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings)
			),
		Optional.of(SINGLE_BIOME_CAVES),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				createWorldScreen.worldGenSettingsComponent.registryHolder(),
				biome -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(fromBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings, SINGLE_BIOME_CAVES, biome)),
				parseBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings)
			),
		Optional.of(SINGLE_BIOME_FLOATING_ISLANDS),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				createWorldScreen.worldGenSettingsComponent.registryHolder(),
				biome -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(fromBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings, SINGLE_BIOME_FLOATING_ISLANDS, biome)),
				parseBuffetSettings(createWorldScreen.worldGenSettingsComponent.registryHolder(), worldGenSettings)
			)
	);
	private final Component description;

	private WorldPreset(String string) {
		this.description = new TranslatableComponent("generator." + string);
	}

	private static WorldGenSettings fromBuffetSettings(RegistryAccess registryAccess, WorldGenSettings worldGenSettings, WorldPreset worldPreset, Biome biome) {
		BiomeSource biomeSource = new FixedBiomeSource(biome);
		Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<NoiseGeneratorSettings> registry2 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		Supplier<NoiseGeneratorSettings> supplier;
		if (worldPreset == SINGLE_BIOME_CAVES) {
			supplier = () -> registry2.getOrThrow(NoiseGeneratorSettings.CAVES);
		} else if (worldPreset == SINGLE_BIOME_FLOATING_ISLANDS) {
			supplier = () -> registry2.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS);
		} else {
			supplier = () -> registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
		}

		return new WorldGenSettings(
			worldGenSettings.seed(),
			worldGenSettings.generateFeatures(),
			worldGenSettings.generateBonusChest(),
			WorldGenSettings.withOverworld(registry, worldGenSettings.dimensions(), new NoiseBasedChunkGenerator(biomeSource, worldGenSettings.seed(), supplier))
		);
	}

	private static Biome parseBuffetSettings(RegistryAccess registryAccess, WorldGenSettings worldGenSettings) {
		return (Biome)worldGenSettings.overworld()
			.getBiomeSource()
			.possibleBiomes()
			.stream()
			.findFirst()
			.orElse(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS));
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

	public WorldGenSettings create(RegistryAccess.RegistryHolder registryHolder, long l, boolean bl, boolean bl2) {
		Registry<Biome> registry = registryHolder.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionType> registry2 = registryHolder.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<NoiseGeneratorSettings> registry3 = registryHolder.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		return new WorldGenSettings(
			l,
			bl,
			bl2,
			WorldGenSettings.withOverworld(registry2, DimensionType.defaultDimensions(registry2, registry, registry3, l), this.generator(registry, registry3, l))
		);
	}

	protected abstract ChunkGenerator generator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l);

	@Environment(EnvType.CLIENT)
	public interface PresetEditor {
		Screen createEditScreen(CreateWorldScreen createWorldScreen, WorldGenSettings worldGenSettings);
	}
}
