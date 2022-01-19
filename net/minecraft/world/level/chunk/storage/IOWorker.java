/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class IOWorker
implements ChunkScanAccess,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
    private final RegionFileStorage storage;
    private final Map<ChunkPos, PendingStore> pendingWrites = Maps.newLinkedHashMap();

    protected IOWorker(Path path, boolean bl, String string) {
        this.storage = new RegionFileStorage(path, bl);
        this.mailbox = new ProcessorMailbox<StrictQueue.IntRunnable>(new StrictQueue.FixedPriorityQueue(Priority.values().length), Util.ioPool(), "IOWorker-" + string);
    }

    public CompletableFuture<Void> store(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
        return this.submitTask(() -> {
            PendingStore pendingStore = this.pendingWrites.computeIfAbsent(chunkPos, chunkPos -> new PendingStore(compoundTag));
            pendingStore.data = compoundTag;
            return Either.left(pendingStore.result);
        }).thenCompose(Function.identity());
    }

    @Nullable
    public CompoundTag load(ChunkPos chunkPos) throws IOException {
        CompletableFuture<CompoundTag> completableFuture = this.loadAsync(chunkPos);
        try {
            return completableFuture.join();
        } catch (CompletionException completionException) {
            if (completionException.getCause() instanceof IOException) {
                throw (IOException)completionException.getCause();
            }
            throw completionException;
        }
    }

    protected CompletableFuture<CompoundTag> loadAsync(ChunkPos chunkPos) {
        return this.submitTask(() -> {
            PendingStore pendingStore = this.pendingWrites.get(chunkPos);
            if (pendingStore != null) {
                return Either.left(pendingStore.data);
            }
            try {
                CompoundTag compoundTag = this.storage.read(chunkPos);
                return Either.left(compoundTag);
            } catch (Exception exception) {
                LOGGER.warn("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
                return Either.right(exception);
            }
        });
    }

    public CompletableFuture<Void> synchronize(boolean bl) {
        CompletionStage completableFuture = this.submitTask(() -> Either.left(CompletableFuture.allOf((CompletableFuture[])this.pendingWrites.values().stream().map(pendingStore -> pendingStore.result).toArray(CompletableFuture[]::new)))).thenCompose(Function.identity());
        if (bl) {
            return ((CompletableFuture)completableFuture).thenCompose(void_ -> this.submitTask(() -> {
                try {
                    this.storage.flush();
                    return Either.left(null);
                } catch (Exception exception) {
                    LOGGER.warn("Failed to synchronize chunks", exception);
                    return Either.right(exception);
                }
            }));
        }
        return ((CompletableFuture)completableFuture).thenCompose(void_ -> this.submitTask(() -> Either.left(null)));
    }

    @Override
    public CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) {
        return this.submitTask(() -> {
            try {
                PendingStore pendingStore = this.pendingWrites.get(chunkPos);
                if (pendingStore != null) {
                    if (pendingStore.data != null) {
                        pendingStore.data.acceptAsRoot(streamTagVisitor);
                    }
                } else {
                    this.storage.scanChunk(chunkPos, streamTagVisitor);
                }
                return Either.left(null);
            } catch (Exception exception) {
                LOGGER.warn("Failed to bulk scan chunk {}", (Object)chunkPos, (Object)exception);
                return Either.right(exception);
            }
        });
    }

    private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier) {
        return this.mailbox.askEither(processorHandle -> new StrictQueue.IntRunnable(Priority.FOREGROUND.ordinal(), () -> this.method_27939(processorHandle, (Supplier)supplier)));
    }

    private void storePendingChunk() {
        if (this.pendingWrites.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<ChunkPos, PendingStore>> iterator = this.pendingWrites.entrySet().iterator();
        Map.Entry<ChunkPos, PendingStore> entry = iterator.next();
        iterator.remove();
        this.runStore(entry.getKey(), entry.getValue());
        this.tellStorePending();
    }

    private void tellStorePending() {
        this.mailbox.tell(new StrictQueue.IntRunnable(Priority.BACKGROUND.ordinal(), this::storePendingChunk));
    }

    private void runStore(ChunkPos chunkPos, PendingStore pendingStore) {
        try {
            this.storage.write(chunkPos, pendingStore.data);
            pendingStore.result.complete(null);
        } catch (Exception exception) {
            LOGGER.error("Failed to store chunk {}", (Object)chunkPos, (Object)exception);
            pendingStore.result.completeExceptionally(exception);
        }
    }

    @Override
    public void close() throws IOException {
        if (!this.shutdownRequested.compareAndSet(false, true)) {
            return;
        }
        this.mailbox.ask(processorHandle -> new StrictQueue.IntRunnable(Priority.SHUTDOWN.ordinal(), () -> processorHandle.tell(Unit.INSTANCE))).join();
        this.mailbox.close();
        try {
            this.storage.close();
        } catch (Exception exception) {
            LOGGER.error("Failed to close storage", exception);
        }
    }

    private /* synthetic */ void method_27939(ProcessorHandle processorHandle, Supplier supplier) {
        if (!this.shutdownRequested.get()) {
            processorHandle.tell((Either)supplier.get());
        }
        this.tellStorePending();
    }

    static enum Priority {
        FOREGROUND,
        BACKGROUND,
        SHUTDOWN;

    }

    static class PendingStore {
        @Nullable
        CompoundTag data;
        final CompletableFuture<Void> result = new CompletableFuture();

        public PendingStore(@Nullable CompoundTag compoundTag) {
            this.data = compoundTag;
        }
    }
}

