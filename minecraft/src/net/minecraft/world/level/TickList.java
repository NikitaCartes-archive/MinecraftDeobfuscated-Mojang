package net.minecraft.world.level;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public interface TickList<T> {
	boolean hasScheduledTick(BlockPos blockPos, T object);

	default void scheduleTick(BlockPos blockPos, T object, int i) {
		this.scheduleTick(blockPos, object, i, TickPriority.NORMAL);
	}

	void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority);

	boolean willTickThisTick(BlockPos blockPos, T object);

	void addAll(Stream<TickNextTickData<T>> stream);
}
