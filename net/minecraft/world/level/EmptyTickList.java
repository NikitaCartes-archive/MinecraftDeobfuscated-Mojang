/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;

public class EmptyTickList<T>
implements TickList<T> {
    private static final EmptyTickList<Object> INSTANCE = new EmptyTickList();

    public static <T> EmptyTickList<T> empty() {
        return INSTANCE;
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T object) {
        return false;
    }

    @Override
    public void scheduleTick(BlockPos blockPos, T object, int i) {
    }

    @Override
    public void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
    }

    @Override
    public boolean willTickThisTick(BlockPos blockPos, T object) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}

