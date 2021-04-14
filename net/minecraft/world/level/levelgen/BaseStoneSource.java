/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BaseStoneSource {
    default public BlockState getBaseStone(BlockPos blockPos) {
        return this.getBaseStone(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public BlockState getBaseStone(int var1, int var2, int var3);
}

