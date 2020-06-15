/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface LevelWriter {
    public boolean setBlock(BlockPos var1, BlockState var2, int var3, int var4);

    default public boolean setBlock(BlockPos blockPos, BlockState blockState, int i) {
        return this.setBlock(blockPos, blockState, i, 512);
    }

    public boolean removeBlock(BlockPos var1, boolean var2);

    default public boolean destroyBlock(BlockPos blockPos, boolean bl) {
        return this.destroyBlock(blockPos, bl, null);
    }

    default public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity) {
        return this.destroyBlock(blockPos, bl, entity, 512);
    }

    public boolean destroyBlock(BlockPos var1, boolean var2, @Nullable Entity var3, int var4);

    default public boolean addFreshEntity(Entity entity) {
        return false;
    }
}

