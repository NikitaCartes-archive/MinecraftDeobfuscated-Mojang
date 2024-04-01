package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class NetherPlacements {
	public static final ResourceKey<PlacedFeature> DELTA = PlacementUtils.createKey("delta");
	public static final ResourceKey<PlacedFeature> POISON_POOL = PlacementUtils.createKey("poison_pool");
	public static final ResourceKey<PlacedFeature> SMALL_BASALT_COLUMNS = PlacementUtils.createKey("small_basalt_columns");
	public static final ResourceKey<PlacedFeature> SMALL_DEBRIS_COLUMNS = PlacementUtils.createKey("small_debris_columns");
	public static final ResourceKey<PlacedFeature> LARGE_BASALT_COLUMNS = PlacementUtils.createKey("large_basalt_columns");
	public static final ResourceKey<PlacedFeature> LARGE_POTATO_COLUMNS = PlacementUtils.createKey("large_potato_columns");
	public static final ResourceKey<PlacedFeature> BASALT_BLOBS = PlacementUtils.createKey("basalt_blobs");
	public static final ResourceKey<PlacedFeature> BLACKSTONE_BLOBS = PlacementUtils.createKey("blackstone_blobs");
	public static final ResourceKey<PlacedFeature> GLOWSTONE_EXTRA = PlacementUtils.createKey("glowstone_extra");
	public static final ResourceKey<PlacedFeature> GLOWSTONE = PlacementUtils.createKey("glowstone");
	public static final ResourceKey<PlacedFeature> CRIMSON_FOREST_VEGETATION = PlacementUtils.createKey("crimson_forest_vegetation");
	public static final ResourceKey<PlacedFeature> WARPED_FOREST_VEGETATION = PlacementUtils.createKey("warped_forest_vegetation");
	public static final ResourceKey<PlacedFeature> NETHER_SPROUTS = PlacementUtils.createKey("nether_sprouts");
	public static final ResourceKey<PlacedFeature> TWISTING_VINES = PlacementUtils.createKey("twisting_vines");
	public static final ResourceKey<PlacedFeature> CORRUPTED_BUDS = PlacementUtils.createKey("corrupted_buds");
	public static final ResourceKey<PlacedFeature> POTATO_SPROUTS = PlacementUtils.createKey("potato_sprouts");
	public static final ResourceKey<PlacedFeature> WEEPING_VINES = PlacementUtils.createKey("weeping_vines");
	public static final ResourceKey<PlacedFeature> PATCH_CRIMSON_ROOTS = PlacementUtils.createKey("patch_crimson_roots");
	public static final ResourceKey<PlacedFeature> BASALT_PILLAR = PlacementUtils.createKey("basalt_pillar");
	public static final ResourceKey<PlacedFeature> SPRING_DELTA = PlacementUtils.createKey("spring_delta");
	public static final ResourceKey<PlacedFeature> SPRING_CLOSED = PlacementUtils.createKey("spring_closed");
	public static final ResourceKey<PlacedFeature> SPRING_CLOSED_DOUBLE = PlacementUtils.createKey("spring_closed_double");
	public static final ResourceKey<PlacedFeature> SPRING_OPEN = PlacementUtils.createKey("spring_open");
	public static final ResourceKey<PlacedFeature> PATCH_SOUL_FIRE = PlacementUtils.createKey("patch_soul_fire");
	public static final ResourceKey<PlacedFeature> PATCH_FIRE = PlacementUtils.createKey("patch_fire");

	public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
		HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
		Holder<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(NetherFeatures.DELTA);
		Holder<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(NetherFeatures.POISON);
		Holder<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(NetherFeatures.SMALL_BASALT_COLUMNS);
		Holder<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(NetherFeatures.SMALL_DEBRIS_COLUMNS);
		Holder<ConfiguredFeature<?, ?>> holder5 = holderGetter.getOrThrow(NetherFeatures.LARGE_BASALT_COLUMNS);
		Holder<ConfiguredFeature<?, ?>> holder6 = holderGetter.getOrThrow(NetherFeatures.LARGE_POTATO_COLUMNS);
		Holder<ConfiguredFeature<?, ?>> holder7 = holderGetter.getOrThrow(NetherFeatures.BASALT_BLOBS);
		Holder<ConfiguredFeature<?, ?>> holder8 = holderGetter.getOrThrow(NetherFeatures.BLACKSTONE_BLOBS);
		Holder<ConfiguredFeature<?, ?>> holder9 = holderGetter.getOrThrow(NetherFeatures.GLOWSTONE_EXTRA);
		Holder<ConfiguredFeature<?, ?>> holder10 = holderGetter.getOrThrow(NetherFeatures.CRIMSON_FOREST_VEGETATION);
		Holder<ConfiguredFeature<?, ?>> holder11 = holderGetter.getOrThrow(NetherFeatures.WARPED_FOREST_VEGETION);
		Holder<ConfiguredFeature<?, ?>> holder12 = holderGetter.getOrThrow(NetherFeatures.NETHER_SPROUTS);
		Holder<ConfiguredFeature<?, ?>> holder13 = holderGetter.getOrThrow(NetherFeatures.TWISTING_VINES);
		Holder<ConfiguredFeature<?, ?>> holder14 = holderGetter.getOrThrow(NetherFeatures.CORRUPTED_BUDS);
		Holder<ConfiguredFeature<?, ?>> holder15 = holderGetter.getOrThrow(NetherFeatures.POTATO_SPROUTS);
		Holder<ConfiguredFeature<?, ?>> holder16 = holderGetter.getOrThrow(NetherFeatures.WEEPING_VINES);
		Holder<ConfiguredFeature<?, ?>> holder17 = holderGetter.getOrThrow(NetherFeatures.PATCH_CRIMSON_ROOTS);
		Holder<ConfiguredFeature<?, ?>> holder18 = holderGetter.getOrThrow(NetherFeatures.BASALT_PILLAR);
		Holder<ConfiguredFeature<?, ?>> holder19 = holderGetter.getOrThrow(NetherFeatures.SPRING_LAVA_NETHER);
		Holder<ConfiguredFeature<?, ?>> holder20 = holderGetter.getOrThrow(NetherFeatures.SPRING_NETHER_CLOSED);
		Holder<ConfiguredFeature<?, ?>> holder21 = holderGetter.getOrThrow(NetherFeatures.SPRING_NETHER_OPEN);
		Holder<ConfiguredFeature<?, ?>> holder22 = holderGetter.getOrThrow(NetherFeatures.PATCH_SOUL_FIRE);
		Holder<ConfiguredFeature<?, ?>> holder23 = holderGetter.getOrThrow(NetherFeatures.PATCH_FIRE);
		PlacementUtils.register(bootstrapContext, DELTA, holder, CountOnEveryLayerPlacement.of(40), BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, POISON_POOL, holder2, CountOnEveryLayerPlacement.of(40), BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, SMALL_BASALT_COLUMNS, holder3, CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
		PlacementUtils.register(
			bootstrapContext, SMALL_DEBRIS_COLUMNS, holder4, CountOnEveryLayerPlacement.of(1), RarityFilter.onAverageOnceEvery(3), BiomeFilter.biome()
		);
		PlacementUtils.register(bootstrapContext, LARGE_BASALT_COLUMNS, holder5, CountOnEveryLayerPlacement.of(2), BiomeFilter.biome());
		PlacementUtils.register(
			bootstrapContext, LARGE_POTATO_COLUMNS, holder6, CountOnEveryLayerPlacement.of(1), RarityFilter.onAverageOnceEvery(3), BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, BASALT_BLOBS, holder7, CountPlacement.of(75), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, BLACKSTONE_BLOBS, holder8, CountPlacement.of(25), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext,
			GLOWSTONE_EXTRA,
			holder9,
			CountPlacement.of(BiasedToBottomInt.of(0, 9)),
			InSquarePlacement.spread(),
			PlacementUtils.RANGE_4_4,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, GLOWSTONE, holder9, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(bootstrapContext, CRIMSON_FOREST_VEGETATION, holder10, CountOnEveryLayerPlacement.of(6), BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, WARPED_FOREST_VEGETATION, holder11, CountOnEveryLayerPlacement.of(5), BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, NETHER_SPROUTS, holder12, CountOnEveryLayerPlacement.of(4), BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, POTATO_SPROUTS, holder15, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
		PlacementUtils.register(
			bootstrapContext, TWISTING_VINES, holder13, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, CORRUPTED_BUDS, holder14, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, WEEPING_VINES, holder16, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(bootstrapContext, PATCH_CRIMSON_ROOTS, holder17, PlacementUtils.FULL_RANGE, BiomeFilter.biome());
		PlacementUtils.register(
			bootstrapContext, BASALT_PILLAR, holder18, CountPlacement.of(10), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, SPRING_DELTA, holder19, CountPlacement.of(16), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, SPRING_CLOSED, holder20, CountPlacement.of(16), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, SPRING_CLOSED_DOUBLE, holder20, CountPlacement.of(32), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstrapContext, SPRING_OPEN, holder21, CountPlacement.of(8), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome()
		);
		List<PlacementModifier> list = List.of(CountPlacement.of(UniformInt.of(0, 5)), InSquarePlacement.spread(), PlacementUtils.RANGE_4_4, BiomeFilter.biome());
		PlacementUtils.register(bootstrapContext, PATCH_SOUL_FIRE, holder22, list);
		PlacementUtils.register(bootstrapContext, PATCH_FIRE, holder23, list);
	}
}
