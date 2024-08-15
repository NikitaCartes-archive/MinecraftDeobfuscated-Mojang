package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public record ScheduledTick<T>(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
	public static final Comparator<ScheduledTick<?>> DRAIN_ORDER = (scheduledTick, scheduledTick2) -> {
		int i = Long.compare(scheduledTick.triggerTick, scheduledTick2.triggerTick);
		if (i != 0) {
			return i;
		} else {
			i = scheduledTick.priority.compareTo(scheduledTick2.priority);
			return i != 0 ? i : Long.compare(scheduledTick.subTickOrder, scheduledTick2.subTickOrder);
		}
	};
	public static final Comparator<ScheduledTick<?>> INTRA_TICK_DRAIN_ORDER = (scheduledTick, scheduledTick2) -> {
		int i = scheduledTick.priority.compareTo(scheduledTick2.priority);
		return i != 0 ? i : Long.compare(scheduledTick.subTickOrder, scheduledTick2.subTickOrder);
	};
	public static final Strategy<ScheduledTick<?>> UNIQUE_TICK_HASH = new Strategy<ScheduledTick<?>>() {
		public int hashCode(ScheduledTick<?> scheduledTick) {
			return 31 * scheduledTick.pos().hashCode() + scheduledTick.type().hashCode();
		}

		public boolean equals(@Nullable ScheduledTick<?> scheduledTick, @Nullable ScheduledTick<?> scheduledTick2) {
			if (scheduledTick == scheduledTick2) {
				return true;
			} else {
				return scheduledTick != null && scheduledTick2 != null
					? scheduledTick.type() == scheduledTick2.type() && scheduledTick.pos().equals(scheduledTick2.pos())
					: false;
			}
		}
	};

	public ScheduledTick(T object, BlockPos blockPos, long l, long m) {
		this(object, blockPos, l, TickPriority.NORMAL, m);
	}

	public ScheduledTick(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
		pos = pos.immutable();
		this.type = type;
		this.pos = pos;
		this.triggerTick = triggerTick;
		this.priority = priority;
		this.subTickOrder = subTickOrder;
	}

	public static <T> ScheduledTick<T> probe(T object, BlockPos blockPos) {
		return new ScheduledTick<>(object, blockPos, 0L, TickPriority.NORMAL, 0L);
	}

	public SavedTick<T> toSavedTick(long l) {
		return new SavedTick<>(this.type, this.pos, (int)(this.triggerTick - l), this.priority);
	}
}
