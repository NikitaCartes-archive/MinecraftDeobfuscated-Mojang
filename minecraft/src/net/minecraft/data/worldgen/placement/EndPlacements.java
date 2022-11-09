package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class EndPlacements {
	public static final ResourceKey<PlacedFeature> END_SPIKE = PlacementUtils.createKey("end_spike");
	public static final ResourceKey<PlacedFeature> END_GATEWAY_RETURN = PlacementUtils.createKey("end_gateway_return");
	public static final ResourceKey<PlacedFeature> CHORUS_PLANT = PlacementUtils.createKey("chorus_plant");
	public static final ResourceKey<PlacedFeature> END_ISLAND_DECORATED = PlacementUtils.createKey("end_island_decorated");

	public static void bootstrap(BootstapContext<PlacedFeature> bootstapContext) {
		HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstapContext.lookup(Registries.CONFIGURED_FEATURE);
		Holder<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(EndFeatures.END_SPIKE);
		Holder<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(EndFeatures.END_GATEWAY_RETURN);
		Holder<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(EndFeatures.CHORUS_PLANT);
		Holder<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(EndFeatures.END_ISLAND);
		PlacementUtils.register(bootstapContext, END_SPIKE, holder, BiomeFilter.biome());
		PlacementUtils.register(
			bootstapContext,
			END_GATEWAY_RETURN,
			holder2,
			RarityFilter.onAverageOnceEvery(700),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			RandomOffsetPlacement.vertical(UniformInt.of(3, 9)),
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, CHORUS_PLANT, holder3, CountPlacement.of(UniformInt.of(0, 4)), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			END_ISLAND_DECORATED,
			holder4,
			RarityFilter.onAverageOnceEvery(14),
			PlacementUtils.countExtra(1, 0.25F, 1),
			InSquarePlacement.spread(),
			HeightRangePlacement.uniform(VerticalAnchor.absolute(55), VerticalAnchor.absolute(70)),
			BiomeFilter.biome()
		);
	}
}
