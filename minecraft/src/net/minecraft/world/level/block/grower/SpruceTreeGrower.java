package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
	@Override
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean bl) {
		return TreeFeatures.SPRUCE;
	}

	@Override
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(Random random) {
		return random.nextBoolean() ? TreeFeatures.MEGA_SPRUCE : TreeFeatures.MEGA_PINE;
	}
}
