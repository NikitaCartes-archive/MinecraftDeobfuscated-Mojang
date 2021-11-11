package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
	public static ConfiguredFeature<?, ?> bootstrap() {
		ConfiguredFeature<?, ?>[] configuredFeatures = new ConfiguredFeature[]{
			AquaticFeatures.KELP,
			CaveFeatures.MOSS_PATCH_BONEMEAL,
			EndFeatures.CHORUS_PLANT,
			MiscOverworldFeatures.SPRING_LAVA_OVERWORLD,
			NetherFeatures.BASALT_BLOBS,
			OreFeatures.ORE_ANCIENT_DEBRIS_LARGE,
			PileFeatures.PILE_HAY,
			TreeFeatures.AZALEA_TREE,
			VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA
		};
		return Util.getRandom(configuredFeatures, new Random());
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

	public static RandomPatchConfiguration simpleRandomPatchConfiguration(int i, PlacedFeature placedFeature) {
		return new RandomPatchConfiguration(i, 7, 3, () -> placedFeature);
	}

	public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> configuredFeature, List<Block> list, int i) {
		return simpleRandomPatchConfiguration(i, configuredFeature.filtered(simplePatchPredicate(list)));
	}

	public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> configuredFeature, List<Block> list) {
		return simplePatchConfiguration(configuredFeature, list, 96);
	}

	public static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> configuredFeature) {
		return simplePatchConfiguration(configuredFeature, List.of(), 96);
	}

	public static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> register(String string, ConfiguredFeature<FC, ?> configuredFeature) {
		return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, string, configuredFeature);
	}
}
