/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.jetbrains.annotations.Nullable;

public class FlatLevelSource
extends ChunkGenerator<FlatLevelGeneratorSettings> {
    private final Biome biomeWrapper;
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final CatSpawner catSpawner = new CatSpawner();

    public FlatLevelSource(LevelAccessor levelAccessor, BiomeSource biomeSource, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        super(levelAccessor, biomeSource, flatLevelGeneratorSettings);
        this.biomeWrapper = this.getBiomeFromSettings();
    }

    /*
     * WARNING - void declaration
     */
    private Biome getBiomeFromSettings() {
        void var6_11;
        boolean bl;
        Biome biome = ((FlatLevelGeneratorSettings)this.settings).getBiome();
        FlatLevelBiomeWrapper flatLevelBiomeWrapper = new FlatLevelBiomeWrapper(biome.getSurfaceBuilder(), biome.getPrecipitation(), biome.getBiomeCategory(), biome.getDepth(), biome.getScale(), biome.getTemperature(), biome.getDownfall(), biome.getWaterColor(), biome.getWaterFogColor(), biome.getParent());
        Map<String, Map<String, String>> map = ((FlatLevelGeneratorSettings)this.settings).getStructuresOptions();
        for (String string : map.keySet()) {
            ConfiguredFeature<?, ?>[] configuredFeatureArray = FlatLevelGeneratorSettings.STRUCTURE_FEATURES.get(string);
            if (configuredFeatureArray == null) continue;
            ConfiguredFeature<?, ?>[] configuredFeatureArray2 = configuredFeatureArray;
            int n = configuredFeatureArray2.length;
            for (int i = 0; i < n; ++i) {
                ConfiguredFeature<?, ?> configuredFeature = configuredFeatureArray2[i];
                flatLevelBiomeWrapper.addFeature(FlatLevelGeneratorSettings.STRUCTURE_FEATURES_STEP.get(configuredFeature), configuredFeature);
                ConfiguredFeature<?, ?> configuredFeature2 = ((DecoratedFeatureConfiguration)configuredFeature.config).feature;
                if (!(configuredFeature2.feature instanceof StructureFeature)) continue;
                StructureFeature structureFeature = (StructureFeature)configuredFeature2.feature;
                Object featureConfiguration = biome.getStructureConfiguration(structureFeature);
                Object featureConfiguration2 = featureConfiguration != null ? featureConfiguration : FlatLevelGeneratorSettings.STRUCTURE_FEATURES_DEFAULT.get(configuredFeature);
                flatLevelBiomeWrapper.addStructureStart(structureFeature.configured(featureConfiguration2));
            }
        }
        boolean bl2 = bl = (!((FlatLevelGeneratorSettings)this.settings).isVoidGen() || biome == Biomes.THE_VOID) && map.containsKey("decoration");
        if (bl) {
            ArrayList<GenerationStep.Decoration> list = Lists.newArrayList();
            list.add(GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
            list.add(GenerationStep.Decoration.SURFACE_STRUCTURES);
            for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
                if (list.contains((Object)decoration)) continue;
                for (ConfiguredFeature<?, ?> configuredFeature2 : biome.getFeaturesForStep(decoration)) {
                    flatLevelBiomeWrapper.addFeature(decoration, configuredFeature2);
                }
            }
        }
        BlockState[] blockStates = ((FlatLevelGeneratorSettings)this.settings).getLayers();
        boolean bl3 = false;
        while (var6_11 < blockStates.length) {
            BlockState blockState = blockStates[var6_11];
            if (blockState != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
                ((FlatLevelGeneratorSettings)this.settings).deleteLayer((int)var6_11);
                flatLevelBiomeWrapper.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration((int)var6_11, blockState)).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE)));
            }
            ++var6_11;
        }
        return flatLevelBiomeWrapper;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
    }

    @Override
    public int getSpawnHeight() {
        ChunkAccess chunkAccess = this.level.getChunk(0, 0);
        return chunkAccess.getHeight(Heightmap.Types.MOTION_BLOCKING, 8, 8);
    }

    @Override
    protected Biome getCarvingOrDecorationBiome(BiomeManager biomeManager, BlockPos blockPos) {
        return this.biomeWrapper;
    }

    @Override
    public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
        BlockState[] blockStates = ((FlatLevelGeneratorSettings)this.settings).getLayers();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int i = 0; i < blockStates.length; ++i) {
            BlockState blockState = blockStates[i];
            if (blockState == null) continue;
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    chunkAccess.setBlockState(mutableBlockPos.set(j, i, k), blockState, false);
                    heightmap.update(j, i, k, blockState);
                    heightmap2.update(j, i, k, blockState);
                }
            }
        }
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types) {
        BlockState[] blockStates = ((FlatLevelGeneratorSettings)this.settings).getLayers();
        for (int k = blockStates.length - 1; k >= 0; --k) {
            BlockState blockState = blockStates[k];
            if (blockState == null || !types.isOpaque().test(blockState)) continue;
            return k + 1;
        }
        return 0;
    }

    @Override
    public void tickCustomSpawners(ServerLevel serverLevel, boolean bl, boolean bl2) {
        this.phantomSpawner.tick(serverLevel, bl, bl2);
        this.catSpawner.tick(serverLevel, bl, bl2);
    }

    @Override
    public boolean isBiomeValidStartForStructure(Biome biome, StructureFeature<? extends FeatureConfiguration> structureFeature) {
        return this.biomeWrapper.isValidStart(structureFeature);
    }

    @Override
    @Nullable
    public <C extends FeatureConfiguration> C getStructureConfiguration(Biome biome, StructureFeature<C> structureFeature) {
        return this.biomeWrapper.getStructureConfiguration(structureFeature);
    }

    @Override
    @Nullable
    public BlockPos findNearestMapFeature(Level level, String string, BlockPos blockPos, int i, boolean bl) {
        if (!((FlatLevelGeneratorSettings)this.settings).getStructuresOptions().keySet().contains(string.toLowerCase(Locale.ROOT))) {
            return null;
        }
        return super.findNearestMapFeature(level, string, blockPos, i, bl);
    }

    class FlatLevelBiomeWrapper
    extends Biome {
        protected FlatLevelBiomeWrapper(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder, Biome.Precipitation precipitation, Biome.BiomeCategory biomeCategory, float f, float g, float h, float i, int j, @Nullable int k, String string) {
            super(new Biome.BiomeBuilder().surfaceBuilder(configuredSurfaceBuilder).precipitation(precipitation).biomeCategory(biomeCategory).depth(f).scale(g).temperature(h).downfall(i).waterColor(j).waterFogColor(k).parent(string));
        }
    }
}

