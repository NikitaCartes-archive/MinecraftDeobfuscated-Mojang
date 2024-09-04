package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class ThrottlingChunkTaskDispatcher extends ChunkTaskDispatcher {
	private final LongSet chunkPositionsInExecution = new LongOpenHashSet();
	private final int maxChunksInExecution;
	private final String executorSchedulerName;

	public ThrottlingChunkTaskDispatcher(TaskScheduler<Runnable> taskScheduler, Executor executor, int i) {
		super(taskScheduler, executor);
		this.maxChunksInExecution = i;
		this.executorSchedulerName = taskScheduler.name();
	}

	@Override
	protected void onRelease(long l) {
		this.chunkPositionsInExecution.remove(l);
	}

	@Nullable
	@Override
	protected ChunkTaskPriorityQueue.TasksForChunk popTasks() {
		return this.chunkPositionsInExecution.size() < this.maxChunksInExecution ? super.popTasks() : null;
	}

	@Override
	protected void scheduleForExecution(ChunkTaskPriorityQueue.TasksForChunk tasksForChunk) {
		this.chunkPositionsInExecution.add(tasksForChunk.chunkPos());
		super.scheduleForExecution(tasksForChunk);
	}

	@VisibleForTesting
	public String getDebugStatus() {
		return this.executorSchedulerName
			+ "=["
			+ (String)this.chunkPositionsInExecution.stream().map(long_ -> long_ + ":" + new ChunkPos(long_)).collect(Collectors.joining(","))
			+ "], s="
			+ this.sleeping;
	}
}
