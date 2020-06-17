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
		protected ChunkGenerator generator(long l) {
			return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
		}
	};
	private static final WorldPreset FLAT = new WorldPreset("flat") {
		@Override
		protected ChunkGenerator generator(long l) {
			return new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
		}
	};
	private static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes") {
		@Override
		protected ChunkGenerator generator(long l) {
			return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, true), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
		}
	};
	public static final WorldPreset AMPLIFIED = new WorldPreset("amplified") {
		@Override
		protected ChunkGenerator generator(long l) {
			return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false), l, NoiseGeneratorSettings.Preset.AMPLIFIED.settings());
		}
	};
	private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface") {
		@Override
		protected ChunkGenerator generator(long l) {
			return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.PLAINS), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
		}
	};
	private static final WorldPreset SINGLE_BIOME_CAVES = new WorldPreset("single_biome_caves") {
		@Override
		public WorldGenSettings create(RegistryAccess.RegistryHolder registryHolder, long l, boolean bl, boolean bl2) {
			return new WorldGenSettings(
				l, bl, bl2, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(l), DimensionType::defaultOverworldCaves, this.generator(l))
			);
		}

		@Override
		protected ChunkGenerator generator(long l) {
			return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.PLAINS), l, NoiseGeneratorSettings.Preset.CAVES.settings());
		}
	};
	private static final WorldPreset SINGLE_BIOME_FLOATING_ISLANDS = new WorldPreset("single_biome_floating_islands") {
		@Override
		protected ChunkGenerator generator(long l) {
			return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.PLAINS), l, NoiseGeneratorSettings.Preset.FLOATING_ISLANDS.settings());
		}
	};
	private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states") {
		@Override
		protected ChunkGenerator generator(long l) {
			return DebugLevelSource.INSTANCE;
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
								WorldGenSettings.withOverworld(worldGenSettings.dimensions(), new FlatLevelSource(flatLevelGeneratorSettings))
							)
						),
				chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault()
			);
		},
		Optional.of(SINGLE_BIOME_SURFACE),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				biome -> createWorldScreen.worldGenSettingsComponent.updateSettings(fromBuffetSettings(worldGenSettings, SINGLE_BIOME_SURFACE, biome)),
				parseBuffetSettings(worldGenSettings)
			),
		Optional.of(SINGLE_BIOME_CAVES),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				biome -> createWorldScreen.worldGenSettingsComponent.updateSettings(fromBuffetSettings(worldGenSettings, SINGLE_BIOME_CAVES, biome)),
				parseBuffetSettings(worldGenSettings)
			),
		Optional.of(SINGLE_BIOME_FLOATING_ISLANDS),
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				biome -> createWorldScreen.worldGenSettingsComponent.updateSettings(fromBuffetSettings(worldGenSettings, SINGLE_BIOME_FLOATING_ISLANDS, biome)),
				parseBuffetSettings(worldGenSettings)
			)
	);
	private final Component description;

	private WorldPreset(String string) {
		this.description = new TranslatableComponent("generator." + string);
	}

	private static WorldGenSettings fromBuffetSettings(WorldGenSettings worldGenSettings, WorldPreset worldPreset, Biome biome) {
		BiomeSource biomeSource = new FixedBiomeSource(biome);
		NoiseGeneratorSettings noiseGeneratorSettings;
		if (worldPreset == SINGLE_BIOME_CAVES) {
			noiseGeneratorSettings = NoiseGeneratorSettings.Preset.CAVES.settings();
		} else if (worldPreset == SINGLE_BIOME_FLOATING_ISLANDS) {
			noiseGeneratorSettings = NoiseGeneratorSettings.Preset.FLOATING_ISLANDS.settings();
		} else {
			noiseGeneratorSettings = NoiseGeneratorSettings.Preset.OVERWORLD.settings();
		}

		return new WorldGenSettings(
			worldGenSettings.seed(),
			worldGenSettings.generateFeatures(),
			worldGenSettings.generateBonusChest(),
			WorldGenSettings.withOverworld(worldGenSettings.dimensions(), new NoiseBasedChunkGenerator(biomeSource, worldGenSettings.seed(), noiseGeneratorSettings))
		);
	}

	private static Biome parseBuffetSettings(WorldGenSettings worldGenSettings) {
		return (Biome)worldGenSettings.overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(Biomes.PLAINS);
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
		return new WorldGenSettings(l, bl, bl2, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(l), this.generator(l)));
	}

	protected abstract ChunkGenerator generator(long l);

	@Environment(EnvType.CLIENT)
	public interface PresetEditor {
		Screen createEditScreen(CreateWorldScreen createWorldScreen, WorldGenSettings worldGenSettings);
	}
}
