/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
    public boolean setBlock(BlockPos var1, BlockState var2, int var3);

    public boolean removeBlock(BlockPos var1, boolean var2);

    public boolean destroyBlock(BlockPos var1, boolean var2);

    default public boolean addFreshEntity(Entity entity) {
        return false;
    }
}

