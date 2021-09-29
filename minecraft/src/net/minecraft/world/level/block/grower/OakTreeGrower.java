package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class OakTreeGrower extends AbstractTreeGrower {
	@Override
	protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
		if (random.nextInt(10) == 0) {
			return bl ? Features.FANCY_OAK_BEES_005 : Features.FANCY_OAK;
		} else {
			return bl ? Features.OAK_BEES_005 : Features.OAK;
		}
	}
}
