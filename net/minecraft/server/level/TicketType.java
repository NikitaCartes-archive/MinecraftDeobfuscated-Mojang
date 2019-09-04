/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;

public class TicketType<T> {
    private final String name;
    private final Comparator<T> comparator;
    private final long timeout;
    public static final TicketType<Unit> START = TicketType.create("start", (unit, unit2) -> 0);
    public static final TicketType<Unit> DRAGON = TicketType.create("dragon", (unit, unit2) -> 0);
    public static final TicketType<ChunkPos> PLAYER = TicketType.create("player", Comparator.comparingLong(ChunkPos::toLong));
    public static final TicketType<ChunkPos> FORCED = TicketType.create("forced", Comparator.comparingLong(ChunkPos::toLong));
    public static final TicketType<ChunkPos> LIGHT = TicketType.create("light", Comparator.comparingLong(ChunkPos::toLong));
    public static final TicketType<BlockPos> PORTAL = TicketType.create("portal", Vec3i::compareTo, 300);
    public static final TicketType<Integer> POST_TELEPORT = TicketType.create("post_teleport", Integer::compareTo, 5);
    public static final TicketType<ChunkPos> UNKNOWN = TicketType.create("unknown", Comparator.comparingLong(ChunkPos::toLong), 1);

    public static <T> TicketType<T> create(String string, Comparator<T> comparator) {
        return new TicketType<T>(string, comparator, 0L);
    }

    public static <T> TicketType<T> create(String string, Comparator<T> comparator, int i) {
        return new TicketType<T>(string, comparator, i);
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

