package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveLeavesBlock extends LeavesBlock implements BonemealableBlock {
	public static final int GROWTH_CHANCE = 5;

	public MangroveLeavesBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return true;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		super.randomTick(blockState, serverLevel, blockPos, random);
		if (random.nextInt(5) == 0 && !(Boolean)blockState.getValue(PERSISTENT) && !this.decaying(blockState)) {
			BlockPos blockPos2 = blockPos.below();
			if (serverLevel.getBlockState(blockPos2).isAir()
				&& serverLevel.getBlockState(blockPos2.below()).isAir()
				&& !isTooCloseToAnotherPropagule(serverLevel, blockPos2)) {
				serverLevel.setBlockAndUpdate(blockPos2, MangrovePropaguleBlock.createNewHangingPropagule());
			}
		}
	}

	private static boolean isTooCloseToAnotherPropagule(LevelAccessor levelAccessor, BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.above().north().east(), blockPos.below().south().west())) {
			if (levelAccessor.getBlockState(blockPos2).is(Blocks.MANGROVE_PROPAGULE)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return blockGetter.getBlockState(blockPos.below()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		serverLevel.setBlock(blockPos.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
	}
}
