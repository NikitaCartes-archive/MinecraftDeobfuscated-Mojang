package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.DarkOakFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
		return null;
	}

	@Nullable
	@Override
	protected AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random random) {
		return new DarkOakFeature(NoneFeatureConfiguration::deserialize, true);
	}
}
