package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.material.Fluids;

public class NetherFeatures {
	public static final ResourceKey<ConfiguredFeature<?, ?>> DELTA = FeatureUtils.createKey("delta");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_BASALT_COLUMNS = FeatureUtils.createKey("small_basalt_columns");
	public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_BASALT_COLUMNS = FeatureUtils.createKey("large_basalt_columns");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BASALT_BLOBS = FeatureUtils.createKey("basalt_blobs");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BLACKSTONE_BLOBS = FeatureUtils.createKey("blackstone_blobs");
	public static final ResourceKey<ConfiguredFeature<?, ?>> GLOWSTONE_EXTRA = FeatureUtils.createKey("glowstone_extra");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CRIMSON_FOREST_VEGETATION = FeatureUtils.createKey("crimson_forest_vegetation");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CRIMSON_FOREST_VEGETATION_BONEMEAL = FeatureUtils.createKey("crimson_forest_vegetation_bonemeal");
	public static final ResourceKey<ConfiguredFeature<?, ?>> WARPED_FOREST_VEGETION = FeatureUtils.createKey("warped_forest_vegetation");
	public static final ResourceKey<ConfiguredFeature<?, ?>> WARPED_FOREST_VEGETATION_BONEMEAL = FeatureUtils.createKey("warped_forest_vegetation_bonemeal");
	public static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_SPROUTS = FeatureUtils.createKey("nether_sprouts");
	public static final ResourceKey<ConfiguredFeature<?, ?>> NETHER_SPROUTS_BONEMEAL = FeatureUtils.createKey("nether_sprouts_bonemeal");
	public static final ResourceKey<ConfiguredFeature<?, ?>> TWISTING_VINES = FeatureUtils.createKey("twisting_vines");
	public static final ResourceKey<ConfiguredFeature<?, ?>> TWISTING_VINES_BONEMEAL = FeatureUtils.createKey("twisting_vines_bonemeal");
	public static final ResourceKey<ConfiguredFeature<?, ?>> WEEPING_VINES = FeatureUtils.createKey("weeping_vines");
	public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CRIMSON_ROOTS = FeatureUtils.createKey("patch_crimson_roots");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BASALT_PILLAR = FeatureUtils.createKey("basalt_pillar");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_LAVA_NETHER = FeatureUtils.createKey("spring_lava_nether");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_NETHER_CLOSED = FeatureUtils.createKey("spring_nether_closed");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_NETHER_OPEN = FeatureUtils.createKey("spring_nether_open");
	public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_FIRE = FeatureUtils.createKey("patch_fire");
	public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_SOUL_FIRE = FeatureUtils.createKey("patch_soul_fire");

	public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
		FeatureUtils.register(
			bootstrapContext,
			DELTA,
			Feature.DELTA_FEATURE,
			new DeltaFeatureConfiguration(Blocks.LAVA.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), UniformInt.of(3, 7), UniformInt.of(0, 2))
		);
		FeatureUtils.register(bootstrapContext, SMALL_BASALT_COLUMNS, Feature.BASALT_COLUMNS, new ColumnFeatureConfiguration(ConstantInt.of(1), UniformInt.of(1, 4)));
		FeatureUtils.register(
			bootstrapContext, LARGE_BASALT_COLUMNS, Feature.BASALT_COLUMNS, new ColumnFeatureConfiguration(UniformInt.of(2, 3), UniformInt.of(5, 10))
		);
		FeatureUtils.register(
			bootstrapContext,
			BASALT_BLOBS,
			Feature.REPLACE_BLOBS,
			new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BASALT.defaultBlockState(), UniformInt.of(3, 7))
		);
		FeatureUtils.register(
			bootstrapContext,
			BLACKSTONE_BLOBS,
			Feature.REPLACE_BLOBS,
			new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), UniformInt.of(3, 7))
		);
		FeatureUtils.register(bootstrapContext, GLOWSTONE_EXTRA, Feature.GLOWSTONE_BLOB);
		WeightedStateProvider weightedStateProvider = new WeightedStateProvider(
			SimpleWeightedRandomList.<BlockState>builder()
				.add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 87)
				.add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 11)
				.add(Blocks.WARPED_FUNGUS.defaultBlockState(), 1)
		);
		FeatureUtils.register(
			bootstrapContext, CRIMSON_FOREST_VEGETATION, Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(weightedStateProvider, 8, 4)
		);
		FeatureUtils.register(
			bootstrapContext, CRIMSON_FOREST_VEGETATION_BONEMEAL, Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(weightedStateProvider, 3, 1)
		);
		WeightedStateProvider weightedStateProvider2 = new WeightedStateProvider(
			SimpleWeightedRandomList.<BlockState>builder()
				.add(Blocks.WARPED_ROOTS.defaultBlockState(), 85)
				.add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 1)
				.add(Blocks.WARPED_FUNGUS.defaultBlockState(), 13)
				.add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 1)
		);
		FeatureUtils.register(
			bootstrapContext, WARPED_FOREST_VEGETION, Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(weightedStateProvider2, 8, 4)
		);
		FeatureUtils.register(
			bootstrapContext, WARPED_FOREST_VEGETATION_BONEMEAL, Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(weightedStateProvider2, 3, 1)
		);
		FeatureUtils.register(
			bootstrapContext, NETHER_SPROUTS, Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(BlockStateProvider.simple(Blocks.NETHER_SPROUTS), 8, 4)
		);
		FeatureUtils.register(
			bootstrapContext,
			NETHER_SPROUTS_BONEMEAL,
			Feature.NETHER_FOREST_VEGETATION,
			new NetherForestVegetationConfig(BlockStateProvider.simple(Blocks.NETHER_SPROUTS), 3, 1)
		);
		FeatureUtils.register(bootstrapContext, TWISTING_VINES, Feature.TWISTING_VINES, new TwistingVinesConfig(8, 4, 8));
		FeatureUtils.register(bootstrapContext, TWISTING_VINES_BONEMEAL, Feature.TWISTING_VINES, new TwistingVinesConfig(3, 1, 2));
		FeatureUtils.register(bootstrapContext, WEEPING_VINES, Feature.WEEPING_VINES);
		FeatureUtils.register(
			bootstrapContext,
			PATCH_CRIMSON_ROOTS,
			Feature.RANDOM_PATCH,
			FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.CRIMSON_ROOTS)))
		);
		FeatureUtils.register(bootstrapContext, BASALT_PILLAR, Feature.BASALT_PILLAR);
		FeatureUtils.register(
			bootstrapContext,
			SPRING_LAVA_NETHER,
			Feature.SPRING,
			new SpringConfiguration(
				Fluids.LAVA.defaultFluidState(),
				true,
				4,
				1,
				HolderSet.direct(Block::builtInRegistryHolder, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA_BLOCK, Blocks.BLACKSTONE)
			)
		);
		FeatureUtils.register(
			bootstrapContext,
			SPRING_NETHER_CLOSED,
			Feature.SPRING,
			new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 5, 0, HolderSet.direct(Block::builtInRegistryHolder, Blocks.NETHERRACK))
		);
		FeatureUtils.register(
			bootstrapContext,
			SPRING_NETHER_OPEN,
			Feature.SPRING,
			new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 4, 1, HolderSet.direct(Block::builtInRegistryHolder, Blocks.NETHERRACK))
		);
		FeatureUtils.register(
			bootstrapContext,
			PATCH_FIRE,
			Feature.RANDOM_PATCH,
			FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.FIRE)), List.of(Blocks.NETHERRACK))
		);
		FeatureUtils.register(
			bootstrapContext,
			PATCH_SOUL_FIRE,
			Feature.RANDOM_PATCH,
			FeatureUtils.simplePatchConfiguration(
				Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SOUL_FIRE)), List.of(Blocks.SOUL_SOIL)
			)
		);
	}
}
