package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
	@Override
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean bl) {
		return TreeFeatures.JUNGLE_TREE_NO_VINE;
	}

	@Override
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(Random random) {
		return TreeFeatures.MEGA_JUNGLE_TREE;
	}
}
