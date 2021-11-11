package net.minecraft.data.worldgen.features;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.material.Fluids;

public class NetherFeatures {
	public static final ConfiguredFeature<DeltaFeatureConfiguration, ?> DELTA = FeatureUtils.register(
		"delta",
		Feature.DELTA_FEATURE
			.configured(new DeltaFeatureConfiguration(Blocks.LAVA.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), UniformInt.of(3, 7), UniformInt.of(0, 2)))
	);
	public static final ConfiguredFeature<ColumnFeatureConfiguration, ?> SMALL_BASALT_COLUMNS = FeatureUtils.register(
		"small_basalt_columns", Feature.BASALT_COLUMNS.configured(new ColumnFeatureConfiguration(ConstantInt.of(1), UniformInt.of(1, 4)))
	);
	public static final ConfiguredFeature<ColumnFeatureConfiguration, ?> LARGE_BASALT_COLUMNS = FeatureUtils.register(
		"large_basalt_columns_temp", Feature.BASALT_COLUMNS.configured(new ColumnFeatureConfiguration(UniformInt.of(2, 3), UniformInt.of(5, 10)))
	);
	public static final ConfiguredFeature<ReplaceSphereConfiguration, ?> BASALT_BLOBS = FeatureUtils.register(
		"basalt_blobs",
		Feature.REPLACE_BLOBS
			.configured(new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BASALT.defaultBlockState(), UniformInt.of(3, 7)))
	);
	public static final ConfiguredFeature<ReplaceSphereConfiguration, ?> BLACKSTONE_BLOBS = FeatureUtils.register(
		"blackstone_blobs",
		Feature.REPLACE_BLOBS
			.configured(new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), UniformInt.of(3, 7)))
	);
	public static final ConfiguredFeature<NoneFeatureConfiguration, ?> GLOWSTONE_EXTRA = FeatureUtils.register(
		"glowstone_extra", Feature.GLOWSTONE_BLOB.configured(FeatureConfiguration.NONE)
	);
	public static final WeightedStateProvider CRIMSON_VEGETATION_PROVIDER = new WeightedStateProvider(
		SimpleWeightedRandomList.<BlockState>builder()
			.add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 87)
			.add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 11)
			.add(Blocks.WARPED_FUNGUS.defaultBlockState(), 1)
	);
	public static final ConfiguredFeature<?, ?> CRIMSON_FOREST_VEGETATION = FeatureUtils.register(
		"crimson_forest_vegetation", Feature.NETHER_FOREST_VEGETATION.configured(new NetherForestVegetationConfig(CRIMSON_VEGETATION_PROVIDER, 8, 4))
	);
	public static final ConfiguredFeature<?, ?> CRIMSON_FOREST_VEGETATION_BONEMEAL = FeatureUtils.register(
		"crimson_forest_vegetation_bonemeal", Feature.NETHER_FOREST_VEGETATION.configured(new NetherForestVegetationConfig(CRIMSON_VEGETATION_PROVIDER, 3, 1))
	);
	public static final WeightedStateProvider WARPED_VEGETATION_PROVIDER = new WeightedStateProvider(
		SimpleWeightedRandomList.<BlockState>builder()
			.add(Blocks.WARPED_ROOTS.defaultBlockState(), 85)
			.add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 1)
			.add(Blocks.WARPED_FUNGUS.defaultBlockState(), 13)
			.add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 1)
	);
	public static final ConfiguredFeature<?, ?> WARPED_FOREST_VEGETION = FeatureUtils.register(
		"warped_forest_vegetation", Feature.NETHER_FOREST_VEGETATION.configured(new NetherForestVegetationConfig(WARPED_VEGETATION_PROVIDER, 8, 4))
	);
	public static final ConfiguredFeature<?, ?> WARPED_FOREST_VEGETATION_BONEMEAL = FeatureUtils.register(
		"warped_forest_vegetation_bonemeal", Feature.NETHER_FOREST_VEGETATION.configured(new NetherForestVegetationConfig(WARPED_VEGETATION_PROVIDER, 3, 1))
	);
	public static final ConfiguredFeature<?, ?> NETHER_SPROUTS = FeatureUtils.register(
		"nether_sprouts", Feature.NETHER_FOREST_VEGETATION.configured(new NetherForestVegetationConfig(BlockStateProvider.simple(Blocks.NETHER_SPROUTS), 8, 4))
	);
	public static final ConfiguredFeature<?, ?> NETHER_SPROUTS_BONEMEAL = FeatureUtils.register(
		"nether_sprouts_bonemeal",
		Feature.NETHER_FOREST_VEGETATION.configured(new NetherForestVegetationConfig(BlockStateProvider.simple(Blocks.NETHER_SPROUTS), 3, 1))
	);
	public static final ConfiguredFeature<?, ?> TWISTING_VINES = FeatureUtils.register(
		"twisting_vines", Feature.TWISTING_VINES.configured(new TwistingVinesConfig(8, 4, 8))
	);
	public static final ConfiguredFeature<?, ?> TWISTING_VINES_BONEMEAL = FeatureUtils.register(
		"twisting_vines_bonemeal", Feature.TWISTING_VINES.configured(new TwistingVinesConfig(3, 1, 2))
	);
	public static final ConfiguredFeature<NoneFeatureConfiguration, ?> WEEPING_VINES = FeatureUtils.register(
		"weeping_vines", Feature.WEEPING_VINES.configured(FeatureConfiguration.NONE)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_CRIMSON_ROOTS = FeatureUtils.register(
		"patch_crimson_roots",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.CRIMSON_ROOTS))))
			)
	);
	public static final ConfiguredFeature<NoneFeatureConfiguration, ?> BASALT_PILLAR = FeatureUtils.register(
		"basalt_pillar", Feature.BASALT_PILLAR.configured(FeatureConfiguration.NONE)
	);
	public static final ConfiguredFeature<SpringConfiguration, ?> SPRING_LAVA_NETHER = FeatureUtils.register(
		"spring_lava_nether",
		Feature.SPRING
			.configured(
				new SpringConfiguration(
					Fluids.LAVA.defaultFluidState(), true, 4, 1, ImmutableSet.of(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA_BLOCK, Blocks.BLACKSTONE)
				)
			)
	);
	public static final ConfiguredFeature<SpringConfiguration, ?> SPRING_NETHER_CLOSED = FeatureUtils.register(
		"spring_nether_closed", Feature.SPRING.configured(new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 5, 0, ImmutableSet.of(Blocks.NETHERRACK)))
	);
	public static final ConfiguredFeature<SpringConfiguration, ?> SPRING_NETHER_OPEN = FeatureUtils.register(
		"spring_nether_open", Feature.SPRING.configured(new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 4, 1, ImmutableSet.of(Blocks.NETHERRACK)))
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_FIRE = FeatureUtils.register(
		"patch_fire",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(
					Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.FIRE))), List.of(Blocks.NETHERRACK)
				)
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_SOUL_FIRE = FeatureUtils.register(
		"patch_soul_fire",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(
					Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SOUL_FIRE))), List.of(Blocks.SOUL_SOIL)
				)
			)
	);
}
