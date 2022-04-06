package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
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
		RandomSource randomSource = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		Optional<Block> optional = Registry.BLOCK.getTag(BlockTags.CORAL_BLOCKS).flatMap(named -> named.getRandomElement(randomSource)).map(Holder::value);
		return optional.isEmpty() ? false : this.placeFeature(worldGenLevel, randomSource, blockPos, ((Block)optional.get()).defaultBlockState());
	}

	protected abstract boolean placeFeature(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState);

	protected boolean placeCoralBlock(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = levelAccessor.getBlockState(blockPos);
		if ((blockState2.is(Blocks.WATER) || blockState2.is(BlockTags.CORALS)) && levelAccessor.getBlockState(blockPos2).is(Blocks.WATER)) {
			levelAccessor.setBlock(blockPos, blockState, 3);
			if (randomSource.nextFloat() < 0.25F) {
				Registry.BLOCK
					.getTag(BlockTags.CORALS)
					.flatMap(named -> named.getRandomElement(randomSource))
					.map(Holder::value)
					.ifPresent(block -> levelAccessor.setBlock(blockPos2, block.defaultBlockState(), 2));
			} else if (randomSource.nextFloat() < 0.05F) {
				levelAccessor.setBlock(blockPos2, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(randomSource.nextInt(4) + 1)), 2);
			}

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (randomSource.nextFloat() < 0.2F) {
					BlockPos blockPos3 = blockPos.relative(direction);
					if (levelAccessor.getBlockState(blockPos3).is(Blocks.WATER)) {
						Registry.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap(named -> named.getRandomElement(randomSource)).map(Holder::value).ifPresent(block -> {
							BlockState blockStatex = block.defaultBlockState();
							if (blockStatex.hasProperty(BaseCoralWallFanBlock.FACING)) {
								blockStatex = blockStatex.setValue(BaseCoralWallFanBlock.FACING, direction);
							}

							levelAccessor.setBlock(blockPos3, blockStatex, 2);
						});
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}
}
