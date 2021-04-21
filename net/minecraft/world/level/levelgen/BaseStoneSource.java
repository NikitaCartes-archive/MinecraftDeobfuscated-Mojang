/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BaseStoneSource {
    default public BlockState getBaseBlock(BlockPos blockPos) {
        return this.getBaseBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public BlockState getBaseBlock(int var1, int var2, int var3);
}

