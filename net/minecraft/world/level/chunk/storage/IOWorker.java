/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class IOWorker
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Thread thread;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final Queue<Runnable> inbox = Queues.newConcurrentLinkedQueue();
    private final RegionFileStorage storage;
    private final Map<ChunkPos, PendingStore> pendingWrites = Maps.newLinkedHashMap();
    private boolean running = true;
    private CompletableFuture<Void> shutdownListener = new CompletableFuture();

    IOWorker(RegionFileStorage regionFileStorage, String string) {
        this.storage = regionFileStorage;
        this.thread = new Thread(this::loop);
        this.thread.setName(string + " IO worker");
        this.thread.start();
    }

    public CompletableFuture<Void> store(ChunkPos chunkPos, CompoundTag compoundTag) {
        return this.submitTask(completableFuture -> () -> {
            PendingStore pendingStore = this.pendingWrites.computeIfAbsent(chunkPos, chunkPos -> new PendingStore());
            pendingStore.data = compoundTag;
            pendingStore.result.whenComplete((void_, throwable) -> {
                if (throwable != null) {
                    completableFuture.completeExceptionally((Throwable)throwable);
                } else {
                    completableFuture.complete(null);
                }
            });
        });
    }

    @Nullable
    public CompoundTag load(ChunkPos chunkPos) throws IOException {
        CompletableFuture completableFuture2 = this.submitTask(completableFuture -> () -> {
            PendingStore pendingStore = this.pendingWrites.get(chunkPos);
            if (pendingStore != null) {
                completableFuture.complete(pendingStore.data);
            } else {
                try {
                    CompoundTag compoundTag = this.storage.read(chunkPos);
                    completableFuture.complete(compoundTag);
                } catch (Exception exception) {
                    LOGGER.warn("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
                    completableFuture.completeExceptionally(exception);
                }
            }
        });
        try {
            return (CompoundTag)completableFuture2.join();
        } catch (CompletionException completionException) {
            if (completionException.getCause() instanceof IOException) {
                throw (IOException)completionException.getCause();
            }
            throw completionException;
        }
    }

    private CompletableFuture<Void> shutdown() {
        return this.submitTask(completableFuture -> () -> {
            this.running = false;
            this.shutdownListener = completableFuture;
        });
    }

    public CompletableFuture<Void> synchronize() {
        return this.submitTask(completableFuture -> () -> {
            CompletableFuture<Void> completableFuture2 = CompletableFuture.allOf((CompletableFuture[])this.pendingWrites.values().stream().map(pendingStore -> ((PendingStore)pendingStore).result).toArray(CompletableFuture[]::new));
            completableFuture2.whenComplete((object, throwable) -> {
                try {
                    this.storage.flush();
                    completableFuture.complete(null);
                } catch (Exception exception) {
                    LOGGER.warn("Failed to synchronized chunks", (Throwable)exception);
                    completableFuture.completeExceptionally(exception);
                }
            });
        });
    }

    private <T> CompletableFuture<T> submitTask(Function<CompletableFuture<T>, Runnable> function) {
        CompletableFuture completableFuture = new CompletableFuture();
        this.inbox.add(function.apply(completableFuture));
        LockSupport.unpark(this.thread);
        return completableFuture;
    }

    private void waitForQueueNonEmpty() {
        LockSupport.park("waiting for tasks");
    }

    private void loop() {
        try {
            while (this.running) {
                boolean bl = this.processInbox();
                boolean bl2 = this.storePendingChunk();
                if (bl || bl2) continue;
                this.waitForQueueNonEmpty();
            }
            this.processInbox();
            this.storeRemainingPendingChunks();
        } finally {
            this.closeStorage();
        }
    }

    private boolean storePendingChunk() {
        Iterator<Map.Entry<ChunkPos, PendingStore>> iterator = this.pendingWrites.entrySet().iterator();
        if (!iterator.hasNext()) {
            return false;
        }
        Map.Entry<ChunkPos, PendingStore> entry = iterator.next();
        iterator.remove();
        this.runStore(entry.getKey(), entry.getValue());
        return true;
    }

    private void storeRemainingPendingChunks() {
        this.pendingWrites.forEach(this::runStore);
        this.pendingWrites.clear();
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

    private void closeStorage() {
        try {
            this.storage.close();
            this.shutdownListener.complete(null);
        } catch (Exception exception) {
            LOGGER.error("Failed to close storage", (Throwable)exception);
            this.shutdownListener.completeExceptionally(exception);
        }
    }

    private boolean processInbox() {
        Runnable runnable;
        boolean bl = false;
        while ((runnable = this.inbox.poll()) != null) {
            bl = true;
            runnable.run();
        }
        return bl;
    }

    @Override
    public void close() throws IOException {
        if (!this.shutdownRequested.compareAndSet(false, true)) {
            return;
        }
        try {
            this.shutdown().join();
        } catch (CompletionException completionException) {
            if (completionException.getCause() instanceof IOException) {
                throw (IOException)completionException.getCause();
            }
            throw completionException;
        }
    }

    static class PendingStore {
        private CompoundTag data;
        private final CompletableFuture<Void> result = new CompletableFuture();

        private PendingStore() {
        }
    }
}

