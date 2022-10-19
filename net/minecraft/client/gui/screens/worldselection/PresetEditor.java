/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
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
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

@Environment(value=EnvType.CLIENT)
public interface PresetEditor {
    public static final Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(Optional.of(WorldPresets.FLAT), (createWorldScreen, worldCreationContext) -> {
        ChunkGenerator chunkGenerator = worldCreationContext.selectedDimensions().overworld();
        RegistryAccess.Frozen registryAccess = worldCreationContext.worldgenLoadContext();
        Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        return new CreateFlatWorldScreen(createWorldScreen, flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent.updateSettings(PresetEditor.flatWorldConfigurator(flatLevelGeneratorSettings)), chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault(registry, registry2));
    }, Optional.of(WorldPresets.SINGLE_BIOME_SURFACE), (createWorldScreen, worldCreationContext) -> new CreateBuffetWorldScreen(createWorldScreen, worldCreationContext, holder -> createWorldScreen.worldGenSettingsComponent.updateSettings(PresetEditor.fixedBiomeConfigurator(holder))));

    public Screen createEditScreen(CreateWorldScreen var1, WorldCreationContext var2);

    private static WorldCreationContext.DimensionsUpdater flatWorldConfigurator(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return (frozen, worldDimensions) -> {
            Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            FlatLevelSource chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
            return worldDimensions.replaceOverworldGenerator((RegistryAccess)frozen, chunkGenerator);
        };
    }

    private static WorldCreationContext.DimensionsUpdater fixedBiomeConfigurator(Holder<Biome> holder) {
        return (frozen, worldDimensions) -> {
            Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            Registry<NoiseGeneratorSettings> registry2 = frozen.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
            Registry<NormalNoise.NoiseParameters> registry3 = frozen.registryOrThrow(Registry.NOISE_REGISTRY);
            Holder.Reference<NoiseGeneratorSettings> holder2 = registry2.getOrCreateHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
            FixedBiomeSource biomeSource = new FixedBiomeSource(holder);
            NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(registry, registry3, (BiomeSource)biomeSource, holder2);
            return worldDimensions.replaceOverworldGenerator((RegistryAccess)frozen, chunkGenerator);
        };
    }
}

