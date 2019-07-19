package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.MegaJungleTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
		return new TreeFeature(
			NoneFeatureConfiguration::deserialize, true, 4 + random.nextInt(7), Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState(), false
		);
	}

	@Nullable
	@Override
	protected AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random random) {
		return new MegaJungleTreeFeature(
			NoneFeatureConfiguration::deserialize, true, 10, 20, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState()
		);
	}
}
