/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickPriority;

public interface TickList<T> {
    public boolean hasScheduledTick(BlockPos var1, T var2);

    default public void scheduleTick(BlockPos blockPos, T object, int i) {
        this.scheduleTick(blockPos, object, i, TickPriority.NORMAL);
    }

    public void scheduleTick(BlockPos var1, T var2, int var3, TickPriority var4);

    public boolean willTickThisTick(BlockPos var1, T var2);

    public int size();
}

