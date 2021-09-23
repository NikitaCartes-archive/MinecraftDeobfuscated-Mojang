package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
		return Features.JUNGLE_TREE_NO_VINE;
	}

	@Nullable
	@Override
	protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random random) {
		return Features.MEGA_JUNGLE_TREE;
	}
}
