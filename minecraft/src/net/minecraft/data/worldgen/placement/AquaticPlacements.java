package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
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
	public static final ResourceKey<PlacedFeature> SEAGRASS_WARM = PlacementUtils.createKey("seagrass_warm");
	public static final ResourceKey<PlacedFeature> SEAGRASS_NORMAL = PlacementUtils.createKey("seagrass_normal");
	public static final ResourceKey<PlacedFeature> SEAGRASS_COLD = PlacementUtils.createKey("seagrass_cold");
	public static final ResourceKey<PlacedFeature> SEAGRASS_RIVER = PlacementUtils.createKey("seagrass_river");
	public static final ResourceKey<PlacedFeature> SEAGRASS_SWAMP = PlacementUtils.createKey("seagrass_swamp");
	public static final ResourceKey<PlacedFeature> SEAGRASS_DEEP_WARM = PlacementUtils.createKey("seagrass_deep_warm");
	public static final ResourceKey<PlacedFeature> SEAGRASS_DEEP = PlacementUtils.createKey("seagrass_deep");
	public static final ResourceKey<PlacedFeature> SEAGRASS_DEEP_COLD = PlacementUtils.createKey("seagrass_deep_cold");
	public static final ResourceKey<PlacedFeature> SEAGRASS_SIMPLE = PlacementUtils.createKey("seagrass_simple");
	public static final ResourceKey<PlacedFeature> SEA_PICKLE = PlacementUtils.createKey("sea_pickle");
	public static final ResourceKey<PlacedFeature> KELP_COLD = PlacementUtils.createKey("kelp_cold");
	public static final ResourceKey<PlacedFeature> KELP_WARM = PlacementUtils.createKey("kelp_warm");
	public static final ResourceKey<PlacedFeature> WARM_OCEAN_VEGETATION = PlacementUtils.createKey("warm_ocean_vegetation");

	private static List<PlacementModifier> seagrassPlacement(int i) {
		return List.of(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, CountPlacement.of(i), BiomeFilter.biome());
	}

	public static void bootstrap(BootstapContext<PlacedFeature> bootstapContext) {
		HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstapContext.lookup(Registries.CONFIGURED_FEATURE);
		Holder.Reference<ConfiguredFeature<?, ?>> reference = holderGetter.getOrThrow(AquaticFeatures.SEAGRASS_SHORT);
		Holder.Reference<ConfiguredFeature<?, ?>> reference2 = holderGetter.getOrThrow(AquaticFeatures.SEAGRASS_SLIGHTLY_LESS_SHORT);
		Holder.Reference<ConfiguredFeature<?, ?>> reference3 = holderGetter.getOrThrow(AquaticFeatures.SEAGRASS_MID);
		Holder.Reference<ConfiguredFeature<?, ?>> reference4 = holderGetter.getOrThrow(AquaticFeatures.SEAGRASS_TALL);
		Holder.Reference<ConfiguredFeature<?, ?>> reference5 = holderGetter.getOrThrow(AquaticFeatures.SEAGRASS_SIMPLE);
		Holder.Reference<ConfiguredFeature<?, ?>> reference6 = holderGetter.getOrThrow(AquaticFeatures.SEA_PICKLE);
		Holder.Reference<ConfiguredFeature<?, ?>> reference7 = holderGetter.getOrThrow(AquaticFeatures.KELP);
		Holder.Reference<ConfiguredFeature<?, ?>> reference8 = holderGetter.getOrThrow(AquaticFeatures.WARM_OCEAN_VEGETATION);
		PlacementUtils.register(bootstapContext, SEAGRASS_WARM, reference, seagrassPlacement(80));
		PlacementUtils.register(bootstapContext, SEAGRASS_NORMAL, reference, seagrassPlacement(48));
		PlacementUtils.register(bootstapContext, SEAGRASS_COLD, reference, seagrassPlacement(32));
		PlacementUtils.register(bootstapContext, SEAGRASS_RIVER, reference2, seagrassPlacement(48));
		PlacementUtils.register(bootstapContext, SEAGRASS_SWAMP, reference3, seagrassPlacement(64));
		PlacementUtils.register(bootstapContext, SEAGRASS_DEEP_WARM, reference4, seagrassPlacement(80));
		PlacementUtils.register(bootstapContext, SEAGRASS_DEEP, reference4, seagrassPlacement(48));
		PlacementUtils.register(bootstapContext, SEAGRASS_DEEP_COLD, reference4, seagrassPlacement(40));
		PlacementUtils.register(
			bootstapContext,
			SEAGRASS_SIMPLE,
			reference5,
			CarvingMaskPlacement.forStep(GenerationStep.Carving.LIQUID),
			RarityFilter.onAverageOnceEvery(10),
			BlockPredicateFilter.forPredicate(
				BlockPredicate.allOf(
					BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.STONE),
					BlockPredicate.matchesBlocks(BlockPos.ZERO, Blocks.WATER),
					BlockPredicate.matchesBlocks(Direction.UP.getNormal(), Blocks.WATER)
				)
			),
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			SEA_PICKLE,
			reference6,
			RarityFilter.onAverageOnceEvery(16),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_TOP_SOLID,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			KELP_COLD,
			reference7,
			NoiseBasedCountPlacement.of(120, 80.0, 0.0),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_TOP_SOLID,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			KELP_WARM,
			reference7,
			NoiseBasedCountPlacement.of(80, 80.0, 0.0),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_TOP_SOLID,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			WARM_OCEAN_VEGETATION,
			reference8,
			NoiseBasedCountPlacement.of(20, 400.0, 0.0),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_TOP_SOLID,
			BiomeFilter.biome()
		);
	}
}
