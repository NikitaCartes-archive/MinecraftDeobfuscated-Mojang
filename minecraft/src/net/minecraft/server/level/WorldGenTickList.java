package net.minecraft.server.level;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;

public class WorldGenTickList<T> implements TickList<T> {
	private final Function<BlockPos, TickList<T>> index;

	public WorldGenTickList(Function<BlockPos, TickList<T>> function) {
		this.index = function;
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		return ((TickList)this.index.apply(blockPos)).hasScheduledTick(blockPos, object);
	}

	@Override
	public void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
		((TickList)this.index.apply(blockPos)).scheduleTick(blockPos, object, i, tickPriority);
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
