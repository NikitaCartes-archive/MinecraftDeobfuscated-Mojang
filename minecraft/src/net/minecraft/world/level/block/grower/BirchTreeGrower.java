package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BirchTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
		return Feature.NORMAL_TREE.configured(bl ? BiomeDefaultFeatures.BIRCH_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.BIRCH_TREE_CONFIG);
	}
}
