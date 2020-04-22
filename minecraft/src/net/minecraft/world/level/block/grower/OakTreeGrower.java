package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class OakTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
		return random.nextInt(10) == 0
			? Feature.TREE.configured(bl ? BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.FANCY_TREE_CONFIG)
			: Feature.TREE.configured(bl ? BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.NORMAL_TREE_CONFIG);
	}
}
