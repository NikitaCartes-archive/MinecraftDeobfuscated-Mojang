/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkCompileTask;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderWorker
implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ChunkRenderDispatcher dispatcher;
    private final ChunkBufferBuilderPack fixedBuffers;
    private boolean running = true;

    public ChunkRenderWorker(ChunkRenderDispatcher chunkRenderDispatcher) {
        this(chunkRenderDispatcher, null);
    }

    public ChunkRenderWorker(ChunkRenderDispatcher chunkRenderDispatcher, @Nullable ChunkBufferBuilderPack chunkBufferBuilderPack) {
        this.dispatcher = chunkRenderDispatcher;
        this.fixedBuffers = chunkBufferBuilderPack;
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                this.doTask(this.dispatcher.takeChunk());
            } catch (InterruptedException interruptedException) {
                LOGGER.debug("Stopping chunk worker due to interrupt");
                return;
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Batching chunks");
                Minecraft.getInstance().delayCrash(Minecraft.getInstance().fillReport(crashReport));
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void doTask(final ChunkCompileTask chunkCompileTask) throws InterruptedException {
        chunkCompileTask.getStatusLock().lock();
        try {
            if (!ChunkRenderWorker.checkState(chunkCompileTask, ChunkCompileTask.Status.PENDING)) {
                return;
            }
            if (!chunkCompileTask.getChunk().hasAllNeighbors()) {
                chunkCompileTask.cancel();
                return;
            }
            chunkCompileTask.setStatus(ChunkCompileTask.Status.COMPILING);
        } finally {
            chunkCompileTask.getStatusLock().unlock();
        }
        final ChunkBufferBuilderPack chunkBufferBuilderPack = this.takeBuffers();
        chunkCompileTask.getStatusLock().lock();
        try {
            if (!ChunkRenderWorker.checkState(chunkCompileTask, ChunkCompileTask.Status.COMPILING)) {
                this.releaseBuffers(chunkBufferBuilderPack);
                return;
            }
        } finally {
            chunkCompileTask.getStatusLock().unlock();
        }
        chunkCompileTask.setBuilders(chunkBufferBuilderPack);
        Vec3 vec3 = this.dispatcher.getCameraPosition();
        float f = (float)vec3.x;
        float g = (float)vec3.y;
        float h = (float)vec3.z;
        ChunkCompileTask.Type type = chunkCompileTask.getType();
        if (type == ChunkCompileTask.Type.REBUILD_CHUNK) {
            chunkCompileTask.getChunk().compile(f, g, h, chunkCompileTask);
        } else if (type == ChunkCompileTask.Type.RESORT_TRANSPARENCY) {
            chunkCompileTask.getChunk().rebuildTransparent(f, g, h, chunkCompileTask);
        }
        chunkCompileTask.getStatusLock().lock();
        try {
            if (!ChunkRenderWorker.checkState(chunkCompileTask, ChunkCompileTask.Status.COMPILING)) {
                this.releaseBuffers(chunkBufferBuilderPack);
                return;
            }
            chunkCompileTask.setStatus(ChunkCompileTask.Status.UPLOADING);
        } finally {
            chunkCompileTask.getStatusLock().unlock();
        }
        final CompiledChunk compiledChunk = chunkCompileTask.getCompiledChunk();
        ArrayList<ListenableFuture<Void>> list = Lists.newArrayList();
        if (type == ChunkCompileTask.Type.REBUILD_CHUNK) {
            for (BlockLayer blockLayer : BlockLayer.values()) {
                if (!compiledChunk.hasLayer(blockLayer)) continue;
                list.add(this.dispatcher.uploadChunkLayer(blockLayer, chunkCompileTask.getBuilders().builder(blockLayer), chunkCompileTask.getChunk(), compiledChunk, chunkCompileTask.getDistAtCreation()));
            }
        } else if (type == ChunkCompileTask.Type.RESORT_TRANSPARENCY) {
            list.add(this.dispatcher.uploadChunkLayer(BlockLayer.TRANSLUCENT, chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT), chunkCompileTask.getChunk(), compiledChunk, chunkCompileTask.getDistAtCreation()));
        }
        ListenableFuture listenableFuture = Futures.allAsList(list);
        chunkCompileTask.addCancelListener(() -> listenableFuture.cancel(false));
        Futures.addCallback(listenableFuture, new FutureCallback<List<Void>>(){

            @Override
            public void onSuccess(@Nullable List<Void> list) {
                ChunkRenderWorker.this.releaseBuffers(chunkBufferBuilderPack);
                chunkCompileTask.getStatusLock().lock();
                try {
                    if (!ChunkRenderWorker.checkState(chunkCompileTask, ChunkCompileTask.Status.UPLOADING)) {
                        return;
                    }
                    chunkCompileTask.setStatus(ChunkCompileTask.Status.DONE);
                } finally {
                    chunkCompileTask.getStatusLock().unlock();
                }
                chunkCompileTask.getChunk().setCompiledChunk(compiledChunk);
            }

            @Override
            public void onFailure(Throwable throwable) {
                ChunkRenderWorker.this.releaseBuffers(chunkBufferBuilderPack);
                if (!(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                }
            }

            @Override
            public /* synthetic */ void onSuccess(@Nullable Object object) {
                this.onSuccess((List)object);
            }
        });
    }

    private static boolean checkState(ChunkCompileTask chunkCompileTask, ChunkCompileTask.Status status) {
        if (chunkCompileTask.getStatus() != status) {
            if (!chunkCompileTask.wasCancelled()) {
                LOGGER.warn("Chunk render task was {} when I expected it to be {}; ignoring task", (Object)chunkCompileTask.getStatus(), (Object)status);
            }
            return false;
        }
        return true;
    }

    private ChunkBufferBuilderPack takeBuffers() throws InterruptedException {
        return this.fixedBuffers != null ? this.fixedBuffers : this.dispatcher.takeChunkBufferBuilder();
    }

    private void releaseBuffers(ChunkBufferBuilderPack chunkBufferBuilderPack) {
        if (chunkBufferBuilderPack != this.fixedBuffers) {
            this.dispatcher.releaseChunkBufferBuilder(chunkBufferBuilderPack);
        }
    }

    public void stop() {
        this.running = false;
    }
}

