package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

public class BuddingAmethystBlock extends AmethystBlock {
	public static final int GROWTH_CHANCE = 5;
	private static final Direction[] DIRECTIONS = Direction.values();

	public BuddingAmethystBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(5) == 0) {
			Direction direction = DIRECTIONS[randomSource.nextInt(DIRECTIONS.length)];
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState2 = serverLevel.getBlockState(blockPos2);
			Block block = null;
			if (canClusterGrowAtState(blockState2)) {
				block = Blocks.SMALL_AMETHYST_BUD;
			} else if (blockState2.is(Blocks.SMALL_AMETHYST_BUD) && blockState2.getValue(AmethystClusterBlock.FACING) == direction) {
				block = Blocks.MEDIUM_AMETHYST_BUD;
			} else if (blockState2.is(Blocks.MEDIUM_AMETHYST_BUD) && blockState2.getValue(AmethystClusterBlock.FACING) == direction) {
				block = Blocks.LARGE_AMETHYST_BUD;
			} else if (blockState2.is(Blocks.LARGE_AMETHYST_BUD) && blockState2.getValue(AmethystClusterBlock.FACING) == direction) {
				block = Blocks.AMETHYST_CLUSTER;
			}

			if (block != null) {
				BlockState blockState3 = block.defaultBlockState()
					.setValue(AmethystClusterBlock.FACING, direction)
					.setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockState2.getFluidState().getType() == Fluids.WATER));
				serverLevel.setBlockAndUpdate(blockPos2, blockState3);
			}
		}
	}

	public static boolean canClusterGrowAtState(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER) && blockState.getFluidState().getAmount() == 8;
	}
}
