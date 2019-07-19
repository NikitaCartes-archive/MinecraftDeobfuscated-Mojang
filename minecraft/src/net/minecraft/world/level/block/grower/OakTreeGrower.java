package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.BigTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public class OakTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
		return (AbstractTreeFeature<NoneFeatureConfiguration>)(random.nextInt(10) == 0
			? new BigTreeFeature(NoneFeatureConfiguration::deserialize, true)
			: new TreeFeature(NoneFeatureConfiguration::deserialize, true));
	}
}
