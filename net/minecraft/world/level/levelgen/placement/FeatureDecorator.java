/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.DecoratorChanceRange;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.feature.DecoratorNoiseDependant;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecorator;
import net.minecraft.world.level.levelgen.placement.ChanceHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.ChanceHeightmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.ChancePassthroughDecorator;
import net.minecraft.world.level.levelgen.placement.ChanceTopSolidHeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.ChorusPlantPlacementDecorator;
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
import net.minecraft.world.level.levelgen.placement.DecoratorCarvingMaskConfig;
import net.minecraft.world.level.levelgen.placement.DecoratorChance;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequencyChance;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequencyWithExtraChance;
import net.minecraft.world.level.levelgen.placement.DecoratorNoiseCountFactor;
import net.minecraft.world.level.levelgen.placement.DecoratorRange;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfigation;
import net.minecraft.world.level.levelgen.placement.EmeraldPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.EndGatewayPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.EndIslandPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.ForestRockPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.IcebergPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.LakeChanceDecoratorConfig;
import net.minecraft.world.level.levelgen.placement.LakeLavaPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.LakeWaterPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.MonsterRoomPlacementConfiguration;
import net.minecraft.world.level.levelgen.placement.MonsterRoomPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.NoiseHeightmap32Decorator;
import net.minecraft.world.level.levelgen.placement.NoiseHeightmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.NopePlacementDecorator;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapDecorator;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapNoiseBasedDecorator;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.ChanceRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.CountRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.HellFireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.LightGemChanceDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;
import net.minecraft.world.level.levelgen.placement.nether.RandomCountRangeDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
    public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHTMAP = FeatureDecorator.register("count_heightmap", new CountHeightmapDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> COUNT_TOP_SOLID = FeatureDecorator.register("count_top_solid", new CountTopSolidDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHTMAP_32 = FeatureDecorator.register("count_heightmap_32", new CountHeightmap32Decorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHTMAP_DOUBLE = FeatureDecorator.register("count_heightmap_double", new CountHeighmapDoubleDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHT_64 = FeatureDecorator.register("count_height_64", new CountHeight64Decorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorNoiseDependant> NOISE_HEIGHTMAP_32 = FeatureDecorator.register("noise_heightmap_32", new NoiseHeightmap32Decorator((Function<Dynamic<?>, ? extends DecoratorNoiseDependant>)((Function<Dynamic<?>, DecoratorNoiseDependant>)DecoratorNoiseDependant::deserialize)));
    public static final FeatureDecorator<DecoratorNoiseDependant> NOISE_HEIGHTMAP_DOUBLE = FeatureDecorator.register("noise_heightmap_double", new NoiseHeightmapDoubleDecorator((Function<Dynamic<?>, ? extends DecoratorNoiseDependant>)((Function<Dynamic<?>, DecoratorNoiseDependant>)DecoratorNoiseDependant::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = FeatureDecorator.register("nope", new NopePlacementDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    public static final FeatureDecorator<DecoratorChance> CHANCE_HEIGHTMAP = FeatureDecorator.register("chance_heightmap", new ChanceHeightmapDecorator((Function<Dynamic<?>, ? extends DecoratorChance>)((Function<Dynamic<?>, DecoratorChance>)DecoratorChance::deserialize)));
    public static final FeatureDecorator<DecoratorChance> CHANCE_HEIGHTMAP_DOUBLE = FeatureDecorator.register("chance_heightmap_double", new ChanceHeightmapDoubleDecorator((Function<Dynamic<?>, ? extends DecoratorChance>)((Function<Dynamic<?>, DecoratorChance>)DecoratorChance::deserialize)));
    public static final FeatureDecorator<DecoratorChance> CHANCE_PASSTHROUGH = FeatureDecorator.register("chance_passthrough", new ChancePassthroughDecorator((Function<Dynamic<?>, ? extends DecoratorChance>)((Function<Dynamic<?>, DecoratorChance>)DecoratorChance::deserialize)));
    public static final FeatureDecorator<DecoratorChance> CHANCE_TOP_SOLID_HEIGHTMAP = FeatureDecorator.register("chance_top_solid_heightmap", new ChanceTopSolidHeightmapDecorator((Function<Dynamic<?>, ? extends DecoratorChance>)((Function<Dynamic<?>, DecoratorChance>)DecoratorChance::deserialize)));
    public static final FeatureDecorator<DecoratorFrequencyWithExtraChance> COUNT_EXTRA_HEIGHTMAP = FeatureDecorator.register("count_extra_heightmap", new CountWithExtraChanceHeightmapDecorator((Function<Dynamic<?>, ? extends DecoratorFrequencyWithExtraChance>)((Function<Dynamic<?>, DecoratorFrequencyWithExtraChance>)DecoratorFrequencyWithExtraChance::deserialize)));
    public static final FeatureDecorator<DecoratorCountRange> COUNT_RANGE = FeatureDecorator.register("count_range", new CountRangeDecorator((Function<Dynamic<?>, ? extends DecoratorCountRange>)((Function<Dynamic<?>, DecoratorCountRange>)DecoratorCountRange::deserialize)));
    public static final FeatureDecorator<DecoratorCountRange> COUNT_BIASED_RANGE = FeatureDecorator.register("count_biased_range", new CountBiasedRangeDecorator((Function<Dynamic<?>, ? extends DecoratorCountRange>)((Function<Dynamic<?>, DecoratorCountRange>)DecoratorCountRange::deserialize)));
    public static final FeatureDecorator<DecoratorCountRange> COUNT_VERY_BIASED_RANGE = FeatureDecorator.register("count_very_biased_range", new CountVeryBiasedRangeDecorator((Function<Dynamic<?>, ? extends DecoratorCountRange>)((Function<Dynamic<?>, DecoratorCountRange>)DecoratorCountRange::deserialize)));
    public static final FeatureDecorator<DecoratorCountRange> RANDOM_COUNT_RANGE = FeatureDecorator.register("random_count_range", new RandomCountRangeDecorator((Function<Dynamic<?>, ? extends DecoratorCountRange>)((Function<Dynamic<?>, DecoratorCountRange>)DecoratorCountRange::deserialize)));
    public static final FeatureDecorator<DecoratorChanceRange> CHANCE_RANGE = FeatureDecorator.register("chance_range", new ChanceRangeDecorator((Function<Dynamic<?>, ? extends DecoratorChanceRange>)((Function<Dynamic<?>, DecoratorChanceRange>)DecoratorChanceRange::deserialize)));
    public static final FeatureDecorator<DecoratorFrequencyChance> COUNT_CHANCE_HEIGHTMAP = FeatureDecorator.register("count_chance_heightmap", new CountChanceHeightmapDecorator((Function<Dynamic<?>, ? extends DecoratorFrequencyChance>)((Function<Dynamic<?>, DecoratorFrequencyChance>)DecoratorFrequencyChance::deserialize)));
    public static final FeatureDecorator<DecoratorFrequencyChance> COUNT_CHANCE_HEIGHTMAP_DOUBLE = FeatureDecorator.register("count_chance_heightmap_double", new CountChanceHeightmapDoubleDecorator((Function<Dynamic<?>, ? extends DecoratorFrequencyChance>)((Function<Dynamic<?>, DecoratorFrequencyChance>)DecoratorFrequencyChance::deserialize)));
    public static final FeatureDecorator<DepthAverageConfigation> COUNT_DEPTH_AVERAGE = FeatureDecorator.register("count_depth_average", new CountDepthAverageDecorator((Function<Dynamic<?>, ? extends DepthAverageConfigation>)((Function<Dynamic<?>, DepthAverageConfigation>)DepthAverageConfigation::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = FeatureDecorator.register("top_solid_heightmap", new TopSolidHeightMapDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    public static final FeatureDecorator<DecoratorRange> TOP_SOLID_HEIGHTMAP_RANGE = FeatureDecorator.register("top_solid_heightmap_range", new TopSolidHeightMapRangeDecorator((Function<Dynamic<?>, ? extends DecoratorRange>)((Function<Dynamic<?>, DecoratorRange>)DecoratorRange::deserialize)));
    public static final FeatureDecorator<DecoratorNoiseCountFactor> TOP_SOLID_HEIGHTMAP_NOISE_BIASED = FeatureDecorator.register("top_solid_heightmap_noise_biased", new TopSolidHeightMapNoiseBasedDecorator((Function<Dynamic<?>, ? extends DecoratorNoiseCountFactor>)((Function<Dynamic<?>, DecoratorNoiseCountFactor>)DecoratorNoiseCountFactor::deserialize)));
    public static final FeatureDecorator<DecoratorCarvingMaskConfig> CARVING_MASK = FeatureDecorator.register("carving_mask", new CarvingMaskDecorator((Function<Dynamic<?>, ? extends DecoratorCarvingMaskConfig>)((Function<Dynamic<?>, DecoratorCarvingMaskConfig>)DecoratorCarvingMaskConfig::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> FOREST_ROCK = FeatureDecorator.register("forest_rock", new ForestRockPlacementDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> HELL_FIRE = FeatureDecorator.register("hell_fire", new HellFireDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> MAGMA = FeatureDecorator.register("magma", new MagmaDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = FeatureDecorator.register("emerald_ore", new EmeraldPlacementDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    public static final FeatureDecorator<LakeChanceDecoratorConfig> LAVA_LAKE = FeatureDecorator.register("lava_lake", new LakeLavaPlacementDecorator((Function<Dynamic<?>, ? extends LakeChanceDecoratorConfig>)((Function<Dynamic<?>, LakeChanceDecoratorConfig>)LakeChanceDecoratorConfig::deserialize)));
    public static final FeatureDecorator<LakeChanceDecoratorConfig> WATER_LAKE = FeatureDecorator.register("water_lake", new LakeWaterPlacementDecorator((Function<Dynamic<?>, ? extends LakeChanceDecoratorConfig>)((Function<Dynamic<?>, LakeChanceDecoratorConfig>)LakeChanceDecoratorConfig::deserialize)));
    public static final FeatureDecorator<MonsterRoomPlacementConfiguration> DUNGEONS = FeatureDecorator.register("dungeons", new MonsterRoomPlacementDecorator((Function<Dynamic<?>, ? extends MonsterRoomPlacementConfiguration>)((Function<Dynamic<?>, MonsterRoomPlacementConfiguration>)MonsterRoomPlacementConfiguration::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = FeatureDecorator.register("dark_oak_tree", new DarkOakTreePlacementDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    public static final FeatureDecorator<DecoratorChance> ICEBERG = FeatureDecorator.register("iceberg", new IcebergPlacementDecorator((Function<Dynamic<?>, ? extends DecoratorChance>)((Function<Dynamic<?>, DecoratorChance>)DecoratorChance::deserialize)));
    public static final FeatureDecorator<DecoratorFrequency> LIGHT_GEM_CHANCE = FeatureDecorator.register("light_gem_chance", new LightGemChanceDecorator((Function<Dynamic<?>, ? extends DecoratorFrequency>)((Function<Dynamic<?>, DecoratorFrequency>)DecoratorFrequency::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = FeatureDecorator.register("end_island", new EndIslandPlacementDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> CHORUS_PLANT = FeatureDecorator.register("chorus_plant", new ChorusPlantPlacementDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = FeatureDecorator.register("end_gateway", new EndGatewayPlacementDecorator((Function<Dynamic<?>, ? extends NoneDecoratorConfiguration>)((Function<Dynamic<?>, NoneDecoratorConfiguration>)NoneDecoratorConfiguration::deserialize)));
    private final Function<Dynamic<?>, ? extends DC> configurationFactory;

    private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String string, G featureDecorator) {
        return (G)Registry.register(Registry.DECORATOR, string, featureDecorator);
    }

    public FeatureDecorator(Function<Dynamic<?>, ? extends DC> function) {
        this.configurationFactory = function;
    }

    public DC createSettings(Dynamic<?> dynamic) {
        return (DC)((DecoratorConfiguration)this.configurationFactory.apply(dynamic));
    }

    protected <FC extends FeatureConfiguration> boolean placeFeature(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos2, DC decoratorConfiguration, ConfiguredFeature<FC> configuredFeature) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        this.getPositions(levelAccessor, chunkGenerator, random, decoratorConfiguration, blockPos2).forEach(blockPos -> {
            boolean bl = configuredFeature.place(levelAccessor, (ChunkGenerator<ChunkGeneratorSettings>)chunkGenerator, random, (BlockPos)blockPos);
            atomicBoolean.set(atomicBoolean.get() || bl);
        });
        return atomicBoolean.get();
    }

    public abstract Stream<BlockPos> getPositions(LevelAccessor var1, ChunkGenerator<? extends ChunkGeneratorSettings> var2, Random var3, DC var4, BlockPos var5);

    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
    }
}

