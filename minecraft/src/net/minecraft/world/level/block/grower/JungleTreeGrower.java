package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
		return new TreeFeature(TreeConfiguration::deserialize).configured(BiomeDefaultFeatures.JUNGLE_TREE_NOVINE_CONFIG);
	}

	@Nullable
	@Override
	protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
		return Feature.TREE.configured(BiomeDefaultFeatures.MEGA_JUNGLE_TREE_CONFIG);
	}
}
