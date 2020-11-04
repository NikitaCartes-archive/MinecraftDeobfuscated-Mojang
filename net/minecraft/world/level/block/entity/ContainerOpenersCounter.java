/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
    private int openCount;

    protected abstract void onOpen(Level var1, BlockPos var2, BlockState var3);

    protected abstract void onClose(Level var1, BlockPos var2, BlockState var3);

    protected abstract void openerCountChanged(Level var1, BlockPos var2, BlockState var3, int var4, int var5);

    protected abstract boolean isOwnContainer(Player var1);

    public void incrementOpeners(Level level, BlockPos blockPos, BlockState blockState) {
        int i;
        if ((i = this.openCount++) == 0) {
            this.onOpen(level, blockPos, blockState);
            ContainerOpenersCounter.scheduleRecheck(level, blockPos, blockState);
        }
        this.openerCountChanged(level, blockPos, blockState, i, this.openCount);
    }

    public void decrementOpeners(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(level, blockPos, blockState);
        }
        this.openerCountChanged(level, blockPos, blockState, i, this.openCount);
    }

    private int getOpenCount(Level level, BlockPos blockPos) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        float f = 5.0f;
        AABB aABB = new AABB((float)i - 5.0f, (float)j - 5.0f, (float)k - 5.0f, (float)(i + 1) + 5.0f, (float)(j + 1) + 5.0f, (float)(k + 1) + 5.0f);
        return level.getEntities(EntityTypeTest.forClass(Player.class), aABB, this::isOwnContainer).size();
    }

    public void recheckOpeners(Level level, BlockPos blockPos, BlockState blockState) {
        int j = this.openCount;
        int i = this.getOpenCount(level, blockPos);
        if (j != i) {
            boolean bl2;
            boolean bl = i != 0;
            boolean bl3 = bl2 = j != 0;
            if (bl && !bl2) {
                this.onOpen(level, blockPos, blockState);
            } else if (!bl) {
                this.onClose(level, blockPos, blockState);
            }
            this.openCount = i;
        }
        this.openerCountChanged(level, blockPos, blockState, j, i);
        if (i > 0) {
            ContainerOpenersCounter.scheduleRecheck(level, blockPos, blockState);
        }
    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(Level level, BlockPos blockPos, BlockState blockState) {
        level.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 5);
    }
}

