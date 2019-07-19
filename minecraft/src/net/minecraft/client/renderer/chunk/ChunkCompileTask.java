package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;

@Environment(EnvType.CLIENT)
public class ChunkCompileTask implements Comparable<ChunkCompileTask> {
	private final RenderChunk chunk;
	private final ReentrantLock lock = new ReentrantLock();
	private final List<Runnable> cancelListeners = Lists.<Runnable>newArrayList();
	private final ChunkCompileTask.Type type;
	private final double distAtCreation;
	@Nullable
	private RenderChunkRegion region;
	private ChunkBufferBuilderPack builders;
	private CompiledChunk compiledChunk;
	private ChunkCompileTask.Status status = ChunkCompileTask.Status.PENDING;
	private boolean isCancelled;

	public ChunkCompileTask(RenderChunk renderChunk, ChunkCompileTask.Type type, double d, @Nullable RenderChunkRegion renderChunkRegion) {
		this.chunk = renderChunk;
		this.type = type;
		this.distAtCreation = d;
		this.region = renderChunkRegion;
	}

	public ChunkCompileTask.Status getStatus() {
		return this.status;
	}

	public RenderChunk getChunk() {
		return this.chunk;
	}

	@Nullable
	public RenderChunkRegion takeRegion() {
		RenderChunkRegion renderChunkRegion = this.region;
		this.region = null;
		return renderChunkRegion;
	}

	public CompiledChunk getCompiledChunk() {
		return this.compiledChunk;
	}

	public void setCompiledChunk(CompiledChunk compiledChunk) {
		this.compiledChunk = compiledChunk;
	}

	public ChunkBufferBuilderPack getBuilders() {
		return this.builders;
	}

	public void setBuilders(ChunkBufferBuilderPack chunkBufferBuilderPack) {
		this.builders = chunkBufferBuilderPack;
	}

	public void setStatus(ChunkCompileTask.Status status) {
		this.lock.lock();

		try {
			this.status = status;
		} finally {
			this.lock.unlock();
		}
	}

	public void cancel() {
		this.lock.lock();

		try {
			this.region = null;
			if (this.type == ChunkCompileTask.Type.REBUILD_CHUNK && this.status != ChunkCompileTask.Status.DONE) {
				this.chunk.setDirty(false);
			}

			this.isCancelled = true;
			this.status = ChunkCompileTask.Status.DONE;

			for (Runnable runnable : this.cancelListeners) {
				runnable.run();
			}
		} finally {
			this.lock.unlock();
		}
	}

	public void addCancelListener(Runnable runnable) {
		this.lock.lock();

		try {
			this.cancelListeners.add(runnable);
			if (this.isCancelled) {
				runnable.run();
			}
		} finally {
			this.lock.unlock();
		}
	}

	public ReentrantLock getStatusLock() {
		return this.lock;
	}

	public ChunkCompileTask.Type getType() {
		return this.type;
	}

	public boolean wasCancelled() {
		return this.isCancelled;
	}

	public int compareTo(ChunkCompileTask chunkCompileTask) {
		return Doubles.compare(this.distAtCreation, chunkCompileTask.distAtCreation);
	}

	public double getDistAtCreation() {
		return this.distAtCreation;
	}

	@Environment(EnvType.CLIENT)
	public static enum Status {
		PENDING,
		COMPILING,
		UPLOADING,
		DONE;
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		REBUILD_CHUNK,
		RESORT_TRANSPARENCY;
	}
}
