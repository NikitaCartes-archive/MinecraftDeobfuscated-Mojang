package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MossBlock extends Block implements BonemealableBlock {
	public MossBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return blockGetter.getBlockState(blockPos.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		place(serverLevel, random, blockPos.above());
	}

	public static boolean place(WorldGenLevel worldGenLevel, Random random, BlockPos blockPos) {
		if (!worldGenLevel.getBlockState(blockPos).isAir()) {
			return false;
		} else {
			int i = 0;
			int j = Mth.randomBetweenInclusive(random, 1, 3);
			int k = Mth.randomBetweenInclusive(random, 1, 3);

			for (int l = -j; l <= j; l++) {
				for (int m = -k; m <= k; m++) {
					BlockPos blockPos2 = blockPos.offset(l, 0, m);
					i += placeFeature(worldGenLevel, random, blockPos2);
				}
			}

			return i > 0;
		}
	}

	private static int placeFeature(WorldGenLevel worldGenLevel, Random random, BlockPos blockPos) {
		int i = 0;
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = worldGenLevel.getBlockState(blockPos2);
		if (worldGenLevel.isEmptyBlock(blockPos) && blockState.isFaceSturdy(worldGenLevel, blockPos2, Direction.UP)) {
			createMossPatch(worldGenLevel, random, blockPos.below());
			if (random.nextFloat() < 0.8F) {
				BlockState blockState2 = getVegetationBlockState(random);
				if (blockState2.canSurvive(worldGenLevel, blockPos)) {
					if (blockState2.getBlock() instanceof DoublePlantBlock && worldGenLevel.isEmptyBlock(blockPos.above())) {
						DoublePlantBlock doublePlantBlock = (DoublePlantBlock)blockState2.getBlock();
						doublePlantBlock.placeAt(worldGenLevel, blockPos, 2);
						i++;
					} else {
						worldGenLevel.setBlock(blockPos, blockState2, 2);
						i++;
					}
				}
			}
		}

		return i;
	}

	private static void createMossPatch(WorldGenLevel worldGenLevel, Random random, BlockPos blockPos) {
		if (worldGenLevel.getBlockState(blockPos).is(BlockTags.LUSH_PLANTS_REPLACEABLE)) {
			worldGenLevel.setBlock(blockPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
		}
	}

	private static BlockState getVegetationBlockState(Random random) {
		int i = random.nextInt(100) + 1;
		if (i < 5) {
			return Blocks.FLOWERING_AZALEA.defaultBlockState();
		} else if (i < 15) {
			return Blocks.AZALEA.defaultBlockState();
		} else if (i < 40) {
			return Blocks.MOSS_CARPET.defaultBlockState();
		} else {
			return i < 90 ? Blocks.GRASS.defaultBlockState() : Blocks.TALL_GRASS.defaultBlockState();
		}
	}
}
