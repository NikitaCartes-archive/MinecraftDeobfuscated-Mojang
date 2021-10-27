package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public interface TickAccess<T> {
	void schedule(ScheduledTick<T> scheduledTick);

	boolean hasScheduledTick(BlockPos blockPos, T object);

	int count();
}
