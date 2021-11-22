/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

public record ScheduledTick<T>(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    public static final Comparator<ScheduledTick<?>> DRAIN_ORDER = (scheduledTick, scheduledTick2) -> {
        int i = Long.compare(scheduledTick.triggerTick, scheduledTick2.triggerTick);
        if (i != 0) {
            return i;
        }
        i = scheduledTick.priority.compareTo(scheduledTick2.priority);
        if (i != 0) {
            return i;
        }
        return Long.compare(scheduledTick.subTickOrder, scheduledTick2.subTickOrder);
    };
    public static final Comparator<ScheduledTick<?>> INTRA_TICK_DRAIN_ORDER = (scheduledTick, scheduledTick2) -> {
        int i = scheduledTick.priority.compareTo(scheduledTick2.priority);
        if (i != 0) {
            return i;
        }
        return Long.compare(scheduledTick.subTickOrder, scheduledTick2.subTickOrder);
    };
    public static final Hash.Strategy<ScheduledTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<ScheduledTick<?>>(){

        @Override
        public int hashCode(ScheduledTick<?> scheduledTick) {
            return 31 * scheduledTick.pos().hashCode() + scheduledTick.type().hashCode();
        }

        @Override
        public boolean equals(@Nullable ScheduledTick<?> scheduledTick, @Nullable ScheduledTick<?> scheduledTick2) {
            if (scheduledTick == scheduledTick2) {
                return true;
            }
            if (scheduledTick == null || scheduledTick2 == null) {
                return false;
            }
            return scheduledTick.type() == scheduledTick2.type() && scheduledTick.pos().equals(scheduledTick2.pos());
        }

        @Override
        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((ScheduledTick)object, (ScheduledTick)object2);
        }

        @Override
        public /* synthetic */ int hashCode(Object object) {
            return this.hashCode((ScheduledTick)object);
        }
    };

    public ScheduledTick(T object, BlockPos blockPos, long l, long m) {
        this(object, blockPos, l, TickPriority.NORMAL, m);
    }

    public ScheduledTick {
        blockPos = blockPos.immutable();
    }

    public static <T> ScheduledTick<T> probe(T object, BlockPos blockPos) {
        return new ScheduledTick<T>(object, blockPos, 0L, TickPriority.NORMAL, 0L);
    }
}

