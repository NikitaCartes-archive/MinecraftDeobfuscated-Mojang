package net.minecraft.server.level;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue {
	public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
	private final List<Long2ObjectLinkedOpenHashMap<List<Runnable>>> queuesPerPriority = IntStream.range(0, PRIORITY_LEVEL_COUNT)
		.mapToObj(i -> new Long2ObjectLinkedOpenHashMap())
		.toList();
	private volatile int topPriorityQueueIndex = PRIORITY_LEVEL_COUNT;
	private final String name;

	public ChunkTaskPriorityQueue(String string) {
		this.name = string;
	}

	protected void resortChunkTasks(int i, ChunkPos chunkPos, int j) {
		if (i < PRIORITY_LEVEL_COUNT) {
			Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap<List<Runnable>>)this.queuesPerPriority.get(i);
			List<Runnable> list = long2ObjectLinkedOpenHashMap.remove(chunkPos.toLong());
			if (i == this.topPriorityQueueIndex) {
				while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
					this.topPriorityQueueIndex++;
				}
			}

			if (list != null && !list.isEmpty()) {
				((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(j))
					.computeIfAbsent(chunkPos.toLong(), (Long2ObjectFunction<? extends List>)(l -> Lists.newArrayList()))
					.addAll(list);
				this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, j);
			}
		}
	}

	protected void submit(Runnable runnable, long l, int i) {
		((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(i))
			.computeIfAbsent(l, (Long2ObjectFunction<? extends List>)(lx -> Lists.newArrayList()))
			.add(runnable);
		this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, i);
	}

	protected void release(long l, boolean bl) {
		for (Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap : this.queuesPerPriority) {
			List<Runnable> list = long2ObjectLinkedOpenHashMap.get(l);
			if (list != null) {
				if (bl) {
					list.clear();
				}

				if (list.isEmpty()) {
					long2ObjectLinkedOpenHashMap.remove(l);
				}
			}
		}

		while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
			this.topPriorityQueueIndex++;
		}
	}

	@Nullable
	public ChunkTaskPriorityQueue.TasksForChunk pop() {
		if (!this.hasWork()) {
			return null;
		} else {
			int i = this.topPriorityQueueIndex;
			Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap<List<Runnable>>)this.queuesPerPriority.get(i);
			long l = long2ObjectLinkedOpenHashMap.firstLongKey();
			List<Runnable> list = long2ObjectLinkedOpenHashMap.removeFirst();

			while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
				this.topPriorityQueueIndex++;
			}

			return new ChunkTaskPriorityQueue.TasksForChunk(l, list);
		}
	}

	public boolean hasWork() {
		return this.topPriorityQueueIndex < PRIORITY_LEVEL_COUNT;
	}

	public String toString() {
		return this.name + " " + this.topPriorityQueueIndex + "...";
	}

	public static record TasksForChunk(long chunkPos, List<Runnable> tasks) {
	}
}
