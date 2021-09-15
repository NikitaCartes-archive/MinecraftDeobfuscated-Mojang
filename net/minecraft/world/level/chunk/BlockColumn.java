/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import net.minecraft.world.level.block.state.BlockState;

public interface BlockColumn {
    public BlockState getBlock(int var1);

    public void setBlock(int var1, BlockState var2);
}

