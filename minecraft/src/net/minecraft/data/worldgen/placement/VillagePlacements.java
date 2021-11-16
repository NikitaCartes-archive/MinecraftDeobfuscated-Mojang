package net.minecraft.data.worldgen.placement;

import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VillagePlacements {
	public static final PlacedFeature PILE_HAY_VILLAGE = PlacementUtils.register("pile_hay", PileFeatures.PILE_HAY.placed());
	public static final PlacedFeature PILE_MELON_VILLAGE = PlacementUtils.register("pile_melon", PileFeatures.PILE_MELON.placed());
	public static final PlacedFeature PILE_SNOW_VILLAGE = PlacementUtils.register("pile_snow", PileFeatures.PILE_SNOW.placed());
	public static final PlacedFeature PILE_ICE_VILLAGE = PlacementUtils.register("pile_ice", PileFeatures.PILE_ICE.placed());
	public static final PlacedFeature PILE_PUMPKIN_VILLAGE = PlacementUtils.register("pile_pumpkin", PileFeatures.PILE_PUMPKIN.placed());
	public static final PlacedFeature OAK_VILLAGE = PlacementUtils.register("oak", TreeFeatures.OAK.filteredByBlockSurvival(Blocks.OAK_SAPLING));
	public static final PlacedFeature ACACIA_VILLAGE = PlacementUtils.register("acacia", TreeFeatures.ACACIA.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
	public static final PlacedFeature SPRUCE_VILLAGE = PlacementUtils.register("spruce", TreeFeatures.SPRUCE.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
	public static final PlacedFeature PINE_VILLAGE = PlacementUtils.register("pine", TreeFeatures.PINE.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
	public static final PlacedFeature PATCH_CACTUS_VILLAGE = PlacementUtils.register("patch_cactus", VegetationFeatures.PATCH_CACTUS.placed());
	public static final PlacedFeature FLOWER_PLAIN_VILLAGE = PlacementUtils.register("flower_plain", VegetationFeatures.FLOWER_PLAIN.placed());
	public static final PlacedFeature PATCH_TAIGA_GRASS_VILLAGE = PlacementUtils.register("patch_taiga_grass", VegetationFeatures.PATCH_TAIGA_GRASS.placed());
	public static final PlacedFeature PATCH_BERRY_BUSH_VILLAGE = PlacementUtils.register("patch_berry_bush", VegetationFeatures.PATCH_BERRY_BUSH.placed());
}
