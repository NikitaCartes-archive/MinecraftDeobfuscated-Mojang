package net.minecraft.world.level.block.grower;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class OakTreeGrower extends AbstractTreeGrower {
	@Override
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomSource, boolean bl) {
		if (randomSource.nextInt(10) == 0) {
			return bl ? TreeFeatures.FANCY_OAK_BEES_005 : TreeFeatures.FANCY_OAK;
		} else {
			return bl ? TreeFeatures.OAK_BEES_005 : TreeFeatures.OAK;
		}
	}
}
