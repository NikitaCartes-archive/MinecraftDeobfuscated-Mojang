package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
	public static Holder<? extends ConfiguredFeature<?, ?>> bootstrap() {
		List<Holder<? extends ConfiguredFeature<?, ?>>> list = List.of(
			AquaticFeatures.KELP,
			CaveFeatures.MOSS_PATCH_BONEMEAL,
			EndFeatures.CHORUS_PLANT,
			MiscOverworldFeatures.SPRING_LAVA_OVERWORLD,
			NetherFeatures.BASALT_BLOBS,
			OreFeatures.ORE_ANCIENT_DEBRIS_LARGE,
			PileFeatures.PILE_HAY,
			TreeFeatures.AZALEA_TREE,
			VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA
		);
		return Util.getRandom(list, new Random());
	}

	private static BlockPredicate simplePatchPredicate(List<Block> list) {
		BlockPredicate blockPredicate;
		if (!list.isEmpty()) {
			blockPredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(list, new BlockPos(0, -1, 0)));
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

	public static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> register(String string, Feature<NoneFeatureConfiguration> feature) {
		return register(string, feature, FeatureConfiguration.NONE);
	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<ConfiguredFeature<FC, ?>> register(
		String string, F feature, FC featureConfiguration
	) {
		return BuiltinRegistries.registerExact(BuiltinRegistries.CONFIGURED_FEATURE, string, new ConfiguredFeature<>(feature, featureConfiguration));
	}
}
