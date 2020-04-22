package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
		return null;
	}

	@Nullable
	@Override
	protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
		return Feature.TREE.configured(BiomeDefaultFeatures.DARK_OAK_TREE_CONFIG);
	}
}
