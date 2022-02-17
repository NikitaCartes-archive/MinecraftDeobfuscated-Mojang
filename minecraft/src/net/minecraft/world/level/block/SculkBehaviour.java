package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface SculkBehaviour {
	SculkBehaviour DEFAULT = new SculkBehaviour() {
		@Override
		public boolean attemptSpreadVein(Level level, BlockPos blockPos, BlockState blockState, byte b) {
			if (b == -1) {
				return ((SculkVeinBlock)Blocks.SCULK_VEIN).sameSpaceSpreader.spreadAll(level.getBlockState(blockPos), level, blockPos) > 0L;
			} else {
				return b == 0 || !blockState.isAir() && (!blockState.is(Blocks.WATER) || !blockState.getFluidState().isSource())
					? SculkBehaviour.super.attemptSpreadVein(level, blockPos, blockState, b)
					: ((SculkVeinBlock)Blocks.SCULK_VEIN).regrow(level, blockPos, blockState, b);
			}
		}

		@Override
		public short attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random) {
			return chargeCursor.getDecayDelay() > 0 ? chargeCursor.getCharge() : 0;
		}

		@Override
		public byte updateDecayDelay(byte b) {
			return (byte)Math.max(b - 1, 0);
		}
	};

	default byte getSculkSpreadDelay() {
		return 2;
	}

	default void onDischarged(Level level, BlockState blockState, BlockPos blockPos, Random random) {
	}

	default boolean depositCharge(Level level, BlockPos blockPos, Random random) {
		return false;
	}

	default boolean attemptSpreadVein(Level level, BlockPos blockPos, BlockState blockState, byte b) {
		return ((SculkVeinBlock)Blocks.SCULK_VEIN).veinSpreader.spreadAll(blockState, level, blockPos) > 0L;
	}

	default boolean canChangeBlockStateOnSpread() {
		return true;
	}

	default byte updateDecayDelay(byte b) {
		return 2;
	}

	short attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random);
}
