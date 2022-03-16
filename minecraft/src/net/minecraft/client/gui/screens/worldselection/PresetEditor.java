package net.minecraft.client.gui.screens.worldselection;

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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

@Environment(EnvType.CLIENT)
public interface PresetEditor {
	Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(
		Optional.of(WorldPresets.FLAT),
		(PresetEditor)(createWorldScreen, worldCreationContext) -> {
			ChunkGenerator chunkGenerator = worldCreationContext.worldGenSettings().overworld();
			RegistryAccess registryAccess = worldCreationContext.registryAccess();
			Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
			Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
			return new CreateFlatWorldScreen(
				createWorldScreen,
				flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent.updateSettings(flatWorldConfigurator(flatLevelGeneratorSettings)),
				chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault(registry, registry2)
			);
		},
		Optional.of(WorldPresets.SINGLE_BIOME_SURFACE),
		(PresetEditor)(createWorldScreen, worldCreationContext) -> new CreateBuffetWorldScreen(
				createWorldScreen, worldCreationContext, holder -> createWorldScreen.worldGenSettingsComponent.updateSettings(fixedBiomeConfigurator(holder))
			)
	);

	Screen createEditScreen(CreateWorldScreen createWorldScreen, WorldCreationContext worldCreationContext);

	private static WorldCreationContext.Updater flatWorldConfigurator(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		return (frozen, worldGenSettings) -> {
			Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
			ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
			return WorldGenSettings.replaceOverworldGenerator(frozen, worldGenSettings, chunkGenerator);
		};
	}

	private static WorldCreationContext.Updater fixedBiomeConfigurator(Holder<Biome> holder) {
		return (frozen, worldGenSettings) -> {
			Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
			Registry<NoiseGeneratorSettings> registry2 = frozen.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
			Registry<NormalNoise.NoiseParameters> registry3 = frozen.registryOrThrow(Registry.NOISE_REGISTRY);
			Holder<NoiseGeneratorSettings> holder2 = registry2.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD);
			BiomeSource biomeSource = new FixedBiomeSource(holder);
			ChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(registry, registry3, biomeSource, holder2);
			return WorldGenSettings.replaceOverworldGenerator(frozen, worldGenSettings, chunkGenerator);
		};
	}
}
