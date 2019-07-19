package net.minecraft.world.level;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class EmptyTickList<T> implements TickList<T> {
	private static final EmptyTickList<Object> INSTANCE = new EmptyTickList<>();

	public static <T> EmptyTickList<T> empty() {
		return (EmptyTickList<T>)INSTANCE;
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
	public void addAll(Stream<TickNextTickData<T>> stream) {
	}
}
