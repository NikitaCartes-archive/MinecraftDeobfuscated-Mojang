package net.minecraft.data.worldgen.placement;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacementUtils {
	public static final PlacementModifier HEIGHTMAP = HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING);
	public static final PlacementModifier HEIGHTMAP_TOP_SOLID = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR_WG);
	public static final PlacementModifier HEIGHTMAP_WORLD_SURFACE = HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG);
	public static final PlacementModifier HEIGHTMAP_OCEAN_FLOOR = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR);
	public static final PlacementModifier FULL_RANGE = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top());
	public static final PlacementModifier RANGE_10_10 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10));
	public static final PlacementModifier RANGE_8_8 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8));
	public static final PlacementModifier RANGE_4_4 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4));
	public static final PlacementModifier RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(120));

	public static PlacedFeature bootstrap() {
		PlacedFeature[] placedFeatures = new PlacedFeature[]{
			AquaticPlacements.KELP_COLD,
			CavePlacements.CAVE_VINES,
			EndPlacements.CHORUS_PLANT,
			MiscOverworldPlacements.BLUE_ICE,
			NetherPlacements.BASALT_BLOBS,
			OrePlacements.ORE_ANCIENT_DEBRIS_LARGE,
			TreePlacements.ACACIA_CHECKED,
			VegetationPlacements.BAMBOO_VEGETATION
		};
		return Util.getRandom(placedFeatures, new Random());
	}

	public static PlacedFeature register(String string, PlacedFeature placedFeature) {
		return Registry.register(BuiltinRegistries.PLACED_FEATURE, string, placedFeature);
	}

	public static PlacementModifier countExtra(int i, float f, int j) {
		float g = 1.0F / f;
		if (Math.abs(g - (float)((int)g)) > 1.0E-5F) {
			throw new IllegalStateException("Chance data cannot be represented as list weight");
		} else {
			SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList = SimpleWeightedRandomList.<IntProvider>builder()
				.add(ConstantInt.of(i), (int)g - 1)
				.add(ConstantInt.of(i + j), 1)
				.build();
			return CountPlacement.of(new WeightedListInt(simpleWeightedRandomList));
		}
	}
}
