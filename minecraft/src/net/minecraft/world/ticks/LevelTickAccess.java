package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public interface LevelTickAccess<T> extends TickAccess<T> {
	boolean willTickThisTick(BlockPos blockPos, T object);
}
