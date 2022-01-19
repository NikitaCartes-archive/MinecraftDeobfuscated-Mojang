/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class ChunkTaskPriorityQueue<T> {
    public static final int PRIORITY_LEVEL_COUNT = ChunkMap.MAX_CHUNK_DISTANCE + 2;
    private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = IntStream.range(0, PRIORITY_LEVEL_COUNT).mapToObj(i -> new Long2ObjectLinkedOpenHashMap()).collect(Collectors.toList());
    private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
    private final String name;
    private final LongSet acquired = new LongOpenHashSet();
    private final int maxTasks;

    public ChunkTaskPriorityQueue(String string, int i2) {
        this.name = string;
        this.maxTasks = i2;
    }

    protected void resortChunkTasks(int i, ChunkPos chunkPos, int j) {
        if (i >= PRIORITY_LEVEL_COUNT) {
            return;
        }
        Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = this.taskQueue.get(i);
        List<Optional<T>> list = long2ObjectLinkedOpenHashMap.remove(chunkPos.toLong());
        if (i == this.firstQueue) {
            while (this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
                ++this.firstQueue;
            }
        }
        if (list != null && !list.isEmpty()) {
            this.taskQueue.get(j).computeIfAbsent(chunkPos.toLong(), l -> Lists.newArrayList()).addAll(list);
            this.firstQueue = Math.min(this.firstQueue, j);
        }
    }

    protected void submit(Optional<T> optional, long l2, int i) {
        this.taskQueue.get(i).computeIfAbsent(l2, l -> Lists.newArrayList()).add(optional);
        this.firstQueue = Math.min(this.firstQueue, i);
    }

    protected void release(long l, boolean bl) {
        for (Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap : this.taskQueue) {
            List<Optional<T>> list = long2ObjectLinkedOpenHashMap.get(l);
            if (list == null) continue;
            if (bl) {
                list.clear();
            } else {
                list.removeIf(optional -> !optional.isPresent());
            }
            if (!list.isEmpty()) continue;
            long2ObjectLinkedOpenHashMap.remove(l);
        }
        while (this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
            ++this.firstQueue;
        }
        this.acquired.remove(l);
    }

    private Runnable acquire(long l) {
        return () -> this.acquired.add(l);
    }

    @Nullable
    public Stream<Either<T, Runnable>> pop() {
        if (this.acquired.size() >= this.maxTasks) {
            return null;
        }
        if (this.hasWork()) {
            int i = this.firstQueue;
            Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = this.taskQueue.get(i);
            long l = long2ObjectLinkedOpenHashMap.firstLongKey();
            List<Optional<T>> list = long2ObjectLinkedOpenHashMap.removeFirst();
            while (this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
                ++this.firstQueue;
            }
            return list.stream().map(optional -> optional.map(Either::left).orElseGet(() -> Either.right(this.acquire(l))));
        }
        return null;
    }

    public boolean hasWork() {
        return this.firstQueue < PRIORITY_LEVEL_COUNT;
    }

    public String toString() {
        return this.name + " " + this.firstQueue + "...";
    }

    @VisibleForTesting
    LongSet getAcquired() {
        return new LongOpenHashSet(this.acquired);
    }
}

