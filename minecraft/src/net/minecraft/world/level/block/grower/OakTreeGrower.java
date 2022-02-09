package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class OakTreeGrower extends AbstractTreeGrower {
	@Override
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean bl) {
		if (random.nextInt(10) == 0) {
			return bl ? TreeFeatures.FANCY_OAK_BEES_005 : TreeFeatures.FANCY_OAK;
		} else {
			return bl ? TreeFeatures.OAK_BEES_005 : TreeFeatures.OAK;
		}
	}
}
