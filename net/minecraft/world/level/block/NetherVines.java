/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
    private static final double BONEMEAL_GROW_PROBABILITY_DECREASE_RATE = 0.826;
    public static final double GROW_PER_TICK_PROBABILITY = 0.1;

    public static boolean isValidGrowthState(BlockState blockState) {
        return blockState.isAir();
    }

    public static int getBlocksToGrowWhenBonemealed(Random random) {
        double d = 1.0;
        int i = 0;
        while (random.nextDouble() < d) {
            d *= 0.826;
            ++i;
        }
        return i;
    }
}

