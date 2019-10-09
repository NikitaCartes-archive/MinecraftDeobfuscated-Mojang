package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class BirchTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random) {
		return Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_CONFIG);
	}
}
