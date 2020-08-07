package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
	public static boolean isValidGrowthState(BlockState blockState) {
		return blockState.isAir();
	}

	public static int getBlocksToGrowWhenBonemealed(Random random) {
		double d = 1.0;

		int i;
		for (i = 0; random.nextDouble() < d; i++) {
			d *= 0.826;
		}

		return i;
	}
}
