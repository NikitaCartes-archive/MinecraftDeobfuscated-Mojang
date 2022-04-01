package net.minecraft.world.level.block;

import java.util.Collection;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public interface SculkBehaviour {
	SculkBehaviour DEFAULT = new SculkBehaviour() {
		@Override
		public boolean attemptSpreadVein(Level level, BlockPos blockPos, BlockState blockState, @Nullable Collection<Direction> collection) {
			if (collection == null) {
				return ((SculkVeinBlock)Blocks.SCULK_VEIN).getSameSpaceSpreader().spreadAll(level.getBlockState(blockPos), level, blockPos) > 0L;
			} else if (!collection.isEmpty()) {
				return !blockState.isAir() && !blockState.getFluidState().is(Fluids.WATER) ? false : SculkVeinBlock.regrow(level, blockPos, blockState, collection);
			} else {
				return SculkBehaviour.super.attemptSpreadVein(level, blockPos, blockState, collection);
			}
		}

		@Override
		public int attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random) {
			return chargeCursor.getDecayDelay() > 0 ? chargeCursor.getCharge() : 0;
		}

		@Override
		public int updateDecayDelay(int i) {
			return Math.max(i - 1, 0);
		}
	};

	default byte getSculkSpreadDelay() {
		return 1;
	}

	default void onDischarged(Level level, BlockState blockState, BlockPos blockPos, Random random) {
	}

	default boolean depositCharge(Level level, BlockPos blockPos, Random random) {
		return false;
	}

	default boolean attemptSpreadVein(Level level, BlockPos blockPos, BlockState blockState, @Nullable Collection<Direction> collection) {
		return ((MultifaceBlock)Blocks.SCULK_VEIN).getSpreader().spreadAll(blockState, level, blockPos) > 0L;
	}

	default boolean canChangeBlockStateOnSpread() {
		return true;
	}

	default int updateDecayDelay(int i) {
		return 1;
	}

	int attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random);
}
