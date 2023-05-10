package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue<T> {
	public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
	private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = (List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>>)IntStream.range(
			0, PRIORITY_LEVEL_COUNT
		)
		.mapToObj(ix -> new Long2ObjectLinkedOpenHashMap())
		.collect(Collectors.toList());
	private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
	private final String name;
	private final LongSet acquired = new LongOpenHashSet();
	private final int maxTasks;

	public ChunkTaskPriorityQueue(String string, int i) {
		this.name = string;
		this.maxTasks = i;
	}

	protected void resortChunkTasks(int i, ChunkPos chunkPos, int j) {
		if (i < PRIORITY_LEVEL_COUNT) {
			Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap<List<Optional<T>>>)this.taskQueue.get(i);
			List<Optional<T>> list = long2ObjectLinkedOpenHashMap.remove(chunkPos.toLong());
			if (i == this.firstQueue) {
				while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(this.firstQueue)).isEmpty()) {
					this.firstQueue++;
				}
			}

			if (list != null && !list.isEmpty()) {
				((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(j))
					.computeIfAbsent(chunkPos.toLong(), (Long2ObjectFunction<? extends List>)(l -> Lists.newArrayList()))
					.addAll(list);
				this.firstQueue = Math.min(this.firstQueue, j);
			}
		}
	}

	protected void submit(Optional<T> optional, long l, int i) {
		((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(i)).computeIfAbsent(l, (Long2ObjectFunction<? extends List>)(lx -> Lists.newArrayList())).add(optional);
		this.firstQueue = Math.min(this.firstQueue, i);
	}

	protected void release(long l, boolean bl) {
		for (Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap : this.taskQueue) {
			List<Optional<T>> list = long2ObjectLinkedOpenHashMap.get(l);
			if (list != null) {
				if (bl) {
					list.clear();
				} else {
					list.removeIf(optional -> !optional.isPresent());
				}

				if (list.isEmpty()) {
					long2ObjectLinkedOpenHashMap.remove(l);
				}
			}
		}

		while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(this.firstQueue)).isEmpty()) {
			this.firstQueue++;
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
		} else if (!this.hasWork()) {
			return null;
		} else {
			int i = this.firstQueue;
			Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap<List<Optional<T>>>)this.taskQueue.get(i);
			long l = long2ObjectLinkedOpenHashMap.firstLongKey();
			List<Optional<T>> list = long2ObjectLinkedOpenHashMap.removeFirst();

			while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(this.firstQueue)).isEmpty()) {
				this.firstQueue++;
			}

			return list.stream().map(optional -> (Either)optional.map(Either::left).orElseGet(() -> Either.right(this.acquire(l))));
		}
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
