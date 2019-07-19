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
import net.minecraft.world.level.levelgen.placement.nether.ChanceRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.CountRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.HellFireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.LightGemChanceDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;
import net.minecraft.world.level.levelgen.placement.nether.RandomCountRangeDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
	public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHTMAP = register(
		"count_heightmap", new CountHeightmapDecorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequency> COUNT_TOP_SOLID = register(
		"count_top_solid", new CountTopSolidDecorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHTMAP_32 = register(
		"count_heightmap_32", new CountHeightmap32Decorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHTMAP_DOUBLE = register(
		"count_heightmap_double", new CountHeighmapDoubleDecorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequency> COUNT_HEIGHT_64 = register(
		"count_height_64", new CountHeight64Decorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<DecoratorNoiseDependant> NOISE_HEIGHTMAP_32 = register(
		"noise_heightmap_32", new NoiseHeightmap32Decorator(DecoratorNoiseDependant::deserialize)
	);
	public static final FeatureDecorator<DecoratorNoiseDependant> NOISE_HEIGHTMAP_DOUBLE = register(
		"noise_heightmap_double", new NoiseHeightmapDoubleDecorator(DecoratorNoiseDependant::deserialize)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration::deserialize));
	public static final FeatureDecorator<DecoratorChance> CHANCE_HEIGHTMAP = register(
		"chance_heightmap", new ChanceHeightmapDecorator(DecoratorChance::deserialize)
	);
	public static final FeatureDecorator<DecoratorChance> CHANCE_HEIGHTMAP_DOUBLE = register(
		"chance_heightmap_double", new ChanceHeightmapDoubleDecorator(DecoratorChance::deserialize)
	);
	public static final FeatureDecorator<DecoratorChance> CHANCE_PASSTHROUGH = register(
		"chance_passthrough", new ChancePassthroughDecorator(DecoratorChance::deserialize)
	);
	public static final FeatureDecorator<DecoratorChance> CHANCE_TOP_SOLID_HEIGHTMAP = register(
		"chance_top_solid_heightmap", new ChanceTopSolidHeightmapDecorator(DecoratorChance::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequencyWithExtraChance> COUNT_EXTRA_HEIGHTMAP = register(
		"count_extra_heightmap", new CountWithExtraChanceHeightmapDecorator(DecoratorFrequencyWithExtraChance::deserialize)
	);
	public static final FeatureDecorator<DecoratorCountRange> COUNT_RANGE = register("count_range", new CountRangeDecorator(DecoratorCountRange::deserialize));
	public static final FeatureDecorator<DecoratorCountRange> COUNT_BIASED_RANGE = register(
		"count_biased_range", new CountBiasedRangeDecorator(DecoratorCountRange::deserialize)
	);
	public static final FeatureDecorator<DecoratorCountRange> COUNT_VERY_BIASED_RANGE = register(
		"count_very_biased_range", new CountVeryBiasedRangeDecorator(DecoratorCountRange::deserialize)
	);
	public static final FeatureDecorator<DecoratorCountRange> RANDOM_COUNT_RANGE = register(
		"random_count_range", new RandomCountRangeDecorator(DecoratorCountRange::deserialize)
	);
	public static final FeatureDecorator<DecoratorChanceRange> CHANCE_RANGE = register("chance_range", new ChanceRangeDecorator(DecoratorChanceRange::deserialize));
	public static final FeatureDecorator<DecoratorFrequencyChance> COUNT_CHANCE_HEIGHTMAP = register(
		"count_chance_heightmap", new CountChanceHeightmapDecorator(DecoratorFrequencyChance::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequencyChance> COUNT_CHANCE_HEIGHTMAP_DOUBLE = register(
		"count_chance_heightmap_double", new CountChanceHeightmapDoubleDecorator(DecoratorFrequencyChance::deserialize)
	);
	public static final FeatureDecorator<DepthAverageConfigation> COUNT_DEPTH_AVERAGE = register(
		"count_depth_average", new CountDepthAverageDecorator(DepthAverageConfigation::deserialize)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = register(
		"top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration::deserialize)
	);
	public static final FeatureDecorator<DecoratorRange> TOP_SOLID_HEIGHTMAP_RANGE = register(
		"top_solid_heightmap_range", new TopSolidHeightMapRangeDecorator(DecoratorRange::deserialize)
	);
	public static final FeatureDecorator<DecoratorNoiseCountFactor> TOP_SOLID_HEIGHTMAP_NOISE_BIASED = register(
		"top_solid_heightmap_noise_biased", new TopSolidHeightMapNoiseBasedDecorator(DecoratorNoiseCountFactor::deserialize)
	);
	public static final FeatureDecorator<DecoratorCarvingMaskConfig> CARVING_MASK = register(
		"carving_mask", new CarvingMaskDecorator(DecoratorCarvingMaskConfig::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequency> FOREST_ROCK = register(
		"forest_rock", new ForestRockPlacementDecorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<DecoratorFrequency> HELL_FIRE = register("hell_fire", new HellFireDecorator(DecoratorFrequency::deserialize));
	public static final FeatureDecorator<DecoratorFrequency> MAGMA = register("magma", new MagmaDecorator(DecoratorFrequency::deserialize));
	public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = register(
		"emerald_ore", new EmeraldPlacementDecorator(NoneDecoratorConfiguration::deserialize)
	);
	public static final FeatureDecorator<LakeChanceDecoratorConfig> LAVA_LAKE = register(
		"lava_lake", new LakeLavaPlacementDecorator(LakeChanceDecoratorConfig::deserialize)
	);
	public static final FeatureDecorator<LakeChanceDecoratorConfig> WATER_LAKE = register(
		"water_lake", new LakeWaterPlacementDecorator(LakeChanceDecoratorConfig::deserialize)
	);
	public static final FeatureDecorator<MonsterRoomPlacementConfiguration> DUNGEONS = register(
		"dungeons", new MonsterRoomPlacementDecorator(MonsterRoomPlacementConfiguration::deserialize)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = register(
		"dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration::deserialize)
	);
	public static final FeatureDecorator<DecoratorChance> ICEBERG = register("iceberg", new IcebergPlacementDecorator(DecoratorChance::deserialize));
	public static final FeatureDecorator<DecoratorFrequency> LIGHT_GEM_CHANCE = register(
		"light_gem_chance", new LightGemChanceDecorator(DecoratorFrequency::deserialize)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = register(
		"end_island", new EndIslandPlacementDecorator(NoneDecoratorConfiguration::deserialize)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> CHORUS_PLANT = register(
		"chorus_plant", new ChorusPlantPlacementDecorator(NoneDecoratorConfiguration::deserialize)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = register(
		"end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration::deserialize)
	);
	private final Function<Dynamic<?>, ? extends DC> configurationFactory;

	private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String string, G featureDecorator) {
		return Registry.register(Registry.DECORATOR, string, featureDecorator);
	}

	public FeatureDecorator(Function<Dynamic<?>, ? extends DC> function) {
		this.configurationFactory = function;
	}

	public DC createSettings(Dynamic<?> dynamic) {
		return (DC)this.configurationFactory.apply(dynamic);
	}

	protected <FC extends FeatureConfiguration> boolean placeFeature(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		DC decoratorConfiguration,
		ConfiguredFeature<FC> configuredFeature
	) {
		AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		this.getPositions(levelAccessor, chunkGenerator, random, decoratorConfiguration, blockPos).forEach(blockPosx -> {
			boolean bl = configuredFeature.place(levelAccessor, chunkGenerator, random, blockPosx);
			atomicBoolean.set(atomicBoolean.get() || bl);
		});
		return atomicBoolean.get();
	}

	public abstract Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DC decoratorConfiguration, BlockPos blockPos
	);

	public String toString() {
		return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
	}
}
