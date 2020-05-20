/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
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

@Environment(value=EnvType.CLIENT)
public abstract class WorldPreset {
    public static final WorldPreset NORMAL = new WorldPreset("default"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        }
    };
    private static final WorldPreset FLAT = new WorldPreset("flat"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
        }
    };
    private static final WorldPreset LARGE_BIOMES = new WorldPreset("large_biomes"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, true), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        }
    };
    public static final WorldPreset AMPLIFIED = new WorldPreset("amplified"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false), l, NoiseGeneratorSettings.Preset.AMPLIFIED.settings());
        }
    };
    private static final WorldPreset SINGLE_BIOME_SURFACE = new WorldPreset("single_biome_surface"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.OCEAN), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        }
    };
    private static final WorldPreset SINGLE_BIOME_CAVES = new WorldPreset("single_biome_caves"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.OCEAN), l, NoiseGeneratorSettings.Preset.NETHER.settings());
        }
    };
    private static final WorldPreset SINGLE_BIOME_FLOATING_ISLANDS = new WorldPreset("single_biome_floating_islands"){

        @Override
        protected ChunkGenerator generator(long l) {
            return new NoiseBasedChunkGenerator(new FixedBiomeSource(Biomes.OCEAN), l, NoiseGeneratorSettings.Preset.END.settings());
        }
    };
    private static final WorldPreset DEBUG = new WorldPreset("debug_all_block_states"){

        @Override
        protected ChunkGenerator generator(long l) {
            return DebugLevelSource.INSTANCE;
        }
    };
    protected static final List<WorldPreset> PRESETS = Lists.newArrayList(NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, SINGLE_BIOME_SURFACE, SINGLE_BIOME_CAVES, SINGLE_BIOME_FLOATING_ISLANDS, DEBUG);
    protected static final Map<Optional<WorldPreset>, PresetEditor> EDITORS = ImmutableMap.of(Optional.of(FLAT), (createWorldScreen, worldGenSettings) -> {
        ChunkGenerator chunkGenerator = worldGenSettings.overworld();
        return new CreateFlatWorldScreen(createWorldScreen, flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent.updateSettings(new WorldGenSettings(worldGenSettings.seed(), worldGenSettings.generateFeatures(), worldGenSettings.generateBonusChest(), WorldGenSettings.withOverworld(worldGenSettings.dimensions(), new FlatLevelSource((FlatLevelGeneratorSettings)flatLevelGeneratorSettings)))), chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault());
    }, Optional.of(SINGLE_BIOME_SURFACE), (createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(createWorldScreen, biome -> createWorldScreen.worldGenSettingsComponent.updateSettings(WorldPreset.fromBuffetSettings(worldGenSettings, SINGLE_BIOME_SURFACE, biome)), WorldPreset.parseBuffetSettings(worldGenSettings)), Optional.of(SINGLE_BIOME_CAVES), (createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(createWorldScreen, biome -> createWorldScreen.worldGenSettingsComponent.updateSettings(WorldPreset.fromBuffetSettings(worldGenSettings, SINGLE_BIOME_CAVES, biome)), WorldPreset.parseBuffetSettings(worldGenSettings)), Optional.of(SINGLE_BIOME_FLOATING_ISLANDS), (createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(createWorldScreen, biome -> createWorldScreen.worldGenSettingsComponent.updateSettings(WorldPreset.fromBuffetSettings(worldGenSettings, SINGLE_BIOME_FLOATING_ISLANDS, biome)), WorldPreset.parseBuffetSettings(worldGenSettings)));
    private final Component description;

    private WorldPreset(String string) {
        this.description = new TranslatableComponent("generator." + string);
    }

    private static WorldGenSettings fromBuffetSettings(WorldGenSettings worldGenSettings, WorldPreset worldPreset, Biome biome) {
        FixedBiomeSource biomeSource = new FixedBiomeSource(biome);
        NoiseGeneratorSettings noiseGeneratorSettings = worldPreset == SINGLE_BIOME_CAVES ? NoiseGeneratorSettings.Preset.NETHER.settings() : (worldPreset == SINGLE_BIOME_FLOATING_ISLANDS ? NoiseGeneratorSettings.Preset.END.settings() : NoiseGeneratorSettings.Preset.OVERWORLD.settings());
        return new WorldGenSettings(worldGenSettings.seed(), worldGenSettings.generateFeatures(), worldGenSettings.generateBonusChest(), WorldGenSettings.withOverworld(worldGenSettings.dimensions(), new NoiseBasedChunkGenerator(biomeSource, worldGenSettings.seed(), noiseGeneratorSettings)));
    }

    private static Biome parseBuffetSettings(WorldGenSettings worldGenSettings) {
        return worldGenSettings.overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(Biomes.OCEAN);
    }

    public static Optional<WorldPreset> of(WorldGenSettings worldGenSettings) {
        ChunkGenerator chunkGenerator = worldGenSettings.overworld();
        if (chunkGenerator instanceof FlatLevelSource) {
            return Optional.of(FLAT);
        }
        if (chunkGenerator instanceof DebugLevelSource) {
            return Optional.of(DEBUG);
        }
        return Optional.empty();
    }

    public Component description() {
        return this.description;
    }

    public WorldGenSettings create(long l, boolean bl, boolean bl2) {
        return new WorldGenSettings(l, bl, bl2, WorldGenSettings.withOverworld(DimensionType.defaultDimensions(l), this.generator(l)));
    }

    protected abstract ChunkGenerator generator(long var1);

    @Environment(value=EnvType.CLIENT)
    public static interface PresetEditor {
        public Screen createEditScreen(CreateWorldScreen var1, WorldGenSettings var2);
    }
}

