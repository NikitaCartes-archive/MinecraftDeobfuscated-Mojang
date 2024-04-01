package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowerfulPotatoBlock;
import net.minecraft.world.level.block.StrongRootsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TwistedPotatoFeature extends Feature<NoneFeatureConfiguration> {
	public TwistedPotatoFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		if (canReplace(worldGenLevel, blockPos)) {
			worldGenLevel.setBlock(blockPos, passiveCorePotato(), 2);
			generatePlant(worldGenLevel, blockPos.below(), randomSource, 16);
			return true;
		} else {
			return false;
		}
	}

	private static BlockState passiveCorePotato() {
		return Blocks.POWERFUL_POTATO.defaultBlockState().setValue(PowerfulPotatoBlock.SPROUTS, Integer.valueOf(3));
	}

	public static void generatePlant(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, int i) {
		levelAccessor.setBlock(blockPos, StrongRootsBlock.getStateWithConnections(levelAccessor, blockPos, Blocks.WEAK_ROOTS.defaultBlockState()), 2);
		growTreeRecursive(levelAccessor, blockPos, randomSource, blockPos, i, 0);
	}

	public static boolean canReplace(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (blockState.is(Blocks.POWERFUL_POTATO)) {
			return false;
		} else {
			return blockState.is(BlockTags.FEATURES_CANNOT_REPLACE) ? false : !blockState.is(Blocks.WEAK_ROOTS);
		}
	}

	private static void growTreeRecursive(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, BlockPos blockPos2, int i, int j) {
		Block block = Blocks.WEAK_ROOTS;
		int k = randomSource.nextInt(4) + 1;
		if (j == 0) {
			k++;
		}

		for (int l = 0; l < k; l++) {
			BlockPos blockPos3 = blockPos.below(l + 1);
			levelAccessor.setBlock(blockPos3, StrongRootsBlock.getStateWithConnections(levelAccessor, blockPos3, block.defaultBlockState()), 2);
			levelAccessor.setBlock(blockPos3.above(), StrongRootsBlock.getStateWithConnections(levelAccessor, blockPos3.above(), block.defaultBlockState()), 2);
		}

		if (j < 4) {
			int l = randomSource.nextInt(4);
			if (j == 0) {
				l++;
			}

			for (int m = 0; m < l; m++) {
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
				BlockPos blockPos4 = blockPos.below(k).relative(direction);
				if (Math.abs(blockPos4.getX() - blockPos2.getX()) < i
					&& Math.abs(blockPos4.getZ() - blockPos2.getZ()) < i
					&& canReplace(levelAccessor, blockPos4)
					&& canReplace(levelAccessor, blockPos4.below())) {
					levelAccessor.setBlock(blockPos4, StrongRootsBlock.getStateWithConnections(levelAccessor, blockPos4, block.defaultBlockState()), 2);
					levelAccessor.setBlock(
						blockPos4.relative(direction.getOpposite()),
						StrongRootsBlock.getStateWithConnections(levelAccessor, blockPos4.relative(direction.getOpposite()), block.defaultBlockState()),
						2
					);
					growTreeRecursive(levelAccessor, blockPos4, randomSource, blockPos2, i, j + 1);
				}
			}
		}
	}
}
