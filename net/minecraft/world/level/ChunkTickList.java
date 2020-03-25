/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;

public class ChunkTickList<T>
implements TickList<T> {
    private final List<ScheduledTick<T>> ticks;
    private final Function<T, ResourceLocation> toId;

    public ChunkTickList(Function<T, ResourceLocation> function, List<TickNextTickData<T>> list, long l) {
        this(function, list.stream().map(tickNextTickData -> new ScheduledTick(tickNextTickData.getType(), tickNextTickData.pos, (int)(tickNextTickData.triggerTick - l), tickNextTickData.priority)).collect(Collectors.toList()));
    }

    private ChunkTickList(Function<T, ResourceLocation> function, List<ScheduledTick<T>> list) {
        this.ticks = list;
        this.toId = function;
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T object) {
        return false;
    }

    @Override
    public void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
        this.ticks.add(new ScheduledTick(object, blockPos, i, tickPriority));
    }

    @Override
    public boolean willTickThisTick(BlockPos blockPos, T object) {
        return false;
    }

    public ListTag save() {
        ListTag listTag = new ListTag();
        for (ScheduledTick<T> scheduledTick : this.ticks) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("i", this.toId.apply(((ScheduledTick)scheduledTick).type).toString());
            compoundTag.putInt("x", scheduledTick.pos.getX());
            compoundTag.putInt("y", scheduledTick.pos.getY());
            compoundTag.putInt("z", scheduledTick.pos.getZ());
            compoundTag.putInt("t", scheduledTick.delay);
            compoundTag.putInt("p", scheduledTick.priority.getValue());
            listTag.add(compoundTag);
        }
        return listTag;
    }

    public static <T> ChunkTickList<T> create(ListTag listTag, Function<T, ResourceLocation> function, Function<ResourceLocation, T> function2) {
        ArrayList<ScheduledTick<T>> list = Lists.newArrayList();
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            T object = function2.apply(new ResourceLocation(compoundTag.getString("i")));
            if (object == null) continue;
            BlockPos blockPos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
            list.add(new ScheduledTick(object, blockPos, compoundTag.getInt("t"), TickPriority.byValue(compoundTag.getInt("p"))));
        }
        return new ChunkTickList<T>(function, list);
    }

    public void copyOut(TickList<T> tickList) {
        this.ticks.forEach(scheduledTick -> tickList.scheduleTick(scheduledTick.pos, ((ScheduledTick)scheduledTick).type, scheduledTick.delay, scheduledTick.priority));
    }

    static class ScheduledTick<T> {
        private final T type;
        public final BlockPos pos;
        public final int delay;
        public final TickPriority priority;

        private ScheduledTick(T object, BlockPos blockPos, int i, TickPriority tickPriority) {
            this.type = object;
            this.pos = blockPos;
            this.delay = i;
            this.priority = tickPriority;
        }

        public String toString() {
            return this.type + ": " + this.pos + ", " + this.delay + ", " + (Object)((Object)this.priority);
        }
    }
}

