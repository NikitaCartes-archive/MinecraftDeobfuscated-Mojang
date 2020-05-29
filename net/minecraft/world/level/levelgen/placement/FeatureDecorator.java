/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecorator;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.ChanceHeightmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.ChancePassthroughDecorator;
import net.minecraft.world.level.levelgen.placement.ChanceTopSolidHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.ChorusPlantPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.CountBiasedRangeDecorator;
import net.minecraft.world.level.levelgen.placement.CountChanceHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.CountChanceHeightmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.CountDepthAverageDecorator;
import net.minecraft.world.level.levelgen.placement.CountHeighmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.CountHeight64Decorator;
import net.minecraft.world.level.levelgen.placement.CountHeightmap32Decorator;
import net.minecraft.world.level.levelgen.placement.CountHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.CountTopSolidDecorator;
import net.minecraft.world.level.levelgen.placement.CountVeryBiasedRangeDecorator;
import net.minecraft.world.level.levelgen.placement.CountWithExtraChanceHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.DarkOakTreePlacementDecorator;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfigation;
import net.minecraft.world.level.levelgen.placement.EmeraldPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.EndGatewayPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.EndIslandPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.ForestRockPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.IcebergPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.LakeLavaPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.LakeWaterPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.MonsterRoomPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.NoiseCountFactorDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.NoiseHeightmap32Decorator;
import net.minecraft.world.level.levelgen.placement.NoiseHeightmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.NopePlacementDecorator;
import net.minecraft.world.level.levelgen.placement.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapDecorator;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapNoiseBasedDecorator;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.ChanceRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.CountRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.FireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.LightGemChanceDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;
import net.minecraft.world.level.levelgen.placement.nether.RandomCountRangeDecorator;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
    public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = FeatureDecorator.register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP = FeatureDecorator.register("count_heightmap", new CountHeightmapDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_TOP_SOLID = FeatureDecorator.register("count_top_solid", new CountTopSolidDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP_32 = FeatureDecorator.register("count_heightmap_32", new CountHeightmap32Decorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP_DOUBLE = FeatureDecorator.register("count_heightmap_double", new CountHeighmapDoubleDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHT_64 = FeatureDecorator.register("count_height_64", new CountHeight64Decorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> NOISE_HEIGHTMAP_32 = FeatureDecorator.register("noise_heightmap_32", new NoiseHeightmap32Decorator(NoiseDependantDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> NOISE_HEIGHTMAP_DOUBLE = FeatureDecorator.register("noise_heightmap_double", new NoiseHeightmapDoubleDecorator(NoiseDependantDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_HEIGHTMAP = FeatureDecorator.register("chance_heightmap", new ChanceHeightmapDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_HEIGHTMAP_DOUBLE = FeatureDecorator.register("chance_heightmap_double", new ChanceHeightmapDoubleDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_PASSTHROUGH = FeatureDecorator.register("chance_passthrough", new ChancePassthroughDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_TOP_SOLID_HEIGHTMAP = FeatureDecorator.register("chance_top_solid_heightmap", new ChanceTopSolidHeightmapDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA_HEIGHTMAP = FeatureDecorator.register("count_extra_heightmap", new CountWithExtraChanceHeightmapDecorator(FrequencyWithExtraChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_RANGE = FeatureDecorator.register("count_range", new CountRangeDecorator(CountRangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_BIASED_RANGE = FeatureDecorator.register("count_biased_range", new CountBiasedRangeDecorator(CountRangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_VERY_BIASED_RANGE = FeatureDecorator.register("count_very_biased_range", new CountVeryBiasedRangeDecorator(CountRangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountRangeDecoratorConfiguration> RANDOM_COUNT_RANGE = FeatureDecorator.register("random_count_range", new RandomCountRangeDecorator(CountRangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceRangeDecoratorConfiguration> CHANCE_RANGE = FeatureDecorator.register("chance_range", new ChanceRangeDecorator(ChanceRangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyChanceDecoratorConfiguration> COUNT_CHANCE_HEIGHTMAP = FeatureDecorator.register("count_chance_heightmap", new CountChanceHeightmapDecorator(FrequencyChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyChanceDecoratorConfiguration> COUNT_CHANCE_HEIGHTMAP_DOUBLE = FeatureDecorator.register("count_chance_heightmap_double", new CountChanceHeightmapDoubleDecorator(FrequencyChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<DepthAverageConfigation> COUNT_DEPTH_AVERAGE = FeatureDecorator.register("count_depth_average", new CountDepthAverageDecorator(DepthAverageConfigation.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = FeatureDecorator.register("top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<RangeDecoratorConfiguration> TOP_SOLID_HEIGHTMAP_RANGE = FeatureDecorator.register("top_solid_heightmap_range", new TopSolidHeightMapRangeDecorator(RangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> TOP_SOLID_HEIGHTMAP_NOISE_BIASED = FeatureDecorator.register("top_solid_heightmap_noise_biased", new TopSolidHeightMapNoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK = FeatureDecorator.register("carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> FOREST_ROCK = FeatureDecorator.register("forest_rock", new ForestRockPlacementDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> FIRE = FeatureDecorator.register("fire", new FireDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> MAGMA = FeatureDecorator.register("magma", new MagmaDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = FeatureDecorator.register("emerald_ore", new EmeraldPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE = FeatureDecorator.register("lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> WATER_LAKE = FeatureDecorator.register("water_lake", new LakeWaterPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> DUNGEONS = FeatureDecorator.register("dungeons", new MonsterRoomPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = FeatureDecorator.register("dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> ICEBERG = FeatureDecorator.register("iceberg", new IcebergPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyDecoratorConfiguration> LIGHT_GEM_CHANCE = FeatureDecorator.register("light_gem_chance", new LightGemChanceDecorator(FrequencyDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = FeatureDecorator.register("end_island", new EndIslandPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> CHORUS_PLANT = FeatureDecorator.register("chorus_plant", new ChorusPlantPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = FeatureDecorator.register("end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    private final Codec<ConfiguredDecorator<DC>> configuredCodec;

    private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String string, G featureDecorator) {
        return (G)Registry.register(Registry.DECORATOR, string, featureDecorator);
    }

    public FeatureDecorator(Codec<DC> codec) {
        this.configuredCodec = ((MapCodec)codec.fieldOf("config")).xmap(decoratorConfiguration -> new ConfiguredDecorator<DecoratorConfiguration>(this, (DecoratorConfiguration)decoratorConfiguration), configuredDecorator -> configuredDecorator.config).codec();
    }

    public ConfiguredDecorator<DC> configured(DC decoratorConfiguration) {
        return new ConfiguredDecorator<DC>(this, decoratorConfiguration);
    }

    public Codec<ConfiguredDecorator<DC>> configuredCodec() {
        return this.configuredCodec;
    }

    protected <FC extends FeatureConfiguration, F extends Feature<FC>> boolean placeFeature(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos2, DC decoratorConfiguration, ConfiguredFeature<FC, F> configuredFeature) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        this.getPositions(worldGenLevel, chunkGenerator, random, decoratorConfiguration, blockPos2).forEach(blockPos -> {
            if (configuredFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, (BlockPos)blockPos)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.isTrue();
    }

    public abstract Stream<BlockPos> getPositions(LevelAccessor var1, ChunkGenerator var2, Random var3, DC var4, BlockPos var5);

    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
    }
}

