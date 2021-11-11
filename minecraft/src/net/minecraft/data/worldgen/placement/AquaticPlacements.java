package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CarvingMaskPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class AquaticPlacements {
	public static final PlacedFeature SEAGRASS_WARM = PlacementUtils.register("seagrass_warm", AquaticFeatures.SEAGRASS_SHORT.placed(seagrassPlacement(80)));
	public static final PlacedFeature SEAGRASS_NORMAL = PlacementUtils.register("seagrass_normal", AquaticFeatures.SEAGRASS_SHORT.placed(seagrassPlacement(48)));
	public static final PlacedFeature SEAGRASS_COLD = PlacementUtils.register("seagrass_cold", AquaticFeatures.SEAGRASS_SHORT.placed(seagrassPlacement(32)));
	public static final PlacedFeature SEAGRASS_RIVER = PlacementUtils.register(
		"seagrass_river", AquaticFeatures.SEAGRASS_SLIGHTLY_LESS_SHORT.placed(seagrassPlacement(48))
	);
	public static final PlacedFeature SEAGRASS_SWAMP = PlacementUtils.register("seagrass_swamp", AquaticFeatures.SEAGRASS_MID.placed(seagrassPlacement(64)));
	public static final PlacedFeature SEAGRASS_DEEP_WARM = PlacementUtils.register(
		"seagrass_deep_warm", AquaticFeatures.SEAGRASS_TALL.placed(seagrassPlacement(80))
	);
	public static final PlacedFeature SEAGRASS_DEEP = PlacementUtils.register("seagrass_deep", AquaticFeatures.SEAGRASS_TALL.placed(seagrassPlacement(48)));
	public static final PlacedFeature SEAGRASS_DEEP_COLD = PlacementUtils.register(
		"seagrass_deep_cold", AquaticFeatures.SEAGRASS_TALL.placed(seagrassPlacement(40))
	);
	public static final PlacedFeature SEAGRASS_SIMPLE = PlacementUtils.register(
		"seagrass_simple",
		AquaticFeatures.SEAGRASS_SIMPLE
			.placed(
				CarvingMaskPlacement.forStep(GenerationStep.Carving.LIQUID),
				RarityFilter.onAverageOnceEvery(10),
				BlockPredicateFilter.forPredicate(
					BlockPredicate.allOf(
						BlockPredicate.matchesBlock(Blocks.STONE, new BlockPos(0, -1, 0)),
						BlockPredicate.matchesBlock(Blocks.WATER, BlockPos.ZERO),
						BlockPredicate.matchesBlock(Blocks.WATER, new BlockPos(0, 1, 0))
					)
				),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature SEA_PICKLE = PlacementUtils.register(
		"sea_pickle",
		AquaticFeatures.SEA_PICKLE.placed(RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())
	);
	public static final PlacedFeature KELP_COLD = PlacementUtils.register(
		"kelp_cold",
		AquaticFeatures.KELP.placed(NoiseBasedCountPlacement.of(120, 80.0, 0.0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())
	);
	public static final PlacedFeature KELP_WARM = PlacementUtils.register(
		"kelp_warm",
		AquaticFeatures.KELP.placed(NoiseBasedCountPlacement.of(80, 80.0, 0.0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())
	);
	public static final PlacedFeature WARM_OCEAN_VEGETATION = PlacementUtils.register(
		"warm_ocean_vegetation",
		AquaticFeatures.WARM_OCEAN_VEGETATION
			.placed(NoiseBasedCountPlacement.of(20, 400.0, 0.0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())
	);

	public static List<PlacementModifier> seagrassPlacement(int i) {
		return List.of(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, CountPlacement.of(i), BiomeFilter.biome());
	}
}
