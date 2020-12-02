/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NoiseColumn {
    private final int minY;
    private final BlockState[] column;

    public NoiseColumn(int i, BlockState[] blockStates) {
        this.minY = i;
        this.column = blockStates;
    }

    public BlockState getBlockState(BlockPos blockPos) {
        int i = blockPos.getY() - this.minY;
        if (i < 0 || i >= this.column.length) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.column[i];
    }
}

