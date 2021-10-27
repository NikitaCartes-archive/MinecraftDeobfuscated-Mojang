/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.ScheduledTick;

public interface TickAccess<T> {
    public void schedule(ScheduledTick<T> var1);

    public boolean hasScheduledTick(BlockPos var1, T var2);

    public int count();
}

