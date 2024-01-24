package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VillagePlacements {
	public static final ResourceKey<PlacedFeature> PILE_HAY_VILLAGE = PlacementUtils.createKey("pile_hay");
	public static final ResourceKey<PlacedFeature> PILE_MELON_VILLAGE = PlacementUtils.createKey("pile_melon");
	public static final ResourceKey<PlacedFeature> PILE_SNOW_VILLAGE = PlacementUtils.createKey("pile_snow");
	public static final ResourceKey<PlacedFeature> PILE_ICE_VILLAGE = PlacementUtils.createKey("pile_ice");
	public static final ResourceKey<PlacedFeature> PILE_PUMPKIN_VILLAGE = PlacementUtils.createKey("pile_pumpkin");
	public static final ResourceKey<PlacedFeature> OAK_VILLAGE = PlacementUtils.createKey("oak");
	public static final ResourceKey<PlacedFeature> ACACIA_VILLAGE = PlacementUtils.createKey("acacia");
	public static final ResourceKey<PlacedFeature> SPRUCE_VILLAGE = PlacementUtils.createKey("spruce");
	public static final ResourceKey<PlacedFeature> PINE_VILLAGE = PlacementUtils.createKey("pine");
	public static final ResourceKey<PlacedFeature> PATCH_CACTUS_VILLAGE = PlacementUtils.createKey("patch_cactus");
	public static final ResourceKey<PlacedFeature> FLOWER_PLAIN_VILLAGE = PlacementUtils.createKey("flower_plain");
	public static final ResourceKey<PlacedFeature> PATCH_TAIGA_GRASS_VILLAGE = PlacementUtils.createKey("patch_taiga_grass");
	public static final ResourceKey<PlacedFeature> PATCH_BERRY_BUSH_VILLAGE = PlacementUtils.createKey("patch_berry_bush");

	public static void bootstrap(BootstrapContext<PlacedFeature> bootstrapContext) {
		HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstrapContext.lookup(Registries.CONFIGURED_FEATURE);
		Holder<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(PileFeatures.PILE_HAY);
		Holder<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(PileFeatures.PILE_MELON);
		Holder<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(PileFeatures.PILE_SNOW);
		Holder<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(PileFeatures.PILE_ICE);
		Holder<ConfiguredFeature<?, ?>> holder5 = holderGetter.getOrThrow(PileFeatures.PILE_PUMPKIN);
		Holder<ConfiguredFeature<?, ?>> holder6 = holderGetter.getOrThrow(TreeFeatures.OAK);
		Holder<ConfiguredFeature<?, ?>> holder7 = holderGetter.getOrThrow(TreeFeatures.ACACIA);
		Holder<ConfiguredFeature<?, ?>> holder8 = holderGetter.getOrThrow(TreeFeatures.SPRUCE);
		Holder<ConfiguredFeature<?, ?>> holder9 = holderGetter.getOrThrow(TreeFeatures.PINE);
		Holder<ConfiguredFeature<?, ?>> holder10 = holderGetter.getOrThrow(VegetationFeatures.PATCH_CACTUS);
		Holder<ConfiguredFeature<?, ?>> holder11 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
		Holder<ConfiguredFeature<?, ?>> holder12 = holderGetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
		Holder<ConfiguredFeature<?, ?>> holder13 = holderGetter.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
		PlacementUtils.register(bootstrapContext, PILE_HAY_VILLAGE, holder);
		PlacementUtils.register(bootstrapContext, PILE_MELON_VILLAGE, holder2);
		PlacementUtils.register(bootstrapContext, PILE_SNOW_VILLAGE, holder3);
		PlacementUtils.register(bootstrapContext, PILE_ICE_VILLAGE, holder4);
		PlacementUtils.register(bootstrapContext, PILE_PUMPKIN_VILLAGE, holder5);
		PlacementUtils.register(bootstrapContext, OAK_VILLAGE, holder6, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstrapContext, ACACIA_VILLAGE, holder7, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
		PlacementUtils.register(bootstrapContext, SPRUCE_VILLAGE, holder8, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(bootstrapContext, PINE_VILLAGE, holder9, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(bootstrapContext, PATCH_CACTUS_VILLAGE, holder10);
		PlacementUtils.register(bootstrapContext, FLOWER_PLAIN_VILLAGE, holder11);
		PlacementUtils.register(bootstrapContext, PATCH_TAIGA_GRASS_VILLAGE, holder12);
		PlacementUtils.register(bootstrapContext, PATCH_BERRY_BUSH_VILLAGE, holder13);
	}
}
