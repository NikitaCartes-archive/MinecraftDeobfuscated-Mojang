/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
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
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap();
    private static final int REGION_CACHE_SIZE = 1024;

    protected IOWorker(Path path, boolean bl, String string) {
        this.storage = new RegionFileStorage(path, bl);
        this.mailbox = new ProcessorMailbox<StrictQueue.IntRunnable>(new StrictQueue.FixedPriorityQueue(Priority.values().length), Util.ioPool(), "IOWorker-" + string);
    }

    public boolean isOldChunkAround(ChunkPos chunkPos, int i) {
        ChunkPos chunkPos2 = new ChunkPos(chunkPos.x - i, chunkPos.z - i);
        ChunkPos chunkPos3 = new ChunkPos(chunkPos.x + i, chunkPos.z + i);
        for (int j = chunkPos2.getRegionX(); j <= chunkPos3.getRegionX(); ++j) {
            for (int k = chunkPos2.getRegionZ(); k <= chunkPos3.getRegionZ(); ++k) {
                BitSet bitSet = this.getOrCreateOldDataForRegion(j, k).join();
                if (bitSet.isEmpty()) continue;
                ChunkPos chunkPos4 = ChunkPos.minFromRegion(j, k);
                int l = Math.max(chunkPos2.x - chunkPos4.x, 0);
                int m = Math.max(chunkPos2.z - chunkPos4.z, 0);
                int n = Math.min(chunkPos3.x - chunkPos4.x, 31);
                int o = Math.min(chunkPos3.z - chunkPos4.z, 31);
                for (int p = l; p <= n; ++p) {
                    for (int q = m; q <= o; ++q) {
                        int r = q * 32 + p;
                        if (!bitSet.get(r)) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> long2ObjectLinkedOpenHashMap = this.regionCacheForBlender;
        synchronized (long2ObjectLinkedOpenHashMap) {
            CompletableFuture<BitSet> completableFuture = this.regionCacheForBlender.getAndMoveToFirst(l);
            if (completableFuture == null) {
                completableFuture = this.createOldDataForRegion(i, j);
                this.regionCacheForBlender.putAndMoveToFirst(l, completableFuture);
                if (this.regionCacheForBlender.size() > 1024) {
                    this.regionCacheForBlender.removeLast();
                }
            }
            return completableFuture;
        }
    }

    private CompletableFuture<BitSet> createOldDataForRegion(int i, int j) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkPos chunkPos2 = ChunkPos.minFromRegion(i, j);
            ChunkPos chunkPos22 = ChunkPos.maxFromRegion(i, j);
            BitSet bitSet = new BitSet();
            ChunkPos.rangeClosed(chunkPos2, chunkPos22).forEach(chunkPos -> {
                CompoundTag compoundTag;
                CollectFields collectFields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));
                try {
                    this.scanChunk((ChunkPos)chunkPos, collectFields).join();
                } catch (Exception exception) {
                    LOGGER.warn("Failed to scan chunk {}", chunkPos, (Object)exception);
                    return;
                }
                Tag tag = collectFields.getResult();
                if (tag instanceof CompoundTag && this.isOldChunk(compoundTag = (CompoundTag)tag)) {
                    int i = chunkPos.getRegionLocalZ() * 32 + chunkPos.getRegionLocalX();
                    bitSet.set(i);
                }
            });
            return bitSet;
        }, Util.backgroundExecutor());
    }

    private boolean isOldChunk(CompoundTag compoundTag) {
        if (!compoundTag.contains("DataVersion", 99) || compoundTag.getInt("DataVersion") < 3088) {
            return true;
        }
        return compoundTag.contains("blending_data", 10);
    }

    public CompletableFuture<Void> store(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
        return this.submitTask(() -> {
            PendingStore pendingStore = this.pendingWrites.computeIfAbsent(chunkPos, chunkPos -> new PendingStore(compoundTag));
            pendingStore.data = compoundTag;
            return Either.left(pendingStore.result);
        }).thenCompose(Function.identity());
    }

    public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos chunkPos) {
        return this.submitTask(() -> {
            PendingStore pendingStore = this.pendingWrites.get(chunkPos);
            if (pendingStore != null) {
                return Either.left(Optional.ofNullable(pendingStore.data));
            }
            try {
                CompoundTag compoundTag = this.storage.read(chunkPos);
                return Either.left(Optional.ofNullable(compoundTag));
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

