/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public final class NoiseColumn
implements BlockColumn {
    private final int minY;
    private final BlockState[] column;

    public NoiseColumn(int i, BlockState[] blockStates) {
        this.minY = i;
        this.column = blockStates;
    }

    @Override
    public BlockState getBlock(int i) {
        int j = i - this.minY;
        if (j < 0 || j >= this.column.length) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.column[j];
    }

    @Override
    public void setBlock(int i, BlockState blockState) {
        int j = i - this.minY;
        if (j < 0 || j >= this.column.length) {
            throw new IllegalArgumentException("Outside of column height: " + i);
        }
        this.column[j] = blockState;
    }
}

