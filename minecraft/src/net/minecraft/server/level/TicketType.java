package net.minecraft.server.level;

import java.util.Comparator;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;

public class TicketType<T> {
	private final String name;
	private final Comparator<T> comparator;
	private final long timeout;
	public static final TicketType<Unit> START = create("start", (unit, unit2) -> 0);
	public static final TicketType<Unit> DRAGON = create("dragon", (unit, unit2) -> 0);
	public static final TicketType<ChunkPos> PLAYER = create("player", Comparator.comparingLong(ChunkPos::toLong));
	public static final TicketType<ChunkPos> FORCED = create("forced", Comparator.comparingLong(ChunkPos::toLong));
	public static final TicketType<ChunkPos> LIGHT = create("light", Comparator.comparingLong(ChunkPos::toLong));
	public static final TicketType<ColumnPos> PORTAL = create("portal", Comparator.comparingLong(ColumnPos::toLong));
	public static final TicketType<Integer> POST_TELEPORT = create("post_teleport", Integer::compareTo, 5);
	public static final TicketType<ChunkPos> UNKNOWN = create("unknown", Comparator.comparingLong(ChunkPos::toLong), 1);

	public static <T> TicketType<T> create(String string, Comparator<T> comparator) {
		return new TicketType<>(string, comparator, 0L);
	}

	public static <T> TicketType<T> create(String string, Comparator<T> comparator, int i) {
		return new TicketType<>(string, comparator, (long)i);
	}

	protected TicketType(String string, Comparator<T> comparator, long l) {
		this.name = string;
		this.comparator = comparator;
		this.timeout = l;
	}

	public String toString() {
		return this.name;
	}

	public Comparator<T> getComparator() {
		return this.comparator;
	}

	public long timeout() {
		return this.timeout;
	}
}
