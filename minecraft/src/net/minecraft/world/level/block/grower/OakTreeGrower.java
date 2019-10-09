package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class OakTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random) {
		return random.nextInt(10) == 0
			? Feature.FANCY_TREE.configured(BiomeDefaultFeatures.FANCY_TREE_CONFIG)
			: Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_CONFIG);
	}
}
