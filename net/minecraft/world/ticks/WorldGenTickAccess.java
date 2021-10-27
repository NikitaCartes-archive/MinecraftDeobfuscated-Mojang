/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickContainerAccess;

public class WorldGenTickAccess<T>
implements LevelTickAccess<T> {
    private final Function<BlockPos, TickContainerAccess<T>> containerGetter;

    public WorldGenTickAccess(Function<BlockPos, TickContainerAccess<T>> function) {
        this.containerGetter = function;
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T object) {
        return this.containerGetter.apply(blockPos).hasScheduledTick(blockPos, object);
    }

    @Override
    public void schedule(ScheduledTick<T> scheduledTick) {
        this.containerGetter.apply(scheduledTick.pos()).schedule(scheduledTick);
    }

    @Override
    public boolean willTickThisTick(BlockPos blockPos, T object) {
        return false;
    }

    @Override
    public int count() {
        return 0;
    }
}

