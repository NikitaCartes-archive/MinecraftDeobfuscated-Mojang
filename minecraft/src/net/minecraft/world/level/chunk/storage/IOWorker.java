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
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class IOWorker implements ChunkScanAccess, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final AtomicBoolean shutdownRequested = new AtomicBoolean();
	private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
	private final RegionFileStorage storage;
	private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.<ChunkPos, IOWorker.PendingStore>newLinkedHashMap();
	private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap<>();
	private static final int REGION_CACHE_SIZE = 1024;

	protected IOWorker(Path path, boolean bl, String string) {
		this.storage = new RegionFileStorage(path, bl);
		this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + string);
	}

	public boolean isOldChunkAround(ChunkPos chunkPos, int i) {
		ChunkPos chunkPos2 = new ChunkPos(chunkPos.x - i, chunkPos.z - i);
		ChunkPos chunkPos3 = new ChunkPos(chunkPos.x + i, chunkPos.z + i);

		for (int j = chunkPos2.getRegionX(); j <= chunkPos3.getRegionX(); j++) {
			for (int k = chunkPos2.getRegionZ(); k <= chunkPos3.getRegionZ(); k++) {
				BitSet bitSet = (BitSet)this.getOrCreateOldDataForRegion(j, k).join();
				if (!bitSet.isEmpty()) {
					ChunkPos chunkPos4 = ChunkPos.minFromRegion(j, k);
					int l = Math.max(chunkPos2.x - chunkPos4.x, 0);
					int m = Math.max(chunkPos2.z - chunkPos4.z, 0);
					int n = Math.min(chunkPos3.x - chunkPos4.x, 31);
					int o = Math.min(chunkPos3.z - chunkPos4.z, 31);

					for (int p = l; p <= n; p++) {
						for (int q = m; q <= o; q++) {
							int r = q * 32 + p;
							if (bitSet.get(r)) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int i, int j) {
		long l = ChunkPos.asLong(i, j);
		synchronized (this.regionCacheForBlender) {
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
			ChunkPos chunkPos = ChunkPos.minFromRegion(i, j);
			ChunkPos chunkPos2 = ChunkPos.maxFromRegion(i, j);
			BitSet bitSet = new BitSet();
			ChunkPos.rangeClosed(chunkPos, chunkPos2).forEach(chunkPosx -> {
				CollectFields collectFields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));

				try {
					this.scanChunk(chunkPosx, collectFields).join();
				} catch (Exception var7) {
					LOGGER.warn("Failed to scan chunk {}", chunkPosx, var7);
					return;
				}

				if (collectFields.getResult() instanceof CompoundTag compoundTag && this.isOldChunk(compoundTag)) {
					int ixx = chunkPosx.getRegionLocalZ() * 32 + chunkPosx.getRegionLocalX();
					bitSet.set(ixx);
				}
			});
			return bitSet;
		}, Util.backgroundExecutor());
	}

	private boolean isOldChunk(CompoundTag compoundTag) {
		return compoundTag.contains("DataVersion", 99) && compoundTag.getInt("DataVersion") >= 3088 ? compoundTag.contains("blending_data", 10) : true;
	}

	public CompletableFuture<Void> store(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
		return this.submitTask(
				() -> {
					IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites
						.computeIfAbsent(chunkPos, chunkPosxx -> new IOWorker.PendingStore(compoundTag));
					pendingStore.data = compoundTag;
					return Either.left(pendingStore.result);
				}
			)
			.thenCompose(Function.identity());
	}

	public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos chunkPos) {
		return this.submitTask(() -> {
			IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.get(chunkPos);
			if (pendingStore != null) {
				return Either.left(Optional.ofNullable(pendingStore.data));
			} else {
				try {
					CompoundTag compoundTag = this.storage.read(chunkPos);
					return Either.left(Optional.ofNullable(compoundTag));
				} catch (Exception var4) {
					LOGGER.warn("Failed to read chunk {}", chunkPos, var4);
					return Either.right(var4);
				}
			}
		});
	}

	public CompletableFuture<Void> synchronize(boolean bl) {
		CompletableFuture<Void> completableFuture = this.submitTask(
				() -> Either.left(
						CompletableFuture.allOf(
							(CompletableFuture[])this.pendingWrites.values().stream().map(pendingStore -> pendingStore.result).toArray(CompletableFuture[]::new)
						)
					)
			)
			.thenCompose(Function.identity());
		return bl ? completableFuture.thenCompose(void_ -> this.submitTask(() -> {
				try {
					this.storage.flush();
					return Either.left(null);
				} catch (Exception var2x) {
					LOGGER.warn("Failed to synchronize chunks", (Throwable)var2x);
					return Either.right(var2x);
				}
			})) : completableFuture.thenCompose(void_ -> this.submitTask(() -> Either.left(null)));
	}

	@Override
	public CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) {
		return this.submitTask(() -> {
			try {
				IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.get(chunkPos);
				if (pendingStore != null) {
					if (pendingStore.data != null) {
						pendingStore.data.acceptAsRoot(streamTagVisitor);
					}
				} else {
					this.storage.scanChunk(chunkPos, streamTagVisitor);
				}

				return Either.left(null);
			} catch (Exception var4) {
				LOGGER.warn("Failed to bulk scan chunk {}", chunkPos, var4);
				return Either.right(var4);
			}
		});
	}

	private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier) {
		return this.mailbox.askEither(processorHandle -> new StrictQueue.IntRunnable(IOWorker.Priority.FOREGROUND.ordinal(), () -> {
				if (!this.shutdownRequested.get()) {
					processorHandle.tell((Either)supplier.get());
				}

				this.tellStorePending();
			}));
	}

	private void storePendingChunk() {
		if (!this.pendingWrites.isEmpty()) {
			Iterator<Entry<ChunkPos, IOWorker.PendingStore>> iterator = this.pendingWrites.entrySet().iterator();
			Entry<ChunkPos, IOWorker.PendingStore> entry = (Entry<ChunkPos, IOWorker.PendingStore>)iterator.next();
			iterator.remove();
			this.runStore((ChunkPos)entry.getKey(), (IOWorker.PendingStore)entry.getValue());
			this.tellStorePending();
		}
	}

	private void tellStorePending() {
		this.mailbox.tell(new StrictQueue.IntRunnable(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
	}

	private void runStore(ChunkPos chunkPos, IOWorker.PendingStore pendingStore) {
		try {
			this.storage.write(chunkPos, pendingStore.data);
			pendingStore.result.complete(null);
		} catch (Exception var4) {
			LOGGER.error("Failed to store chunk {}", chunkPos, var4);
			pendingStore.result.completeExceptionally(var4);
		}
	}

	public void close() throws IOException {
		if (this.shutdownRequested.compareAndSet(false, true)) {
			this.mailbox.ask(processorHandle -> new StrictQueue.IntRunnable(IOWorker.Priority.SHUTDOWN.ordinal(), () -> processorHandle.tell(Unit.INSTANCE))).join();
			this.mailbox.close();

			try {
				this.storage.close();
			} catch (Exception var2) {
				LOGGER.error("Failed to close storage", (Throwable)var2);
			}
		}
	}

	static class PendingStore {
		@Nullable
		CompoundTag data;
		final CompletableFuture<Void> result = new CompletableFuture();

		public PendingStore(@Nullable CompoundTag compoundTag) {
			this.data = compoundTag;
		}
	}

	static enum Priority {
		FOREGROUND,
		BACKGROUND,
		SHUTDOWN;
	}
}
