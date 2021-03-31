package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
	int SCAN_DISTANCE = 4;

	Optional<BlockState> getNext(BlockState blockState);

	float getChanceModifier();

	default void onRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		float f = 0.05688889F;
		if (random.nextFloat() < 0.05688889F) {
			this.applyChangeOverTime(blockState, serverLevel, blockPos, random);
		}
	}

	T getAge();

	default void applyChangeOverTime(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		int i = this.getAge().ordinal();
		int j = 0;
		int k = 0;

		for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, 4, 4, 4)) {
			int l = blockPos2.distManhattan(blockPos);
			if (l > 4) {
				break;
			}

			if (!blockPos2.equals(blockPos)) {
				BlockState blockState2 = serverLevel.getBlockState(blockPos2);
				Block block = blockState2.getBlock();
				if (block instanceof ChangeOverTimeBlock) {
					Enum<?> enum_ = ((ChangeOverTimeBlock)block).getAge();
					if (this.getAge().getClass() == enum_.getClass()) {
						int m = enum_.ordinal();
						if (m < i) {
							return;
						}

						if (m > i) {
							k++;
						} else {
							j++;
						}
					}
				}
			}
		}

		float f = (float)(k + 1) / (float)(k + j + 1);
		float g = f * f * this.getChanceModifier();
		if (random.nextFloat() < g) {
			this.getNext(blockState).ifPresent(blockStatex -> serverLevel.setBlockAndUpdate(blockPos, blockStatex));
		}
	}
}
