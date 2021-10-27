package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public class BlackholeTickAccess {
	private static final TickContainerAccess<Object> CONTAINER_BLACKHOLE = new TickContainerAccess<Object>() {
		@Override
		public void schedule(ScheduledTick<Object> scheduledTick) {
		}

		@Override
		public boolean hasScheduledTick(BlockPos blockPos, Object object) {
			return false;
		}

		@Override
		public int count() {
			return 0;
		}
	};
	private static final LevelTickAccess<Object> LEVEL_BLACKHOLE = new LevelTickAccess<Object>() {
		@Override
		public void schedule(ScheduledTick<Object> scheduledTick) {
		}

		@Override
		public boolean hasScheduledTick(BlockPos blockPos, Object object) {
			return false;
		}

		@Override
		public boolean willTickThisTick(BlockPos blockPos, Object object) {
			return false;
		}

		@Override
		public int count() {
			return 0;
		}
	};

	public static <T> TickContainerAccess<T> emptyContainer() {
		return (TickContainerAccess<T>)CONTAINER_BLACKHOLE;
	}

	public static <T> LevelTickAccess<T> emptyLevelList() {
		return (LevelTickAccess<T>)LEVEL_BLACKHOLE;
	}
}
