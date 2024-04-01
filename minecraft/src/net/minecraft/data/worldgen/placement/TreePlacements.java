package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class TreePlacements {
	public static final ResourceKey<PlacedFeature> CRIMSON_FUNGI = PlacementUtils.createKey("crimson_fungi");
	public static final ResourceKey<PlacedFeature> WARPED_FUNGI = PlacementUtils.createKey("warped_fungi");
	public static final ResourceKey<PlacedFeature> POTATO_TREE = PlacementUtils.createKey("potato_tree");
	public static final ResourceKey<PlacedFeature> POTATO_TREE_TALL = PlacementUtils.createKey("potato_tree_tall");
	public static final ResourceKey<PlacedFeature> MOTHER_POTATO_TREE = PlacementUtils.createKey("mother_potato_tree");
	public static final ResourceKey<PlacedFeature> POTATO_CHECKED = PlacementUtils.createKey("potato_checked");
	public static final ResourceKey<PlacedFeature> MOTHER_POTATO_CHECKED = PlacementUtils.createKey("mother_potato_checked");
	public static final ResourceKey<PlacedFeature> OAK_CHECKED = PlacementUtils.createKey("oak_checked");
	public static final ResourceKey<PlacedFeature> DARK_OAK_CHECKED = PlacementUtils.createKey("dark_oak_checked");
	public static final ResourceKey<PlacedFeature> BIRCH_CHECKED = PlacementUtils.createKey("birch_checked");
	public static final ResourceKey<PlacedFeature> ACACIA_CHECKED = PlacementUtils.createKey("acacia_checked");
	public static final ResourceKey<PlacedFeature> SPRUCE_CHECKED = PlacementUtils.createKey("spruce_checked");
	public static final ResourceKey<PlacedFeature> MANGROVE_CHECKED = PlacementUtils.createKey("mangrove_checked");
	public static final ResourceKey<PlacedFeature> CHERRY_CHECKED = PlacementUtils.createKey("cherry_checked");
	public static final ResourceKey<PlacedFeature> PINE_ON_SNOW = PlacementUtils.createKey("pine_on_snow");
	public static final ResourceKey<PlacedFeature> SPRUCE_ON_SNOW = PlacementUtils.createKey("spruce_on_snow");
	public static final ResourceKey<PlacedFeature> PINE_CHECKED = PlacementUtils.createKey("pine_checked");
	public static final ResourceKey<PlacedFeature> JUNGLE_TREE_CHECKED = PlacementUtils.createKey("jungle_tree");
	public static final ResourceKey<PlacedFeature> FANCY_OAK_CHECKED = PlacementUtils.createKey("fancy_oak_checked");
	public static final ResourceKey<PlacedFeature> MEGA_JUNGLE_TREE_CHECKED = PlacementUtils.createKey("mega_jungle_tree_checked");
	public static final ResourceKey<PlacedFeature> MEGA_SPRUCE_CHECKED = PlacementUtils.createKey("mega_spruce_checked");
	public static final ResourceKey<PlacedFeature> MEGA_PINE_CHECKED = PlacementUtils.createKey("mega_pine_checked");
	public static final ResourceKey<PlacedFeature> TALL_MANGROVE_CHECKED = PlacementUtils.createKey("tall_mangrove_checked");
	public static final ResourceKey<PlacedFeature> JUNGLE_BUSH = PlacementUtils.createKey("jungle_bush");
	public static final ResourceKey<PlacedFeature> SUPER_BIRCH_BEES_0002 = PlacementUtils.createKey("super_birch_bees_0002");
	public static final ResourceKey<PlacedFeature> SUPER_BIRCH_BEES = PlacementUtils.createKey("super_birch_bees");
	public static final ResourceKey<PlacedFeature> OAK_BEES_0002 = PlacementUtils.createKey("oak_bees_0002");
	public static final ResourceKey<PlacedFeature> OAK_BEES_002 = PlacementUtils.createKey("oak_bees_002");
	public static final ResourceKey<PlacedFeature> BIRCH_BEES_0002_PLACED = PlacementUtils.createKey("birch_bees_0002");
	public static final ResourceKey<PlacedFeature> BIRCH_BEES_002 = PlacementUtils.createKey("birch_bees_002");
	public static final ResourceKey<PlacedFeature> FANCY_OAK_BEES_0002 = PlacementUtils.createKey("fancy_oak_bees_0002");
	public static final ResourceKey<PlacedFeature> FANCY_OAK_BEES_002 = PlacementUtils.createKey("fancy_oak_bees_002");
	public static final ResourceKey<PlacedFeature> FANCY_OAK_BEES = PlacementUtils.createKey("fancy_oak_bees");
	public static final ResourceKey<PlacedFeature> CHERRY_BEES_005 = PlacementUtils.createKey("cherry_bees_005");

	public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
		HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
		Holder<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(TreeFeatures.CRIMSON_FUNGUS);
		Holder<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(TreeFeatures.WARPED_FUNGUS);
		Holder<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(TreeFeatures.POTATO_TREE_TALL);
		Holder<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(TreeFeatures.MOTHER_POTATO_TREE);
		Holder<ConfiguredFeature<?, ?>> holder5 = holderGetter.getOrThrow(TreeFeatures.POTATO_TREE);
		Holder<ConfiguredFeature<?, ?>> holder6 = holderGetter.getOrThrow(TreeFeatures.OAK);
		Holder<ConfiguredFeature<?, ?>> holder7 = holderGetter.getOrThrow(TreeFeatures.DARK_OAK);
		Holder<ConfiguredFeature<?, ?>> holder8 = holderGetter.getOrThrow(TreeFeatures.BIRCH);
		Holder<ConfiguredFeature<?, ?>> holder9 = holderGetter.getOrThrow(TreeFeatures.ACACIA);
		Holder<ConfiguredFeature<?, ?>> holder10 = holderGetter.getOrThrow(TreeFeatures.SPRUCE);
		Holder<ConfiguredFeature<?, ?>> holder11 = holderGetter.getOrThrow(TreeFeatures.MANGROVE);
		Holder<ConfiguredFeature<?, ?>> holder12 = holderGetter.getOrThrow(TreeFeatures.CHERRY);
		Holder<ConfiguredFeature<?, ?>> holder13 = holderGetter.getOrThrow(TreeFeatures.PINE);
		Holder<ConfiguredFeature<?, ?>> holder14 = holderGetter.getOrThrow(TreeFeatures.JUNGLE_TREE);
		Holder<ConfiguredFeature<?, ?>> holder15 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK);
		Holder<ConfiguredFeature<?, ?>> holder16 = holderGetter.getOrThrow(TreeFeatures.MEGA_JUNGLE_TREE);
		Holder<ConfiguredFeature<?, ?>> holder17 = holderGetter.getOrThrow(TreeFeatures.MEGA_SPRUCE);
		Holder<ConfiguredFeature<?, ?>> holder18 = holderGetter.getOrThrow(TreeFeatures.MEGA_PINE);
		Holder<ConfiguredFeature<?, ?>> holder19 = holderGetter.getOrThrow(TreeFeatures.TALL_MANGROVE);
		Holder<ConfiguredFeature<?, ?>> holder20 = holderGetter.getOrThrow(TreeFeatures.JUNGLE_BUSH);
		Holder<ConfiguredFeature<?, ?>> holder21 = holderGetter.getOrThrow(TreeFeatures.SUPER_BIRCH_BEES_0002);
		Holder<ConfiguredFeature<?, ?>> holder22 = holderGetter.getOrThrow(TreeFeatures.SUPER_BIRCH_BEES);
		Holder<ConfiguredFeature<?, ?>> holder23 = holderGetter.getOrThrow(TreeFeatures.OAK_BEES_0002);
		Holder<ConfiguredFeature<?, ?>> holder24 = holderGetter.getOrThrow(TreeFeatures.OAK_BEES_002);
		Holder<ConfiguredFeature<?, ?>> holder25 = holderGetter.getOrThrow(TreeFeatures.BIRCH_BEES_0002);
		Holder<ConfiguredFeature<?, ?>> holder26 = holderGetter.getOrThrow(TreeFeatures.BIRCH_BEES_002);
		Holder<ConfiguredFeature<?, ?>> holder27 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES_0002);
		Holder<ConfiguredFeature<?, ?>> holder28 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES_002);
		Holder<ConfiguredFeature<?, ?>> holder29 = holderGetter.getOrThrow(TreeFeatures.FANCY_OAK_BEES);
		Holder<ConfiguredFeature<?, ?>> holder30 = holderGetter.getOrThrow(TreeFeatures.CHERRY_BEES_005);
		PlacementUtils.register(bootstrapContext, CRIMSON_FUNGI, holder, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, WARPED_FUNGI, holder2, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
		PlacementUtils.register(
			bootstrapContext, POTATO_TREE, holder5, CountOnEveryLayerPlacement.of(6), BiomeFilter.biome(), PlacementUtils.filteredByBlockSurvival(Blocks.POTATO_SPROUTS)
		);
		PlacementUtils.register(
			bootstrapContext,
			POTATO_TREE_TALL,
			holder3,
			RarityFilter.onAverageOnceEvery(6),
			CountOnEveryLayerPlacement.of(1),
			BiomeFilter.biome(),
			PlacementUtils.filteredByBlockSurvival(Blocks.POTATO_SPROUTS)
		);
		PlacementUtils.register(
			bootstrapContext,
			MOTHER_POTATO_TREE,
			holder4,
			RarityFilter.onAverageOnceEvery(48),
			CountOnEveryLayerPlacement.of(1),
			BiomeFilter.biome(),
			PlacementUtils.filteredByBlockSurvival(Blocks.POTATO_SPROUTS)
		);
		PlacementUtils.register(bootstrapContext, POTATO_CHECKED, holder5, PlacementUtils.filteredByBlockSurvival(Blocks.POTATO_SPROUTS));
		PlacementUtils.register(bootstrapContext, MOTHER_POTATO_CHECKED, holder4, PlacementUtils.filteredByBlockSurvival(Blocks.POTATO_SPROUTS));
		PlacementUtils.register(bootstrapContext, OAK_CHECKED, holder6, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, DARK_OAK_CHECKED, holder7, PlacementUtils.filteredByBlockSurvival(Blocks.DARK_OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, BIRCH_CHECKED, holder8, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
		PlacementUtils.register(bootstrapContext, ACACIA_CHECKED, holder9, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
		PlacementUtils.register(bootstrapContext, SPRUCE_CHECKED, holder10, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(bootstrapContext, MANGROVE_CHECKED, holder11, PlacementUtils.filteredByBlockSurvival(Blocks.MANGROVE_PROPAGULE));
		PlacementUtils.register(bootstrapContext, CHERRY_CHECKED, holder12, PlacementUtils.filteredByBlockSurvival(Blocks.CHERRY_SAPLING));
		BlockPredicate blockPredicate = BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
		List<PlacementModifier> list = List.of(
			EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.not(BlockPredicate.matchesBlocks(Blocks.POWDER_SNOW)), 8),
			BlockPredicateFilter.forPredicate(blockPredicate)
		);
		PlacementUtils.register(bootstrapContext, PINE_ON_SNOW, holder13, list);
		PlacementUtils.register(bootstrapContext, SPRUCE_ON_SNOW, holder10, list);
		PlacementUtils.register(bootstrapContext, PINE_CHECKED, holder13, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(bootstrapContext, JUNGLE_TREE_CHECKED, holder14, PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING));
		PlacementUtils.register(bootstrapContext, FANCY_OAK_CHECKED, holder15, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, MEGA_JUNGLE_TREE_CHECKED, holder16, PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING));
		PlacementUtils.register(bootstrapContext, MEGA_SPRUCE_CHECKED, holder17, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(bootstrapContext, MEGA_PINE_CHECKED, holder18, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(bootstrapContext, TALL_MANGROVE_CHECKED, holder19, PlacementUtils.filteredByBlockSurvival(Blocks.MANGROVE_PROPAGULE));
		PlacementUtils.register(bootstrapContext, JUNGLE_BUSH, holder20, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, SUPER_BIRCH_BEES_0002, holder21, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
		PlacementUtils.register(bootstrapContext, SUPER_BIRCH_BEES, holder22, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
		PlacementUtils.register(bootstrapContext, OAK_BEES_0002, holder23, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, OAK_BEES_002, holder24, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, BIRCH_BEES_0002_PLACED, holder25, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
		PlacementUtils.register(bootstrapContext, BIRCH_BEES_002, holder26, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
		PlacementUtils.register(bootstrapContext, FANCY_OAK_BEES_0002, holder27, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, FANCY_OAK_BEES_002, holder28, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, FANCY_OAK_BEES, holder29, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, CHERRY_BEES_005, holder30, PlacementUtils.filteredByBlockSurvival(Blocks.CHERRY_SAPLING));
	}
}
