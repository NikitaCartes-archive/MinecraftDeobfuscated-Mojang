package net.minecraft.world.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;

public class TickNextTickData<T> {
	private static long counter;
	private final T type;
	public final BlockPos pos;
	public final long delay;
	public final TickPriority priority;
	private final long c;

	public TickNextTickData(BlockPos blockPos, T object) {
		this(blockPos, object, 0L, TickPriority.NORMAL);
	}

	public TickNextTickData(BlockPos blockPos, T object, long l, TickPriority tickPriority) {
		this.c = counter++;
		this.pos = blockPos.immutable();
		this.type = object;
		this.delay = l;
		this.priority = tickPriority;
	}

	public boolean equals(Object object) {
		if (!(object instanceof TickNextTickData)) {
			return false;
		} else {
			TickNextTickData<?> tickNextTickData = (TickNextTickData<?>)object;
			return this.pos.equals(tickNextTickData.pos) && this.type == tickNextTickData.type;
		}
	}

	public int hashCode() {
		return this.pos.hashCode();
	}

	public static <T> Comparator<TickNextTickData<T>> createTimeComparator() {
		return Comparator.comparingLong(tickNextTickData -> tickNextTickData.delay)
			.thenComparing(tickNextTickData -> tickNextTickData.priority)
			.thenComparingLong(tickNextTickData -> tickNextTickData.c);
	}

	public String toString() {
		return this.type + ": " + this.pos + ", " + this.delay + ", " + this.priority + ", " + this.c;
	}

	public T getType() {
		return this.type;
	}
}
