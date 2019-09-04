/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.ArrayList;
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
import net.minecraft.client.renderer.chunk.ChunkCompileTask;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderDispatcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat("Chunk Batcher %d").setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).build();
    private final int bufferCount;
    private final List<Thread> threads = Lists.newArrayList();
    private final List<ChunkRenderWorker> workers = Lists.newArrayList();
    private final PriorityBlockingQueue<ChunkCompileTask> chunksToBatch = Queues.newPriorityBlockingQueue();
    private final BlockingQueue<ChunkBufferBuilderPack> availableChunkBuffers;
    private final BufferUploader uploader = new BufferUploader();
    private final VertexBufferUploader vboUploader = new VertexBufferUploader();
    private final Queue<PendingUpload> pendingUploads = Queues.newPriorityQueue();
    private final ChunkRenderWorker localWorker;
    private Vec3 camera = Vec3.ZERO;

    public ChunkRenderDispatcher(boolean bl) {
        int n;
        int m;
        int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / 0xA00000 - 1);
        int j = Runtime.getRuntime().availableProcessors();
        int k = bl ? j : Math.min(j, 4);
        int l = Math.max(1, Math.min(k * 2, i));
        this.localWorker = new ChunkRenderWorker(this, new ChunkBufferBuilderPack());
        ArrayList<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(l);
        try {
            for (m = 0; m < l; ++m) {
                list.add(new ChunkBufferBuilderPack());
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            LOGGER.warn("Allocated only {}/{} buffers", (Object)list.size(), (Object)l);
            n = list.size() * 2 / 3;
            for (int o = 0; o < n; ++o) {
                list.remove(list.size() - 1);
            }
            System.gc();
        }
        this.bufferCount = list.size();
        this.availableChunkBuffers = Queues.newArrayBlockingQueue(this.bufferCount);
        this.availableChunkBuffers.addAll(list);
        m = Math.min(k, this.bufferCount);
        if (m > 1) {
            for (n = 0; n < m; ++n) {
                ChunkRenderWorker chunkRenderWorker = new ChunkRenderWorker(this);
                Thread thread = THREAD_FACTORY.newThread(chunkRenderWorker);
                thread.start();
                this.workers.add(chunkRenderWorker);
                this.threads.add(thread);
            }
        }
    }

    public String getStats() {
        if (this.threads.isEmpty()) {
            return String.format("pC: %03d, single-threaded", this.chunksToBatch.size());
        }
        return String.format("pC: %03d, pU: %02d, aB: %02d", this.chunksToBatch.size(), this.pendingUploads.size(), this.availableChunkBuffers.size());
    }

    public void setCamera(Vec3 vec3) {
        this.camera = vec3;
    }

    public Vec3 getCameraPosition() {
        return this.camera;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean uploadAllPendingUploadsUntil(long l) {
        boolean bl2;
        boolean bl = false;
        do {
            ChunkCompileTask chunkCompileTask;
            bl2 = false;
            if (this.threads.isEmpty() && (chunkCompileTask = this.chunksToBatch.poll()) != null) {
                try {
                    this.localWorker.doTask(chunkCompileTask);
                    bl2 = true;
                } catch (InterruptedException interruptedException) {
                    LOGGER.warn("Skipped task due to interrupt");
                }
            }
            int i = 0;
            Queue<PendingUpload> queue = this.pendingUploads;
            synchronized (queue) {
                PendingUpload pendingUpload;
                while (i < 10 && (pendingUpload = this.pendingUploads.poll()) != null) {
                    if (pendingUpload.future.isDone()) continue;
                    pendingUpload.future.run();
                    bl2 = true;
                    bl = true;
                    ++i;
                }
            }
        } while (l != 0L && bl2 && l >= Util.getNanos());
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean rebuildChunkAsync(RenderChunk renderChunk) {
        renderChunk.getTaskLock().lock();
        try {
            ChunkCompileTask chunkCompileTask = renderChunk.createCompileTask();
            chunkCompileTask.addCancelListener(() -> this.chunksToBatch.remove(chunkCompileTask));
            boolean bl = this.chunksToBatch.offer(chunkCompileTask);
            if (!bl) {
                chunkCompileTask.cancel();
            }
            boolean bl2 = bl;
            return bl2;
        } finally {
            renderChunk.getTaskLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean rebuildChunkSync(RenderChunk renderChunk) {
        renderChunk.getTaskLock().lock();
        try {
            ChunkCompileTask chunkCompileTask = renderChunk.createCompileTask();
            try {
                this.localWorker.doTask(chunkCompileTask);
            } catch (InterruptedException interruptedException) {
                // empty catch block
            }
            boolean bl = true;
            return bl;
        } finally {
            renderChunk.getTaskLock().unlock();
        }
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
        ArrayList<ChunkBufferBuilderPack> list = Lists.newArrayList();
        while (list.size() != this.bufferCount) {
            this.uploadAllPendingUploadsUntil(Long.MAX_VALUE);
            try {
                list.add(this.takeChunkBufferBuilder());
            } catch (InterruptedException interruptedException) {}
        }
        this.availableChunkBuffers.addAll(list);
    }

    public void releaseChunkBufferBuilder(ChunkBufferBuilderPack chunkBufferBuilderPack) {
        this.availableChunkBuffers.add(chunkBufferBuilderPack);
    }

    public ChunkBufferBuilderPack takeChunkBufferBuilder() throws InterruptedException {
        return this.availableChunkBuffers.take();
    }

    public ChunkCompileTask takeChunk() throws InterruptedException {
        return this.chunksToBatch.take();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean resortChunkTransparencyAsync(RenderChunk renderChunk) {
        renderChunk.getTaskLock().lock();
        try {
            ChunkCompileTask chunkCompileTask = renderChunk.createTransparencySortTask();
            if (chunkCompileTask != null) {
                chunkCompileTask.addCancelListener(() -> this.chunksToBatch.remove(chunkCompileTask));
                boolean bl = this.chunksToBatch.offer(chunkCompileTask);
                return bl;
            }
            boolean bl = true;
            return bl;
        } finally {
            renderChunk.getTaskLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ListenableFuture<Void> uploadChunkLayer(BlockLayer blockLayer, BufferBuilder bufferBuilder, RenderChunk renderChunk, CompiledChunk compiledChunk, double d) {
        if (Minecraft.getInstance().isSameThread()) {
            this.uploadChunkLayer(bufferBuilder, renderChunk.getBuffer(blockLayer.ordinal()));
            bufferBuilder.offset(0.0, 0.0, 0.0);
            return Futures.immediateFuture(null);
        }
        ListenableFutureTask<Object> listenableFutureTask = ListenableFutureTask.create(() -> this.uploadChunkLayer(blockLayer, bufferBuilder, renderChunk, compiledChunk, d), null);
        Queue<PendingUpload> queue = this.pendingUploads;
        synchronized (queue) {
            this.pendingUploads.add(new PendingUpload(listenableFutureTask, d));
        }
        return listenableFutureTask;
    }

    private void uploadChunkLayer(BufferBuilder bufferBuilder, VertexBuffer vertexBuffer) {
        this.vboUploader.setBuffer(vertexBuffer);
        this.vboUploader.end(bufferBuilder);
    }

    public void clearBatchQueue() {
        while (!this.chunksToBatch.isEmpty()) {
            ChunkCompileTask chunkCompileTask = this.chunksToBatch.poll();
            if (chunkCompileTask == null) continue;
            chunkCompileTask.cancel();
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
            } catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted whilst waiting for worker to die", (Throwable)interruptedException);
            }
        }
        this.availableChunkBuffers.clear();
    }

    @Environment(value=EnvType.CLIENT)
    class PendingUpload
    implements Comparable<PendingUpload> {
        private final ListenableFutureTask<Void> future;
        private final double dist;

        public PendingUpload(ListenableFutureTask<Void> listenableFutureTask, double d) {
            this.future = listenableFutureTask;
            this.dist = d;
        }

        @Override
        public int compareTo(PendingUpload pendingUpload) {
            return Doubles.compare(this.dist, pendingUpload.dist);
        }

        @Override
        public /* synthetic */ int compareTo(Object object) {
            return this.compareTo((PendingUpload)object);
        }
    }
}

