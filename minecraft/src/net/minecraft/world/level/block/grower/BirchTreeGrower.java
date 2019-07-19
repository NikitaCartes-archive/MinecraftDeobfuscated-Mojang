package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.BirchFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class BirchTreeGrower extends AbstractTreeGrower {
	@Nullable
	@Override
	protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
		return new BirchFeature(NoneFeatureConfiguration::deserialize, true, false);
	}
}
