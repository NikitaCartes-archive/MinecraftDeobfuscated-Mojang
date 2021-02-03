package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
	public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		ReplaceBlockConfiguration replaceBlockConfiguration = featurePlaceContext.config();
		if (worldGenLevel.getBlockState(blockPos).is(replaceBlockConfiguration.target.getBlock())) {
			worldGenLevel.setBlock(blockPos, replaceBlockConfiguration.state, 2);
		}

		return true;
	}
}
