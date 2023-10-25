package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
	int SCAN_DISTANCE = 4;

	Optional<BlockState> getNext(BlockState blockState);

	float getChanceModifier();

	default void changeOverTime(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		float f = 0.05688889F;
		if (randomSource.nextFloat() < 0.05688889F) {
			this.getNextState(blockState, serverLevel, blockPos, randomSource).ifPresent(blockStatex -> serverLevel.setBlockAndUpdate(blockPos, blockStatex));
		}
	}

	T getAge();

	default Optional<BlockState> getNextState(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		int i = this.getAge().ordinal();
		int j = 0;
		int k = 0;

		for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, 4, 4, 4)) {
			int l = blockPos2.distManhattan(blockPos);
			if (l > 4) {
				break;
			}

			if (!blockPos2.equals(blockPos) && serverLevel.getBlockState(blockPos2).getBlock() instanceof ChangeOverTimeBlock<?> changeOverTimeBlock) {
				Enum<?> enum_ = changeOverTimeBlock.getAge();
				if (this.getAge().getClass() == enum_.getClass()) {
					int m = enum_.ordinal();
					if (m < i) {
						return Optional.empty();
					}

					if (m > i) {
						k++;
					} else {
						j++;
					}
				}
			}
		}

		float f = (float)(k + 1) / (float)(k + j + 1);
		float g = f * f * this.getChanceModifier();
		return randomSource.nextFloat() < g ? this.getNext(blockState) : Optional.empty();
	}
}
