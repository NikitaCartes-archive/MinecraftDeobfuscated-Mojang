package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class BlockPileFeature extends Feature<BlockPileConfiguration> {
	public BlockPileFeature(Codec<BlockPileConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockPileConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		Random random = featurePlaceContext.random();
		BlockPileConfiguration blockPileConfiguration = featurePlaceContext.config();
		if (blockPos.getY() < worldGenLevel.getMinBuildHeight() + 5) {
			return false;
		} else {
			int i = 2 + random.nextInt(2);
			int j = 2 + random.nextInt(2);

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-i, 0, -j), blockPos.offset(i, 1, j))) {
				int k = blockPos.getX() - blockPos2.getX();
				int l = blockPos.getZ() - blockPos2.getZ();
				if ((float)(k * k + l * l) <= random.nextFloat() * 10.0F - random.nextFloat() * 6.0F) {
					this.tryPlaceBlock(worldGenLevel, blockPos2, random, blockPileConfiguration);
				} else if ((double)random.nextFloat() < 0.031) {
					this.tryPlaceBlock(worldGenLevel, blockPos2, random, blockPileConfiguration);
				}
			}

			return true;
		}
	}

	private boolean mayPlaceOn(LevelAccessor levelAccessor, BlockPos blockPos, Random random) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = levelAccessor.getBlockState(blockPos2);
		return blockState.is(Blocks.DIRT_PATH) ? random.nextBoolean() : blockState.isFaceSturdy(levelAccessor, blockPos2, Direction.UP);
	}

	private void tryPlaceBlock(LevelAccessor levelAccessor, BlockPos blockPos, Random random, BlockPileConfiguration blockPileConfiguration) {
		if (levelAccessor.isEmptyBlock(blockPos) && this.mayPlaceOn(levelAccessor, blockPos, random)) {
			levelAccessor.setBlock(blockPos, blockPileConfiguration.stateProvider.getState(random, blockPos), 4);
		}
	}
}
