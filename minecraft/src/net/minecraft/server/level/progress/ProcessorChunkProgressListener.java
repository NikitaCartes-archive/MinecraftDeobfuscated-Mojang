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

	public ProcessorChunkProgressListener(ChunkProgressListener chunkProgressListener, Executor executor) {
		this.delegate = chunkProgressListener;
		this.mailbox = ProcessorMailbox.create(executor, "progressListener");
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
	public void stop() {
		this.mailbox.tell(this.delegate::stop);
	}
}
