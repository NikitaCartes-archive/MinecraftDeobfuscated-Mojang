package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.nether.CountMultiLayerDecorator;
import net.minecraft.world.level.levelgen.placement.nether.FireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.GlowstoneDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
	public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE = register("chance", new ChanceDecorator(ChanceDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<CountConfiguration> COUNT = register("count", new CountDecorator(CountConfiguration.CODEC));
	public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> COUNT_NOISE = register(
		"count_noise", new CountNoiseDecorator(NoiseDependantDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> COUNT_NOISE_BIASED = register(
		"count_noise_biased", new NoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA = register(
		"count_extra", new CountWithExtraChanceDecorator(FrequencyWithExtraChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> SQUARE = register("square", new SquareDecorator(NoneDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP = register("heightmap", new HeightmapDecorator<>(NoneDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP_SPREAD_DOUBLE = register(
		"heightmap_spread_double", new HeightmapDoubleDecorator<>(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = register(
		"top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP_WORLD_SURFACE = register(
		"heightmap_world_surface", new HeightMapWorldSurfaceDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE = register("range", new RangeDecorator(RangeDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE_BIASED = register(
		"range_biased", new BiasedRangeDecorator(RangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE_VERY_BIASED = register(
		"range_very_biased", new VeryBiasedRangeDecorator(RangeDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<DepthAverageConfigation> DEPTH_AVERAGE = register(
		"depth_average", new DepthAverageDecorator(DepthAverageConfigation.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> SPREAD_32_ABOVE = register(
		"spread_32_above", new Spread32Decorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK = register(
		"carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountConfiguration> FIRE = register("fire", new FireDecorator(CountConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> MAGMA = register("magma", new MagmaDecorator(NoneDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = register(
		"emerald_ore", new EmeraldPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE = register(
		"lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<ChanceDecoratorConfiguration> WATER_LAKE = register(
		"water_lake", new LakeWaterPlacementDecorator(ChanceDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountConfiguration> GLOWSTONE = register("glowstone", new GlowstoneDecorator(CountConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = register(
		"end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = register(
		"dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<NoneDecoratorConfiguration> ICEBERG = register("iceberg", new IcebergPlacementDecorator(NoneDecoratorConfiguration.CODEC));
	public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = register(
		"end_island", new EndIslandPlacementDecorator(NoneDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<DecoratedDecoratorConfiguration> DECORATED = register(
		"decorated", new DecoratedDecorator(DecoratedDecoratorConfiguration.CODEC)
	);
	public static final FeatureDecorator<CountConfiguration> COUNT_MULTILAYER = register(
		"count_multilayer", new CountMultiLayerDecorator(CountConfiguration.CODEC)
	);
	private final Codec<ConfiguredDecorator<DC>> configuredCodec;

	private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String string, G featureDecorator) {
		return Registry.register(Registry.DECORATOR, string, featureDecorator);
	}

	public FeatureDecorator(Codec<DC> codec) {
		this.configuredCodec = codec.fieldOf("config")
			.<ConfiguredDecorator<DC>>xmap(decoratorConfiguration -> new ConfiguredDecorator<>(this, (DC)decoratorConfiguration), ConfiguredDecorator::config)
			.codec();
	}

	public ConfiguredDecorator<DC> configured(DC decoratorConfiguration) {
		return new ConfiguredDecorator<>(this, decoratorConfiguration);
	}

	public Codec<ConfiguredDecorator<DC>> configuredCodec() {
		return this.configuredCodec;
	}

	public abstract Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos);

	public String toString() {
		return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
	}
}
