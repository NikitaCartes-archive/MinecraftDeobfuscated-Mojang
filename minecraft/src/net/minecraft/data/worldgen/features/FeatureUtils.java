package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
	public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> bootstapContext) {
		AquaticFeatures.bootstrap(bootstapContext);
		CaveFeatures.bootstrap(bootstapContext);
		EndFeatures.bootstrap(bootstapContext);
		MiscOverworldFeatures.bootstrap(bootstapContext);
		NetherFeatures.bootstrap(bootstapContext);
		OreFeatures.bootstrap(bootstapContext);
		PileFeatures.bootstrap(bootstapContext);
		TreeFeatures.bootstrap(bootstapContext);
		VegetationFeatures.bootstrap(bootstapContext);
	}

	private static BlockPredicate simplePatchPredicate(List<Block> list) {
		BlockPredicate blockPredicate;
		if (!list.isEmpty()) {
			blockPredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), list));
		} else {
			blockPredicate = BlockPredicate.ONLY_IN_AIR_PREDICATE;
		}

		return blockPredicate;
	}

	public static RandomPatchConfiguration simpleRandomPatchConfiguration(int i, Holder<PlacedFeature> holder) {
		return new RandomPatchConfiguration(i, 7, 3, holder);
	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(
		F feature, FC featureConfiguration, List<Block> list, int i
	) {
		return simpleRandomPatchConfiguration(i, PlacementUtils.filtered(feature, featureConfiguration, simplePatchPredicate(list)));
	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(
		F feature, FC featureConfiguration, List<Block> list
	) {
		return simplePatchConfiguration(feature, featureConfiguration, list, 96);
	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F feature, FC featureConfiguration) {
		return simplePatchConfiguration(feature, featureConfiguration, List.of(), 96);
	}

	public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String string) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(string));
	}

	public static void register(
		BootstapContext<ConfiguredFeature<?, ?>> bootstapContext, ResourceKey<ConfiguredFeature<?, ?>> resourceKey, Feature<NoneFeatureConfiguration> feature
	) {
		register(bootstapContext, resourceKey, feature, FeatureConfiguration.NONE);
	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
		BootstapContext<ConfiguredFeature<?, ?>> bootstapContext, ResourceKey<ConfiguredFeature<?, ?>> resourceKey, F feature, FC featureConfiguration
	) {
		bootstapContext.register(resourceKey, new ConfiguredFeature(feature, featureConfiguration));
	}
}
