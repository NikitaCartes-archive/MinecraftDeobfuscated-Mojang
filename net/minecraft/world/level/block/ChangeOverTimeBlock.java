/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock {
    default public int getChangeInterval(Random random) {
        return 1200000 + random.nextInt(768000);
    }

    public BlockState getChangeTo(BlockState var1);

    default public void scheduleChange(Level level, Block block, BlockPos blockPos) {
        level.getBlockTicks().scheduleTick(blockPos, block, this.getChangeInterval(level.getRandom()));
    }

    default public void change(Level level, BlockState blockState, BlockPos blockPos) {
        level.setBlockAndUpdate(blockPos, this.getChangeTo(blockState));
    }
}

