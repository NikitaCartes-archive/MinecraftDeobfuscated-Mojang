package net.minecraft.data.worldgen.features;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.UpwardsBranchingTrunkPlacer;

public class TreeFeatures {
	public static final ResourceKey<ConfiguredFeature<?, ?>> CRIMSON_FUNGUS = FeatureUtils.createKey("crimson_fungus");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CRIMSON_FUNGUS_PLANTED = FeatureUtils.createKey("crimson_fungus_planted");
	public static final ResourceKey<ConfiguredFeature<?, ?>> WARPED_FUNGUS = FeatureUtils.createKey("warped_fungus");
	public static final ResourceKey<ConfiguredFeature<?, ?>> POTATO_TREE_TALL = FeatureUtils.createKey("potato_tree_tall");
	public static final ResourceKey<ConfiguredFeature<?, ?>> POTATO_TREE = FeatureUtils.createKey("potato_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> MOTHER_POTATO_TREE = FeatureUtils.createKey("mother_potato_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> WARPED_FUNGUS_PLANTED = FeatureUtils.createKey("warped_fungus_planted");
	public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_BROWN_MUSHROOM = FeatureUtils.createKey("huge_brown_mushroom");
	public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_RED_MUSHROOM = FeatureUtils.createKey("huge_red_mushroom");
	public static final ResourceKey<ConfiguredFeature<?, ?>> OAK = FeatureUtils.createKey("oak");
	public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_OAK = FeatureUtils.createKey("dark_oak");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BIRCH = FeatureUtils.createKey("birch");
	public static final ResourceKey<ConfiguredFeature<?, ?>> ACACIA = FeatureUtils.createKey("acacia");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPRUCE = FeatureUtils.createKey("spruce");
	public static final ResourceKey<ConfiguredFeature<?, ?>> PINE = FeatureUtils.createKey("pine");
	public static final ResourceKey<ConfiguredFeature<?, ?>> JUNGLE_TREE = FeatureUtils.createKey("jungle_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK = FeatureUtils.createKey("fancy_oak");
	public static final ResourceKey<ConfiguredFeature<?, ?>> JUNGLE_TREE_NO_VINE = FeatureUtils.createKey("jungle_tree_no_vine");
	public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_JUNGLE_TREE = FeatureUtils.createKey("mega_jungle_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_SPRUCE = FeatureUtils.createKey("mega_spruce");
	public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_PINE = FeatureUtils.createKey("mega_pine");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SUPER_BIRCH_BEES_0002 = FeatureUtils.createKey("super_birch_bees_0002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SUPER_BIRCH_BEES = FeatureUtils.createKey("super_birch_bees");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SWAMP_OAK = FeatureUtils.createKey("swamp_oak");
	public static final ResourceKey<ConfiguredFeature<?, ?>> JUNGLE_BUSH = FeatureUtils.createKey("jungle_bush");
	public static final ResourceKey<ConfiguredFeature<?, ?>> AZALEA_TREE = FeatureUtils.createKey("azalea_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> MANGROVE = FeatureUtils.createKey("mangrove");
	public static final ResourceKey<ConfiguredFeature<?, ?>> TALL_MANGROVE = FeatureUtils.createKey("tall_mangrove");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CHERRY = FeatureUtils.createKey("cherry");
	public static final ResourceKey<ConfiguredFeature<?, ?>> OAK_BEES_0002 = FeatureUtils.createKey("oak_bees_0002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> OAK_BEES_002 = FeatureUtils.createKey("oak_bees_002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> OAK_BEES_005 = FeatureUtils.createKey("oak_bees_005");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BIRCH_BEES_0002 = FeatureUtils.createKey("birch_bees_0002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BIRCH_BEES_002 = FeatureUtils.createKey("birch_bees_002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> BIRCH_BEES_005 = FeatureUtils.createKey("birch_bees_005");
	public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK_BEES_0002 = FeatureUtils.createKey("fancy_oak_bees_0002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK_BEES_002 = FeatureUtils.createKey("fancy_oak_bees_002");
	public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK_BEES_005 = FeatureUtils.createKey("fancy_oak_bees_005");
	public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK_BEES = FeatureUtils.createKey("fancy_oak_bees");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CHERRY_BEES_005 = FeatureUtils.createKey("cherry_bees_005");

	private static TreeConfiguration.TreeConfigurationBuilder createStraightBlobTree(Block block, Block block2, int i, int j, int k, int l) {
		return new TreeConfiguration.TreeConfigurationBuilder(
			BlockStateProvider.simple(block),
			new StraightTrunkPlacer(i, j, k),
			BlockStateProvider.simple(block2),
			new BlobFoliagePlacer(ConstantInt.of(l), ConstantInt.of(0), 3),
			new TwoLayersFeatureSize(1, 0, 1)
		);
	}

	private static TreeConfiguration.TreeConfigurationBuilder createOak() {
		return createStraightBlobTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, 4, 2, 0, 2).ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder createBirch() {
		return createStraightBlobTree(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 5, 2, 0, 2).ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder createSuperBirch() {
		return createStraightBlobTree(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 5, 2, 6, 2).ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder createJungleTree() {
		return createStraightBlobTree(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES, 4, 8, 0, 2);
	}

	private static TreeConfiguration.TreeConfigurationBuilder createFancyOak() {
		return new TreeConfiguration.TreeConfigurationBuilder(
				BlockStateProvider.simple(Blocks.OAK_LOG),
				new FancyTrunkPlacer(3, 11, 0),
				BlockStateProvider.simple(Blocks.OAK_LEAVES),
				new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
				new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
			)
			.ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder cherry() {
		return new TreeConfiguration.TreeConfigurationBuilder(
				BlockStateProvider.simple(Blocks.CHERRY_LOG),
				new CherryTrunkPlacer(
					7,
					1,
					0,
					new WeightedListInt(SimpleWeightedRandomList.<IntProvider>builder().add(ConstantInt.of(1), 1).add(ConstantInt.of(2), 1).add(ConstantInt.of(3), 1).build()),
					UniformInt.of(2, 4),
					UniformInt.of(-4, -3),
					UniformInt.of(-1, 0)
				),
				BlockStateProvider.simple(Blocks.CHERRY_LEAVES),
				new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25F, 0.5F, 0.16666667F, 0.33333334F),
				new TwoLayersFeatureSize(1, 0, 2)
			)
			.ignoreVines();
	}

	public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
		HolderGetter<Block> holderGetter = bootstrapContext.lookup(Registries.BLOCK);
		BlockPredicate blockPredicate = BlockPredicate.matchesBlocks(
			Blocks.OAK_SAPLING,
			Blocks.SPRUCE_SAPLING,
			Blocks.BIRCH_SAPLING,
			Blocks.JUNGLE_SAPLING,
			Blocks.ACACIA_SAPLING,
			Blocks.CHERRY_SAPLING,
			Blocks.DARK_OAK_SAPLING,
			Blocks.MANGROVE_PROPAGULE,
			Blocks.DANDELION,
			Blocks.TORCHFLOWER,
			Blocks.POPPY,
			Blocks.BLUE_ORCHID,
			Blocks.ALLIUM,
			Blocks.AZURE_BLUET,
			Blocks.RED_TULIP,
			Blocks.ORANGE_TULIP,
			Blocks.WHITE_TULIP,
			Blocks.PINK_TULIP,
			Blocks.OXEYE_DAISY,
			Blocks.CORNFLOWER,
			Blocks.WITHER_ROSE,
			Blocks.LILY_OF_THE_VALLEY,
			Blocks.POTATO_FLOWER,
			Blocks.BROWN_MUSHROOM,
			Blocks.RED_MUSHROOM,
			Blocks.WHEAT,
			Blocks.SUGAR_CANE,
			Blocks.ATTACHED_PUMPKIN_STEM,
			Blocks.ATTACHED_MELON_STEM,
			Blocks.PUMPKIN_STEM,
			Blocks.MELON_STEM,
			Blocks.LILY_PAD,
			Blocks.NETHER_WART,
			Blocks.COCOA,
			Blocks.CARROTS,
			Blocks.POTATOES,
			Blocks.CHORUS_PLANT,
			Blocks.CHORUS_FLOWER,
			Blocks.TORCHFLOWER_CROP,
			Blocks.PITCHER_CROP,
			Blocks.BEETROOTS,
			Blocks.SWEET_BERRY_BUSH,
			Blocks.WARPED_FUNGUS,
			Blocks.CRIMSON_FUNGUS,
			Blocks.WEEPING_VINES,
			Blocks.WEEPING_VINES_PLANT,
			Blocks.TWISTING_VINES,
			Blocks.TWISTING_VINES_PLANT,
			Blocks.CAVE_VINES,
			Blocks.CAVE_VINES_PLANT,
			Blocks.SPORE_BLOSSOM,
			Blocks.AZALEA,
			Blocks.FLOWERING_AZALEA,
			Blocks.MOSS_CARPET,
			Blocks.PINK_PETALS,
			Blocks.BIG_DRIPLEAF,
			Blocks.BIG_DRIPLEAF_STEM,
			Blocks.SMALL_DRIPLEAF
		);
		FeatureUtils.register(
			bootstrapContext,
			CRIMSON_FUNGUS,
			Feature.HUGE_FUNGUS,
			new HugeFungusConfiguration(
				Blocks.CRIMSON_NYLIUM.defaultBlockState(),
				Blocks.CRIMSON_STEM.defaultBlockState(),
				Blocks.NETHER_WART_BLOCK.defaultBlockState(),
				Blocks.SHROOMLIGHT.defaultBlockState(),
				blockPredicate,
				false
			)
		);
		FeatureUtils.register(
			bootstrapContext,
			CRIMSON_FUNGUS_PLANTED,
			Feature.HUGE_FUNGUS,
			new HugeFungusConfiguration(
				Blocks.CRIMSON_NYLIUM.defaultBlockState(),
				Blocks.CRIMSON_STEM.defaultBlockState(),
				Blocks.NETHER_WART_BLOCK.defaultBlockState(),
				Blocks.SHROOMLIGHT.defaultBlockState(),
				blockPredicate,
				true
			)
		);
		FeatureUtils.register(
			bootstrapContext,
			WARPED_FUNGUS,
			Feature.HUGE_FUNGUS,
			new HugeFungusConfiguration(
				Blocks.WARPED_NYLIUM.defaultBlockState(),
				Blocks.WARPED_STEM.defaultBlockState(),
				Blocks.WARPED_WART_BLOCK.defaultBlockState(),
				Blocks.SHROOMLIGHT.defaultBlockState(),
				blockPredicate,
				false
			)
		);
		FeatureUtils.register(
			bootstrapContext,
			WARPED_FUNGUS_PLANTED,
			Feature.HUGE_FUNGUS,
			new HugeFungusConfiguration(
				Blocks.WARPED_NYLIUM.defaultBlockState(),
				Blocks.WARPED_STEM.defaultBlockState(),
				Blocks.WARPED_WART_BLOCK.defaultBlockState(),
				Blocks.SHROOMLIGHT.defaultBlockState(),
				blockPredicate,
				true
			)
		);
		FeatureUtils.register(
			bootstrapContext,
			HUGE_BROWN_MUSHROOM,
			Feature.HUGE_BROWN_MUSHROOM,
			new HugeMushroomFeatureConfiguration(
				BlockStateProvider.simple(
					Blocks.BROWN_MUSHROOM_BLOCK
						.defaultBlockState()
						.setValue(HugeMushroomBlock.UP, Boolean.valueOf(true))
						.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))
				),
				BlockStateProvider.simple(
					Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, Boolean.valueOf(false)).setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))
				),
				3
			)
		);
		FeatureUtils.register(
			bootstrapContext,
			HUGE_RED_MUSHROOM,
			Feature.HUGE_RED_MUSHROOM,
			new HugeMushroomFeatureConfiguration(
				BlockStateProvider.simple(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))),
				BlockStateProvider.simple(
					Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, Boolean.valueOf(false)).setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))
				),
				2
			)
		);
		BeehiveDecorator beehiveDecorator = new BeehiveDecorator(0.002F, true);
		BeehiveDecorator beehiveDecorator2 = new BeehiveDecorator(0.01F, true);
		BeehiveDecorator beehiveDecorator3 = new BeehiveDecorator(0.02F, true);
		BeehiveDecorator beehiveDecorator4 = new BeehiveDecorator(0.05F, true);
		BeehiveDecorator beehiveDecorator5 = new BeehiveDecorator(0.5F, false);
		BeehiveDecorator beehiveDecorator6 = new BeehiveDecorator(1.0F, true);
		FeatureUtils.register(
			bootstrapContext,
			MOTHER_POTATO_TREE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.POTATO_STEM),
					new UpwardsBranchingTrunkPlacer(32, 1, 20, UniformInt.of(1, 10), 0.4F, UniformInt.of(0, 1), holderGetter.getOrThrow(BlockTags.LOGS), false),
					BlockStateProvider.simple(Blocks.POTATO_LEAVES),
					new AcaciaFoliagePlacer(UniformInt.of(3, 4), ConstantInt.of(0)),
					Optional.empty(),
					new TwoLayersFeatureSize(3, 0, 2)
				)
				.decorators(
					List.of(
						new AttachedToLeavesDecorator(
							0.1F,
							false,
							1,
							0,
							List.of(BlockStateProvider.simple(Blocks.POTATO_PEDICULE.defaultBlockState()), BlockStateProvider.simple(Blocks.POTATO_FRUIT.defaultBlockState())),
							3,
							List.of(Direction.DOWN)
						),
						new AttachedToLeavesDecorator(
							0.5F,
							true,
							1,
							0,
							List.of(BlockStateProvider.simple(Blocks.POTATO_PEDICULE.defaultBlockState()), BlockStateProvider.simple(Blocks.POTATO_FRUIT.defaultBlockState())),
							3,
							List.of(Direction.DOWN)
						),
						beehiveDecorator6,
						beehiveDecorator5,
						beehiveDecorator5,
						beehiveDecorator5,
						beehiveDecorator5,
						beehiveDecorator5,
						beehiveDecorator5
					)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			POTATO_TREE_TALL,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.POTATO_STEM),
					new UpwardsBranchingTrunkPlacer(4, 20, 20, UniformInt.of(1, 8), 0.4F, UniformInt.of(0, 1), holderGetter.getOrThrow(BlockTags.LOGS), false),
					BlockStateProvider.simple(Blocks.POTATO_LEAVES),
					new AcaciaFoliagePlacer(UniformInt.of(2, 4), ConstantInt.of(0)),
					Optional.empty(),
					new TwoLayersFeatureSize(3, 0, 2)
				)
				.decorators(
					List.of(
						new AttachedToLeavesDecorator(
							0.02F,
							false,
							1,
							0,
							List.of(BlockStateProvider.simple(Blocks.POTATO_PEDICULE.defaultBlockState()), BlockStateProvider.simple(Blocks.POTATO_FRUIT.defaultBlockState())),
							3,
							List.of(Direction.DOWN)
						),
						new AttachedToLeavesDecorator(
							0.3F,
							true,
							1,
							0,
							List.of(BlockStateProvider.simple(Blocks.POTATO_PEDICULE.defaultBlockState()), BlockStateProvider.simple(Blocks.POTATO_FRUIT.defaultBlockState())),
							3,
							List.of(Direction.DOWN)
						),
						beehiveDecorator5
					)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			POTATO_TREE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.POTATO_STEM),
					new UpwardsBranchingTrunkPlacer(2, 1, 12, UniformInt.of(1, 6), 0.5F, UniformInt.of(0, 1), holderGetter.getOrThrow(BlockTags.LOGS), false),
					BlockStateProvider.simple(Blocks.POTATO_LEAVES),
					new AcaciaFoliagePlacer(UniformInt.of(2, 3), ConstantInt.of(0)),
					Optional.empty(),
					new TwoLayersFeatureSize(3, 0, 2)
				)
				.decorators(
					List.of(
						new AttachedToLeavesDecorator(
							0.005F,
							false,
							1,
							0,
							List.of(BlockStateProvider.simple(Blocks.POTATO_PEDICULE.defaultBlockState()), BlockStateProvider.simple(Blocks.POTATO_FRUIT.defaultBlockState())),
							3,
							List.of(Direction.DOWN)
						),
						new AttachedToLeavesDecorator(
							0.05F,
							true,
							1,
							0,
							List.of(BlockStateProvider.simple(Blocks.POTATO_PEDICULE.defaultBlockState()), BlockStateProvider.simple(Blocks.POTATO_FRUIT.defaultBlockState())),
							3,
							List.of(Direction.DOWN)
						),
						beehiveDecorator2
					)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(bootstrapContext, OAK, Feature.TREE, createOak().build());
		FeatureUtils.register(
			bootstrapContext,
			DARK_OAK,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.DARK_OAK_LOG),
					new DarkOakTrunkPlacer(6, 2, 1),
					BlockStateProvider.simple(Blocks.DARK_OAK_LEAVES),
					new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)),
					new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty())
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(bootstrapContext, BIRCH, Feature.TREE, createBirch().build());
		FeatureUtils.register(
			bootstrapContext,
			ACACIA,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.ACACIA_LOG),
					new ForkingTrunkPlacer(5, 2, 2),
					BlockStateProvider.simple(Blocks.ACACIA_LEAVES),
					new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
					new TwoLayersFeatureSize(1, 0, 2)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(bootstrapContext, CHERRY, Feature.TREE, cherry().build());
		FeatureUtils.register(bootstrapContext, CHERRY_BEES_005, Feature.TREE, cherry().decorators(List.of(beehiveDecorator4)).build());
		FeatureUtils.register(
			bootstrapContext,
			SPRUCE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.SPRUCE_LOG),
					new StraightTrunkPlacer(5, 2, 1),
					BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
					new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)),
					new TwoLayersFeatureSize(2, 0, 2)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			PINE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.SPRUCE_LOG),
					new StraightTrunkPlacer(6, 4, 0),
					BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
					new PineFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1), UniformInt.of(3, 4)),
					new TwoLayersFeatureSize(2, 0, 2)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			JUNGLE_TREE,
			Feature.TREE,
			createJungleTree().decorators(ImmutableList.of(new CocoaDecorator(0.2F), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25F))).ignoreVines().build()
		);
		FeatureUtils.register(bootstrapContext, FANCY_OAK, Feature.TREE, createFancyOak().build());
		FeatureUtils.register(bootstrapContext, JUNGLE_TREE_NO_VINE, Feature.TREE, createJungleTree().ignoreVines().build());
		FeatureUtils.register(
			bootstrapContext,
			MEGA_JUNGLE_TREE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.JUNGLE_LOG),
					new MegaJungleTrunkPlacer(10, 2, 19),
					BlockStateProvider.simple(Blocks.JUNGLE_LEAVES),
					new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
					new TwoLayersFeatureSize(1, 1, 2)
				)
				.decorators(ImmutableList.of(TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25F)))
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			MEGA_SPRUCE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.SPRUCE_LOG),
					new GiantTrunkPlacer(13, 2, 14),
					BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
					new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(13, 17)),
					new TwoLayersFeatureSize(1, 1, 2)
				)
				.decorators(ImmutableList.of(new AlterGroundDecorator(BlockStateProvider.simple(Blocks.PODZOL))))
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			MEGA_PINE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.SPRUCE_LOG),
					new GiantTrunkPlacer(13, 2, 14),
					BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
					new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 7)),
					new TwoLayersFeatureSize(1, 1, 2)
				)
				.decorators(ImmutableList.of(new AlterGroundDecorator(BlockStateProvider.simple(Blocks.PODZOL))))
				.build()
		);
		FeatureUtils.register(bootstrapContext, SUPER_BIRCH_BEES_0002, Feature.TREE, createSuperBirch().decorators(ImmutableList.of(beehiveDecorator)).build());
		FeatureUtils.register(bootstrapContext, SUPER_BIRCH_BEES, Feature.TREE, createSuperBirch().decorators(ImmutableList.of(beehiveDecorator6)).build());
		FeatureUtils.register(
			bootstrapContext,
			SWAMP_OAK,
			Feature.TREE,
			createStraightBlobTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, 5, 3, 0, 3).decorators(ImmutableList.of(new LeaveVineDecorator(0.25F))).build()
		);
		FeatureUtils.register(
			bootstrapContext,
			JUNGLE_BUSH,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.JUNGLE_LOG),
					new StraightTrunkPlacer(1, 0, 0),
					BlockStateProvider.simple(Blocks.OAK_LEAVES),
					new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2),
					new TwoLayersFeatureSize(0, 0, 0)
				)
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			AZALEA_TREE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.OAK_LOG),
					new BendingTrunkPlacer(4, 2, 0, 3, UniformInt.of(1, 2)),
					new WeightedStateProvider(
						SimpleWeightedRandomList.<BlockState>builder()
							.add(Blocks.AZALEA_LEAVES.defaultBlockState(), 3)
							.add(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 1)
					),
					new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 50),
					new TwoLayersFeatureSize(1, 0, 1)
				)
				.dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT))
				.forceDirt()
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			MANGROVE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.MANGROVE_LOG),
					new UpwardsBranchingTrunkPlacer(
						2, 1, 4, UniformInt.of(1, 4), 0.5F, UniformInt.of(0, 1), holderGetter.getOrThrow(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH), true
					),
					BlockStateProvider.simple(Blocks.MANGROVE_LEAVES),
					new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 70),
					Optional.of(
						new MangroveRootPlacer(
							UniformInt.of(1, 3),
							BlockStateProvider.simple(Blocks.MANGROVE_ROOTS),
							Optional.of(new AboveRootPlacement(BlockStateProvider.simple(Blocks.MOSS_CARPET), 0.5F)),
							new MangroveRootPlacement(
								holderGetter.getOrThrow(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH),
								HolderSet.direct(Block::builtInRegistryHolder, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS),
								BlockStateProvider.simple(Blocks.MUDDY_MANGROVE_ROOTS),
								8,
								15,
								0.2F
							)
						)
					),
					new TwoLayersFeatureSize(2, 0, 2)
				)
				.decorators(
					List.of(
						new LeaveVineDecorator(0.125F),
						new AttachedToLeavesDecorator(
							0.14F,
							false,
							1,
							0,
							List.of(
								new RandomizedIntStateProvider(
									BlockStateProvider.simple(Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, Boolean.valueOf(true))),
									MangrovePropaguleBlock.AGE,
									UniformInt.of(0, 4)
								)
							),
							2,
							List.of(Direction.DOWN)
						),
						beehiveDecorator2
					)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(
			bootstrapContext,
			TALL_MANGROVE,
			Feature.TREE,
			new TreeConfiguration.TreeConfigurationBuilder(
					BlockStateProvider.simple(Blocks.MANGROVE_LOG),
					new UpwardsBranchingTrunkPlacer(
						4, 1, 9, UniformInt.of(1, 6), 0.5F, UniformInt.of(0, 1), holderGetter.getOrThrow(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH), true
					),
					BlockStateProvider.simple(Blocks.MANGROVE_LEAVES),
					new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 70),
					Optional.of(
						new MangroveRootPlacer(
							UniformInt.of(3, 7),
							BlockStateProvider.simple(Blocks.MANGROVE_ROOTS),
							Optional.of(new AboveRootPlacement(BlockStateProvider.simple(Blocks.MOSS_CARPET), 0.5F)),
							new MangroveRootPlacement(
								holderGetter.getOrThrow(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH),
								HolderSet.direct(Block::builtInRegistryHolder, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS),
								BlockStateProvider.simple(Blocks.MUDDY_MANGROVE_ROOTS),
								8,
								15,
								0.2F
							)
						)
					),
					new TwoLayersFeatureSize(3, 0, 2)
				)
				.decorators(
					List.of(
						new LeaveVineDecorator(0.125F),
						new AttachedToLeavesDecorator(
							0.14F,
							false,
							1,
							0,
							List.of(
								new RandomizedIntStateProvider(
									BlockStateProvider.simple(Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, Boolean.valueOf(true))),
									MangrovePropaguleBlock.AGE,
									UniformInt.of(0, 4)
								)
							),
							2,
							List.of(Direction.DOWN)
						),
						beehiveDecorator2
					)
				)
				.ignoreVines()
				.build()
		);
		FeatureUtils.register(bootstrapContext, OAK_BEES_0002, Feature.TREE, createOak().decorators(List.of(beehiveDecorator)).build());
		FeatureUtils.register(bootstrapContext, OAK_BEES_002, Feature.TREE, createOak().decorators(List.of(beehiveDecorator3)).build());
		FeatureUtils.register(bootstrapContext, OAK_BEES_005, Feature.TREE, createOak().decorators(List.of(beehiveDecorator4)).build());
		FeatureUtils.register(bootstrapContext, BIRCH_BEES_0002, Feature.TREE, createBirch().decorators(List.of(beehiveDecorator)).build());
		FeatureUtils.register(bootstrapContext, BIRCH_BEES_002, Feature.TREE, createBirch().decorators(List.of(beehiveDecorator3)).build());
		FeatureUtils.register(bootstrapContext, BIRCH_BEES_005, Feature.TREE, createBirch().decorators(List.of(beehiveDecorator4)).build());
		FeatureUtils.register(bootstrapContext, FANCY_OAK_BEES_0002, Feature.TREE, createFancyOak().decorators(List.of(beehiveDecorator)).build());
		FeatureUtils.register(bootstrapContext, FANCY_OAK_BEES_002, Feature.TREE, createFancyOak().decorators(List.of(beehiveDecorator3)).build());
		FeatureUtils.register(bootstrapContext, FANCY_OAK_BEES_005, Feature.TREE, createFancyOak().decorators(List.of(beehiveDecorator4)).build());
		FeatureUtils.register(bootstrapContext, FANCY_OAK_BEES, Feature.TREE, createFancyOak().decorators(List.of(beehiveDecorator6)).build());
	}
}
