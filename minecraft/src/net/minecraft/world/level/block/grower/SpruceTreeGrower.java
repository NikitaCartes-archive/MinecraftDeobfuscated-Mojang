package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
	@Override
	protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
		return Features.SPRUCE;
	}

	@Override
	protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random random) {
		return random.nextBoolean() ? Features.MEGA_SPRUCE : Features.MEGA_PINE;
	}
}
