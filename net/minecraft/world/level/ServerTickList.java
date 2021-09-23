/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class ServerTickList<T>
implements TickList<T> {
    public static final int MAX_TICK_BLOCKS_PER_TICK = 65536;
    protected final Predicate<T> ignore;
    private final Function<T, ResourceLocation> toId;
    private final Set<TickNextTickData<T>> tickNextTickSet = Sets.newHashSet();
    private final Set<TickNextTickData<T>> tickNextTickList = Sets.newTreeSet(TickNextTickData.createTimeComparator());
    private final ServerLevel level;
    private final Queue<TickNextTickData<T>> currentlyTicking = Queues.newArrayDeque();
    private final List<TickNextTickData<T>> alreadyTicked = Lists.newArrayList();
    private final Consumer<TickNextTickData<T>> ticker;

    public ServerTickList(ServerLevel serverLevel, Predicate<T> predicate, Function<T, ResourceLocation> function, Consumer<TickNextTickData<T>> consumer) {
        this.ignore = predicate;
        this.toId = function;
        this.level = serverLevel;
        this.ticker = consumer;
    }

    public void tick() {
        TickNextTickData<T> tickNextTickData;
        int i = this.tickNextTickList.size();
        if (i != this.tickNextTickSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        if (i > 65536) {
            i = 65536;
        }
        Iterator<TickNextTickData<T>> iterator = this.tickNextTickList.iterator();
        this.level.getProfiler().push("cleaning");
        while (i > 0 && iterator.hasNext()) {
            tickNextTickData = iterator.next();
            if (tickNextTickData.triggerTick > this.level.getGameTime()) break;
            if (!this.level.isPositionTickingWithEntitiesLoaded(tickNextTickData.pos)) continue;
            iterator.remove();
            this.tickNextTickSet.remove(tickNextTickData);
            this.currentlyTicking.add(tickNextTickData);
            --i;
        }
        this.level.getProfiler().popPush("ticking");
        while ((tickNextTickData = this.currentlyTicking.poll()) != null) {
            this.alreadyTicked.add(tickNextTickData);
            try {
                this.ticker.accept(tickNextTickData);
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception while ticking");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Block being ticked");
                CrashReportCategory.populateBlockDetails(crashReportCategory, this.level, tickNextTickData.pos, null);
                throw new ReportedException(crashReport);
            }
        }
        this.level.getProfiler().pop();
        this.alreadyTicked.clear();
        this.currentlyTicking.clear();
    }

    @Override
    public boolean willTickThisTick(BlockPos blockPos, T object) {
        return this.currentlyTicking.contains(new TickNextTickData<T>(blockPos, object));
    }

    public List<TickNextTickData<T>> fetchTicksInChunk(ChunkPos chunkPos, boolean bl, boolean bl2) {
        int i = chunkPos.getMinBlockX() - 2;
        int j = i + 16 + 2;
        int k = chunkPos.getMinBlockZ() - 2;
        int l = k + 16 + 2;
        return this.fetchTicksInArea(new BoundingBox(i, this.level.getMinBuildHeight(), k, j, this.level.getMaxBuildHeight(), l), bl, bl2);
    }

    public List<TickNextTickData<T>> fetchTicksInArea(BoundingBox boundingBox, boolean bl, boolean bl2) {
        List<TickNextTickData<T>> list = this.fetchTicksInArea(null, this.tickNextTickList, boundingBox, bl);
        if (bl && list != null) {
            this.tickNextTickSet.removeAll(list);
        }
        list = this.fetchTicksInArea(list, this.currentlyTicking, boundingBox, bl);
        if (!bl2) {
            list = this.fetchTicksInArea(list, this.alreadyTicked, boundingBox, bl);
        }
        return list == null ? Collections.emptyList() : list;
    }

    @Nullable
    private List<TickNextTickData<T>> fetchTicksInArea(@Nullable List<TickNextTickData<T>> list, Collection<TickNextTickData<T>> collection, BoundingBox boundingBox, boolean bl) {
        Iterator<TickNextTickData<T>> iterator = collection.iterator();
        while (iterator.hasNext()) {
            TickNextTickData<T> tickNextTickData = iterator.next();
            BlockPos blockPos = tickNextTickData.pos;
            if (blockPos.getX() < boundingBox.minX() || blockPos.getX() >= boundingBox.maxX() || blockPos.getZ() < boundingBox.minZ() || blockPos.getZ() >= boundingBox.maxZ()) continue;
            if (bl) {
                iterator.remove();
            }
            if (list == null) {
                list = Lists.newArrayList();
            }
            list.add(tickNextTickData);
        }
        return list;
    }

    public void copy(BoundingBox boundingBox, BlockPos blockPos) {
        List<TickNextTickData<T>> list = this.fetchTicksInArea(boundingBox, false, false);
        for (TickNextTickData<T> tickNextTickData : list) {
            if (!boundingBox.isInside(tickNextTickData.pos)) continue;
            BlockPos blockPos2 = tickNextTickData.pos.offset(blockPos);
            T object = tickNextTickData.getType();
            this.addTickData(new TickNextTickData<T>(blockPos2, object, tickNextTickData.triggerTick, tickNextTickData.priority));
        }
    }

    public ListTag save(ChunkPos chunkPos) {
        List<TickNextTickData<T>> list = this.fetchTicksInChunk(chunkPos, false, true);
        return ServerTickList.saveTickList(this.toId, list, this.level.getGameTime());
    }

    private static <T> ListTag saveTickList(Function<T, ResourceLocation> function, Iterable<TickNextTickData<T>> iterable, long l) {
        ListTag listTag = new ListTag();
        for (TickNextTickData<T> tickNextTickData : iterable) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("i", function.apply(tickNextTickData.getType()).toString());
            compoundTag.putInt("x", tickNextTickData.pos.getX());
            compoundTag.putInt("y", tickNextTickData.pos.getY());
            compoundTag.putInt("z", tickNextTickData.pos.getZ());
            compoundTag.putInt("t", (int)(tickNextTickData.triggerTick - l));
            compoundTag.putInt("p", tickNextTickData.priority.getValue());
            listTag.add(compoundTag);
        }
        return listTag;
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T object) {
        return this.tickNextTickSet.contains(new TickNextTickData<T>(blockPos, object));
    }

    @Override
    public void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
        if (!this.ignore.test(object)) {
            this.addTickData(new TickNextTickData<T>(blockPos, object, (long)i + this.level.getGameTime(), tickPriority));
        }
    }

    private void addTickData(TickNextTickData<T> tickNextTickData) {
        if (!this.tickNextTickSet.contains(tickNextTickData)) {
            this.tickNextTickSet.add(tickNextTickData);
            this.tickNextTickList.add(tickNextTickData);
        }
    }

    @Override
    public int size() {
        return this.tickNextTickSet.size();
    }
}

