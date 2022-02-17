package net.minecraft.data.worldgen.placement;

import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class AncientCityPlacements {
	public static final PlacedFeature SCULK_CATALYST_WITH_PATCHES_CITY = PlacementUtils.register(
		"sculk_catalyst_with_patches_city", CaveFeatures.SCULK_CATALYST_WITH_PATCHES.placed()
	);
}
