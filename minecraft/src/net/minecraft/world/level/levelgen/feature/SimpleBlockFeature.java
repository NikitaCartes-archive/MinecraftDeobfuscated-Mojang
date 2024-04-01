package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
	public SimpleBlockFeature(Codec<SimpleBlockConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> featurePlaceContext) {
		SimpleBlockConfiguration simpleBlockConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		BlockState blockState = simpleBlockConfiguration.toPlace().getState(featurePlaceContext.random(), blockPos);
		if (blockState.is(Blocks.POTATOES)) {
			blockState = PotatoBlock.withCorrectTaterBoost(blockState, worldGenLevel.getBlockState(blockPos.below()));
		}

		return blockState.canSurvive(worldGenLevel, blockPos) ? place(blockState, worldGenLevel, blockPos) : false;
	}

	public static boolean place(BlockState blockState, WorldGenLevel worldGenLevel, BlockPos blockPos) {
		if (blockState.getBlock() instanceof DoublePlantBlock) {
			if (!worldGenLevel.isEmptyBlock(blockPos.above())) {
				return false;
			}

			DoublePlantBlock.placeAt(worldGenLevel, blockState, blockPos, 2);
		} else {
			worldGenLevel.setBlock(blockPos, blockState, 2);
		}

		return true;
	}
}
