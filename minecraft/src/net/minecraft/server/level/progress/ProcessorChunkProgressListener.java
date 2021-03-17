package net.minecraft.server.level.progress;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

@Environment(EnvType.CLIENT)
public class ProcessorChunkProgressListener implements ChunkProgressListener {
	private final ChunkProgressListener delegate;
	private final ProcessorMailbox<Runnable> mailbox;

	private ProcessorChunkProgressListener(ChunkProgressListener chunkProgressListener, Executor executor) {
		this.delegate = chunkProgressListener;
		this.mailbox = ProcessorMailbox.create(executor, "progressListener");
	}

	public static ProcessorChunkProgressListener createStarted(ChunkProgressListener chunkProgressListener, Executor executor) {
		ProcessorChunkProgressListener processorChunkProgressListener = new ProcessorChunkProgressListener(chunkProgressListener, executor);
		processorChunkProgressListener.start();
		return processorChunkProgressListener;
	}

	@Override
	public void updateSpawnPos(ChunkPos chunkPos) {
		this.mailbox.tell(() -> this.delegate.updateSpawnPos(chunkPos));
	}

	@Override
	public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
		this.mailbox.tell(() -> this.delegate.onStatusChange(chunkPos, chunkStatus));
	}

	@Override
	public void start() {
		this.mailbox.tell(this.delegate::start);
	}

	@Override
	public void stop() {
		this.mailbox.tell(this.delegate::stop);
	}
}
