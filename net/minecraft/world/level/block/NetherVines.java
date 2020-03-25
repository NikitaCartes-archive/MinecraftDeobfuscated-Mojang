/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines {
    public static boolean isValidGrowthState(BlockState blockState) {
        return blockState.isAir();
    }

    public static int getBlocksToGrowWhenBonemealed(Random random) {
        double d = 1.0;
        int i = 0;
        while (random.nextDouble() < d) {
            d *= 0.94;
            ++i;
        }
        return i;
    }
}

