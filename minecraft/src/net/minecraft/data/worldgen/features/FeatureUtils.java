package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
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
	public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
		AquaticFeatures.bootstrap(bootstrapContext);
		CaveFeatures.bootstrap(bootstrapContext);
		EndFeatures.bootstrap(bootstrapContext);
		MiscOverworldFeatures.bootstrap(bootstrapContext);
		NetherFeatures.bootstrap(bootstrapContext);
		OreFeatures.bootstrap(bootstrapContext);
		PileFeatures.bootstrap(bootstrapContext);
		TreeFeatures.bootstrap(bootstrapContext);
		VegetationFeatures.bootstrap(bootstrapContext);
	}

	private static BlockPredicate simplePatchPredicate(List<Block> list) {
		BlockPredicate blockPredicate;
		if (!list.isEmpty()) {
			blockPredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getUnitVec3i(), list));
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
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.withDefaultNamespace(string));
	}

	public static void register(
		BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext, ResourceKey<ConfiguredFeature<?, ?>> resourceKey, Feature<NoneFeatureConfiguration> feature
	) {
		register(bootstrapContext, resourceKey, feature, FeatureConfiguration.NONE);
	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
		BootstrapContext<ConfiguredFeature<?, ?>> bootstrapContext, ResourceKey<ConfiguredFeature<?, ?>> resourceKey, F feature, FC featureConfiguration
	) {
		bootstrapContext.register(resourceKey, new ConfiguredFeature(feature, featureConfiguration));
	}
}
