/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkCompileTask
implements Comparable<ChunkCompileTask> {
    private final RenderChunk chunk;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Runnable> cancelListeners = Lists.newArrayList();
    private final Type type;
    private final double distAtCreation;
    @Nullable
    private RenderChunkRegion region;
    private ChunkBufferBuilderPack builders;
    private CompiledChunk compiledChunk;
    private Status status = Status.PENDING;
    private boolean isCancelled;

    public ChunkCompileTask(RenderChunk renderChunk, Type type, double d, @Nullable RenderChunkRegion renderChunkRegion) {
        this.chunk = renderChunk;
        this.type = type;
        this.distAtCreation = d;
        this.region = renderChunkRegion;
    }

    public Status getStatus() {
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

    public void setStatus(Status status) {
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
            if (this.type == Type.REBUILD_CHUNK && this.status != Status.DONE) {
                this.chunk.setDirty(false);
            }
            this.isCancelled = true;
            this.status = Status.DONE;
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

    public Type getType() {
        return this.type;
    }

    public boolean wasCancelled() {
        return this.isCancelled;
    }

    @Override
    public int compareTo(ChunkCompileTask chunkCompileTask) {
        return Doubles.compare(this.distAtCreation, chunkCompileTask.distAtCreation);
    }

    public double getDistAtCreation() {
        return this.distAtCreation;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((ChunkCompileTask)object);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Status {
        PENDING,
        COMPILING,
        UPLOADING,
        DONE;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        REBUILD_CHUNK,
        RESORT_TRANSPARENCY;

    }
}

