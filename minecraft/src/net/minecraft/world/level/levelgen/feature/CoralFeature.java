package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
	public CoralFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		Random random = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		BlockState blockState = BlockTags.CORAL_BLOCKS.getRandomElement(random).defaultBlockState();
		return this.placeFeature(worldGenLevel, random, blockPos, blockState);
	}

	protected abstract boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState);

	protected boolean placeCoralBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = levelAccessor.getBlockState(blockPos);
		if ((blockState2.is(Blocks.WATER) || blockState2.is(BlockTags.CORALS)) && levelAccessor.getBlockState(blockPos2).is(Blocks.WATER)) {
			levelAccessor.setBlock(blockPos, blockState, 3);
			if (random.nextFloat() < 0.25F) {
				levelAccessor.setBlock(blockPos2, BlockTags.CORALS.getRandomElement(random).defaultBlockState(), 2);
			} else if (random.nextFloat() < 0.05F) {
				levelAccessor.setBlock(blockPos2, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(random.nextInt(4) + 1)), 2);
			}

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (random.nextFloat() < 0.2F) {
					BlockPos blockPos3 = blockPos.relative(direction);
					if (levelAccessor.getBlockState(blockPos3).is(Blocks.WATER)) {
						BlockState blockState3 = BlockTags.WALL_CORALS.getRandomElement(random).defaultBlockState();
						if (blockState3.hasProperty(BaseCoralWallFanBlock.FACING)) {
							blockState3 = blockState3.setValue(BaseCoralWallFanBlock.FACING, direction);
						}

						levelAccessor.setBlock(blockPos3, blockState3, 2);
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}
}
