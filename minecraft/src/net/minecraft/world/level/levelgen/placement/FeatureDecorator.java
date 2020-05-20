package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
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
import net.minecraft.world.level.levelgen.placement.nether.ChanceRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.CountRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.FireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.LightGemChanceDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;
import net.minecraft.world.level.levelgen.placement.nether.RandomCountRangeDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
	public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP = register(
		"count_heightmap", new CountHeightmapDecorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_TOP_SOLID = register(
		"count_top_solid", new CountTopSolidDecorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP_32 = register(
		"count_heightmap_32", new CountHeightmap32Decorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHTMAP_DOUBLE = register(
		"count_heightmap_double", new CountHeighmapDoubleDecorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> COUNT_HEIGHT_64 = register(
		"count_height_64", new CountHeight64Decorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> NOISE_HEIGHTMAP_32 = register(
		"noise_heightmap_32", new NoiseHeightmap32Decorator(NoiseDependantDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> NOISE_HEIGHTMAP_DOUBLE = register(
		"noise_heightmap_double", new NoiseHeightmapDoubleDecorator(NoiseDependantDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_HEIGHTMAP = register(
		"chance_heightmap", new ChanceHeightmapDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_HEIGHTMAP_DOUBLE = register(
		"chance_heightmap_double", new ChanceHeightmapDoubleDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_PASSTHROUGH = register(
		"chance_passthrough", new ChancePassthroughDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE_TOP_SOLID_HEIGHTMAP = register(
		"chance_top_solid_heightmap", new ChanceTopSolidHeightmapDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA_HEIGHTMAP = register(
		"count_extra_heightmap", new CountWithExtraChanceHeightmapDecorator(FrequencyWithExtraChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_RANGE = register(
		"count_range", new CountRangeDecorator(CountRangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_BIASED_RANGE = register(
		"count_biased_range", new CountBiasedRangeDecorator(CountRangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountRangeDecoratorConfiguration> COUNT_VERY_BIASED_RANGE = register(
		"count_very_biased_range", new CountVeryBiasedRangeDecorator(CountRangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountRangeDecoratorConfiguration> RANDOM_COUNT_RANGE = register(
		"random_count_range", new RandomCountRangeDecorator(CountRangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceRangeDecoratorConfiguration> CHANCE_RANGE = register(
		"chance_range", new ChanceRangeDecorator(ChanceRangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyChanceDecoratorConfiguration> COUNT_CHANCE_HEIGHTMAP = register(
		"count_chance_heightmap", new CountChanceHeightmapDecorator(FrequencyChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyChanceDecoratorConfiguration> COUNT_CHANCE_HEIGHTMAP_DOUBLE = register(
		"count_chance_heightmap_double", new CountChanceHeightmapDoubleDecorator(FrequencyChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<DepthAverageConfigation> COUNT_DEPTH_AVERAGE = register(
		"count_depth_average", new CountDepthAverageDecorator(DepthAverageConfigation.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = register(
		"top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<RangeDecoratorConfiguration> TOP_SOLID_HEIGHTMAP_RANGE = register(
		"top_solid_heightmap_range", new TopSolidHeightMapRangeDecorator(RangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> TOP_SOLID_HEIGHTMAP_NOISE_BIASED = register(
		"top_solid_heightmap_noise_biased", new TopSolidHeightMapNoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK = register(
		"carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> FOREST_ROCK = register(
		"forest_rock", new ForestRockPlacementDecorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> FIRE = register("fire", new FireDecorator(FrequencyDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> MAGMA = register("magma", new MagmaDecorator(FrequencyDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = register(
		"emerald_ore", new EmeraldPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE = register(
		"lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> WATER_LAKE = register(
		"water_lake", new LakeWaterPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> DUNGEONS = register(
		"dungeons", new MonsterRoomPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = register(
		"dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> ICEBERG = register(
		"iceberg", new IcebergPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyDecoratorConfiguration> LIGHT_GEM_CHANCE = register(
		"light_gem_chance", new LightGemChanceDecorator(FrequencyDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = register(
		"end_island", new EndIslandPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> CHORUS_PLANT = register(
		"chorus_plant", new ChorusPlantPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = register(
		"end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	private final Codec<ConfiguredDecorator<DC>> configuredCodec;

	private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String string, G featureDecorator) {
		return Registry.register(Registry.DECORATOR, string, featureDecorator);
	}

	public FeatureDecorator(Codec<DC> codec) {
		this.configuredCodec = codec.fieldOf("config")
			.<ConfiguredDecorator<DC>>xmap(
				decoratorConfiguration -> new ConfiguredDecorator<>(this, (DC)decoratorConfiguration), configuredDecorator -> configuredDecorator.config
			)
			.codec();
	}

	public ConfiguredDecorator<DC> configured(DC decoratorConfiguration) {
		return new ConfiguredDecorator<>(this, decoratorConfiguration);
	}

	public Codec<ConfiguredDecorator<DC>> configuredCodec() {
		return this.configuredCodec;
	}

	protected <FC extends FeatureConfiguration, F extends Feature<FC>> boolean placeFeature(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		DC decoratorConfiguration,
		ConfiguredFeature<FC, F> configuredFeature
	) {
		AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		this.getPositions(worldGenLevel, chunkGenerator, random, decoratorConfiguration, blockPos).forEach(blockPosx -> {
			boolean bl = configuredFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPosx);
			atomicBoolean.set(atomicBoolean.get() || bl);
		});
		return atomicBoolean.get();
	}

	public abstract Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, DC decoratorConfiguration, BlockPos blockPos
	);

	public String toString() {
		return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
	}
}
