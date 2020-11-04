/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public final class NoiseColumn
implements BlockGetter {
    private final BlockState[] column;

    public NoiseColumn(BlockState[] blockStates) {
        this.column = blockStates;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int i = blockPos.getY();
        if (i < 0 || i >= this.column.length) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.column[i];
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.getBlockState(blockPos).getFluidState();
    }

    @Override
    public int getSectionsCount() {
        return 16;
    }

    @Override
    public int getMinSection() {
        return 0;
    }
}

