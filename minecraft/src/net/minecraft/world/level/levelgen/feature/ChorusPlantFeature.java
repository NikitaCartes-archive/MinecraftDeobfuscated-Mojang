package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ChorusPlantFeature extends Feature<NoneFeatureConfiguration> {
	public ChorusPlantFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		Random random = featurePlaceContext.random();
		if (worldGenLevel.isEmptyBlock(blockPos) && worldGenLevel.getBlockState(blockPos.below()).is(Blocks.END_STONE)) {
			ChorusFlowerBlock.generatePlant(worldGenLevel, blockPos, random, 8);
			return true;
		} else {
			return false;
		}
	}
}
