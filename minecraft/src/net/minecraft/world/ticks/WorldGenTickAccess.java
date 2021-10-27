package net.minecraft.world.ticks;

import java.util.function.Function;
import net.minecraft.core.BlockPos;

public class WorldGenTickAccess<T> implements LevelTickAccess<T> {
	private final Function<BlockPos, TickContainerAccess<T>> containerGetter;

	public WorldGenTickAccess(Function<BlockPos, TickContainerAccess<T>> function) {
		this.containerGetter = function;
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		return ((TickContainerAccess)this.containerGetter.apply(blockPos)).hasScheduledTick(blockPos, object);
	}

	@Override
	public void schedule(ScheduledTick<T> scheduledTick) {
		((TickContainerAccess)this.containerGetter.apply(scheduledTick.pos())).schedule(scheduledTick);
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
