package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;

@Environment(EnvType.CLIENT)
public interface PresetEditor {
	Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(
		Optional.of(WorldPresets.FLAT),
		(PresetEditor)(createWorldScreen, worldCreationContext) -> {
			ChunkGenerator chunkGenerator = worldCreationContext.selectedDimensions().overworld();
			RegistryAccess registryAccess = worldCreationContext.worldgenLoadContext();
			HolderGetter<Biome> holderGetter = registryAccess.lookupOrThrow(Registries.BIOME);
			HolderGetter<StructureSet> holderGetter2 = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
			HolderGetter<PlacedFeature> holderGetter3 = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);
			return new CreateFlatWorldScreen(
				createWorldScreen,
				flatLevelGeneratorSettings -> createWorldScreen.getUiState().updateDimensions(flatWorldConfigurator(flatLevelGeneratorSettings)),
				chunkGenerator instanceof FlatLevelSource
					? ((FlatLevelSource)chunkGenerator).settings()
					: FlatLevelGeneratorSettings.getDefault(holderGetter, holderGetter2, holderGetter3)
			);
		},
		Optional.of(WorldPresets.SINGLE_BIOME_SURFACE),
		(PresetEditor)(createWorldScreen, worldCreationContext) -> new CreateBuffetWorldScreen(
				createWorldScreen, worldCreationContext, holder -> createWorldScreen.getUiState().updateDimensions(fixedBiomeConfigurator(holder))
			)
	);

	Screen createEditScreen(CreateWorldScreen createWorldScreen, WorldCreationContext worldCreationContext);

	private static WorldCreationContext.DimensionsUpdater flatWorldConfigurator(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		return (frozen, worldDimensions) -> {
			ChunkGenerator chunkGenerator = new FlatLevelSource(flatLevelGeneratorSettings);
			return worldDimensions.replaceOverworldGenerator(frozen, chunkGenerator);
		};
	}

	private static WorldCreationContext.DimensionsUpdater fixedBiomeConfigurator(Holder<Biome> holder) {
		return (frozen, worldDimensions) -> {
			Registry<NoiseGeneratorSettings> registry = frozen.lookupOrThrow(Registries.NOISE_SETTINGS);
			Holder<NoiseGeneratorSettings> holder2 = registry.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
			BiomeSource biomeSource = new FixedBiomeSource(holder);
			ChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(biomeSource, holder2);
			return worldDimensions.replaceOverworldGenerator(frozen, chunkGenerator);
		};
	}
}
