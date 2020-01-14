package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class AcaciaTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
		return Feature.ACACIA_TREE.configured(BiomeDefaultFeatures.ACACIA_TREE_CONFIG);
	}
}
