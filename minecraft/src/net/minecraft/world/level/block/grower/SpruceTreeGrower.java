package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
		return Features.SPRUCE;
	}

	@Nullable
	@Override
	protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random random) {
		return random.nextBoolean() ? Features.MEGA_SPRUCE : Features.MEGA_PINE;
	}
}
