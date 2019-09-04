package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBufferUploader;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ChunkRenderDispatcher {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
		.setNameFormat("Chunk Batcher %d")
		.setDaemon(true)
		.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
		.build();
	private final int bufferCount;
	private final List<Thread> threads = Lists.<Thread>newArrayList();
	private final List<ChunkRenderWorker> workers = Lists.<ChunkRenderWorker>newArrayList();
	private final PriorityBlockingQueue<ChunkCompileTask> chunksToBatch = Queues.newPriorityBlockingQueue();
	private final BlockingQueue<ChunkBufferBuilderPack> availableChunkBuffers;
	private final BufferUploader uploader = new BufferUploader();
	private final VertexBufferUploader vboUploader = new VertexBufferUploader();
	private final Queue<ChunkRenderDispatcher.PendingUpload> pendingUploads = Queues.<ChunkRenderDispatcher.PendingUpload>newPriorityQueue();
	private final ChunkRenderWorker localWorker;
	private Vec3 camera = Vec3.ZERO;

	public ChunkRenderDispatcher(boolean bl) {
		int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / 10485760 - 1);
		int j = Runtime.getRuntime().availableProcessors();
		int k = bl ? j : Math.min(j, 4);
		int l = Math.max(1, Math.min(k * 2, i));
		this.localWorker = new ChunkRenderWorker(this, new ChunkBufferBuilderPack());
		List<ChunkBufferBuilderPack> list = Lists.<ChunkBufferBuilderPack>newArrayListWithExpectedSize(l);

		try {
			for (int m = 0; m < l; m++) {
				list.add(new ChunkBufferBuilderPack());
			}
		} catch (OutOfMemoryError var11) {
			LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
			int n = list.size() * 2 / 3;

			for (int o = 0; o < n; o++) {
				list.remove(list.size() - 1);
			}

			System.gc();
		}

		this.bufferCount = list.size();
		this.availableChunkBuffers = Queues.<ChunkBufferBuilderPack>newArrayBlockingQueue(this.bufferCount);
		this.availableChunkBuffers.addAll(list);
		int m = Math.min(k, this.bufferCount);
		if (m > 1) {
			for (int n = 0; n < m; n++) {
				ChunkRenderWorker chunkRenderWorker = new ChunkRenderWorker(this);
				Thread thread = THREAD_FACTORY.newThread(chunkRenderWorker);
				thread.start();
				this.workers.add(chunkRenderWorker);
				this.threads.add(thread);
			}
		}
	}

	public String getStats() {
		return this.threads.isEmpty()
			? String.format("pC: %03d, single-threaded", this.chunksToBatch.size())
			: String.format("pC: %03d, pU: %02d, aB: %02d", this.chunksToBatch.size(), this.pendingUploads.size(), this.availableChunkBuffers.size());
	}

	public void setCamera(Vec3 vec3) {
		this.camera = vec3;
	}

	public Vec3 getCameraPosition() {
		return this.camera;
	}

	public boolean uploadAllPendingUploadsUntil(long l) {
		boolean bl = false;

		boolean bl2;
		do {
			bl2 = false;
			if (this.threads.isEmpty()) {
				ChunkCompileTask chunkCompileTask = (ChunkCompileTask)this.chunksToBatch.poll();
				if (chunkCompileTask != null) {
					try {
						this.localWorker.doTask(chunkCompileTask);
						bl2 = true;
					} catch (InterruptedException var9) {
						LOGGER.warn("Skipped task due to interrupt");
					}
				}
			}

			int i = 0;
			synchronized (this.pendingUploads) {
				while (i < 10) {
					ChunkRenderDispatcher.PendingUpload pendingUpload = (ChunkRenderDispatcher.PendingUpload)this.pendingUploads.poll();
					if (pendingUpload == null) {
						break;
					}

					if (!pendingUpload.future.isDone()) {
						pendingUpload.future.run();
						bl2 = true;
						bl = true;
						i++;
					}
				}
			}
		} while (l != 0L && bl2 && l >= Util.getNanos());

		return bl;
	}

	public boolean rebuildChunkAsync(RenderChunk renderChunk) {
		renderChunk.getTaskLock().lock();

		boolean var4;
		try {
			ChunkCompileTask chunkCompileTask = renderChunk.createCompileTask();
			chunkCompileTask.addCancelListener(() -> this.chunksToBatch.remove(chunkCompileTask));
			boolean bl = this.chunksToBatch.offer(chunkCompileTask);
			if (!bl) {
				chunkCompileTask.cancel();
			}

			var4 = bl;
		} finally {
			renderChunk.getTaskLock().unlock();
		}

		return var4;
	}

	public boolean rebuildChunkSync(RenderChunk renderChunk) {
		renderChunk.getTaskLock().lock();

		boolean var3;
		try {
			ChunkCompileTask chunkCompileTask = renderChunk.createCompileTask();

			try {
				this.localWorker.doTask(chunkCompileTask);
			} catch (InterruptedException var7) {
			}

			var3 = true;
		} finally {
			renderChunk.getTaskLock().unlock();
		}

		return var3;
	}

	public void blockUntilClear() {
		this.clearBatchQueue();
		List<ChunkBufferBuilderPack> list = Lists.<ChunkBufferBuilderPack>newArrayList();

		while (list.size() != this.bufferCount) {
			this.uploadAllPendingUploadsUntil(Long.MAX_VALUE);

			try {
				list.add(this.takeChunkBufferBuilder());
			} catch (InterruptedException var3) {
			}
		}

		this.availableChunkBuffers.addAll(list);
	}

	public void releaseChunkBufferBuilder(ChunkBufferBuilderPack chunkBufferBuilderPack) {
		this.availableChunkBuffers.add(chunkBufferBuilderPack);
	}

	public ChunkBufferBuilderPack takeChunkBufferBuilder() throws InterruptedException {
		return (ChunkBufferBuilderPack)this.availableChunkBuffers.take();
	}

	public ChunkCompileTask takeChunk() throws InterruptedException {
		return (ChunkCompileTask)this.chunksToBatch.take();
	}

	public boolean resortChunkTransparencyAsync(RenderChunk renderChunk) {
		renderChunk.getTaskLock().lock();

		boolean var3;
		try {
			ChunkCompileTask chunkCompileTask = renderChunk.createTransparencySortTask();
			if (chunkCompileTask == null) {
				return true;
			}

			chunkCompileTask.addCancelListener(() -> this.chunksToBatch.remove(chunkCompileTask));
			var3 = this.chunksToBatch.offer(chunkCompileTask);
		} finally {
			renderChunk.getTaskLock().unlock();
		}

		return var3;
	}

	public ListenableFuture<Void> uploadChunkLayer(
		BlockLayer blockLayer, BufferBuilder bufferBuilder, RenderChunk renderChunk, CompiledChunk compiledChunk, double d
	) {
		if (Minecraft.getInstance().isSameThread()) {
			this.uploadChunkLayer(bufferBuilder, renderChunk.getBuffer(blockLayer.ordinal()));
			bufferBuilder.offset(0.0, 0.0, 0.0);
			return Futures.immediateFuture(null);
		} else {
			ListenableFutureTask<Void> listenableFutureTask = ListenableFutureTask.create(
				() -> this.uploadChunkLayer(blockLayer, bufferBuilder, renderChunk, compiledChunk, d), null
			);
			synchronized (this.pendingUploads) {
				this.pendingUploads.add(new ChunkRenderDispatcher.PendingUpload(listenableFutureTask, d));
				return listenableFutureTask;
			}
		}
	}

	private void uploadChunkLayer(BufferBuilder bufferBuilder, VertexBuffer vertexBuffer) {
		this.vboUploader.setBuffer(vertexBuffer);
		this.vboUploader.end(bufferBuilder);
	}

	public void clearBatchQueue() {
		while (!this.chunksToBatch.isEmpty()) {
			ChunkCompileTask chunkCompileTask = (ChunkCompileTask)this.chunksToBatch.poll();
			if (chunkCompileTask != null) {
				chunkCompileTask.cancel();
			}
		}
	}

	public boolean isQueueEmpty() {
		return this.chunksToBatch.isEmpty() && this.pendingUploads.isEmpty();
	}

	public void dispose() {
		this.clearBatchQueue();

		for (ChunkRenderWorker chunkRenderWorker : this.workers) {
			chunkRenderWorker.stop();
		}

		for (Thread thread : this.threads) {
			try {
				thread.interrupt();
				thread.join();
			} catch (InterruptedException var4) {
				LOGGER.warn("Interrupted whilst waiting for worker to die", (Throwable)var4);
			}
		}

		this.availableChunkBuffers.clear();
	}

	@Environment(EnvType.CLIENT)
	class PendingUpload implements Comparable<ChunkRenderDispatcher.PendingUpload> {
		private final ListenableFutureTask<Void> future;
		private final double dist;

		public PendingUpload(ListenableFutureTask<Void> listenableFutureTask, double d) {
			this.future = listenableFutureTask;
			this.dist = d;
		}

		public int compareTo(ChunkRenderDispatcher.PendingUpload pendingUpload) {
			return Doubles.compare(this.dist, pendingUpload.dist);
		}
	}
}
