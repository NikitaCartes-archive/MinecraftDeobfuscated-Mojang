package net.minecraft.world.level.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
	private static final double BONEMEAL_GROW_PROBABILITY_DECREASE_RATE = 0.826;
	public static final double GROW_PER_TICK_PROBABILITY = 0.1;

	public static boolean isValidGrowthState(BlockState blockState) {
		return blockState.isAir();
	}

	public static int getBlocksToGrowWhenBonemealed(RandomSource randomSource) {
		double d = 1.0;

		int i;
		for (i = 0; randomSource.nextDouble() < d; i++) {
			d *= 0.826;
		}

		return i;
	}
}
