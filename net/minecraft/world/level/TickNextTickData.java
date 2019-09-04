/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickPriority;

public class TickNextTickData<T> {
    private static long counter;
    private final T type;
    public final BlockPos pos;
    public final long delay;
    public final TickPriority priority;
    private final long c = counter++;

    public TickNextTickData(BlockPos blockPos, T object) {
        this(blockPos, object, 0L, TickPriority.NORMAL);
    }

    public TickNextTickData(BlockPos blockPos, T object, long l, TickPriority tickPriority) {
        this.pos = blockPos.immutable();
        this.type = object;
        this.delay = l;
        this.priority = tickPriority;
    }

    public boolean equals(Object object) {
        if (object instanceof TickNextTickData) {
            TickNextTickData tickNextTickData = (TickNextTickData)object;
            return this.pos.equals(tickNextTickData.pos) && this.type == tickNextTickData.type;
        }
        return false;
    }

    public int hashCode() {
        return this.pos.hashCode();
    }

    public static <T> Comparator<TickNextTickData<T>> createTimeComparator() {
        return Comparator.comparingLong(tickNextTickData -> tickNextTickData.delay).thenComparing(tickNextTickData -> tickNextTickData.priority).thenComparingLong(tickNextTickData -> tickNextTickData.c);
    }

    public String toString() {
        return this.type + ": " + this.pos + ", " + this.delay + ", " + (Object)((Object)this.priority) + ", " + this.c;
    }

    public T getType() {
        return this.type;
    }
}

