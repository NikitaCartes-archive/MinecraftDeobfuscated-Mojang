/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.TickAccess;

public interface LevelTickAccess<T>
extends TickAccess<T> {
    public boolean willTickThisTick(BlockPos var1, T var2);
}

