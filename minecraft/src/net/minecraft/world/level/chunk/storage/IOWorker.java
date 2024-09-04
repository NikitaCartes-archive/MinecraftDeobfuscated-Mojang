package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.SequencedMap;
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
import net.minecraft.util.thread.PriorityConsecutiveExecutor;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class IOWorker implements ChunkScanAccess, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final AtomicBoolean shutdownRequested = new AtomicBoolean();
	private final PriorityConsecutiveExecutor consecutiveExecutor;
	private final RegionFileStorage storage;
	private final SequencedMap<ChunkPos, IOWorker.PendingStore> pendingWrites = new LinkedHashMap();
	private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap<>();
	private static final int REGION_CACHE_SIZE = 1024;

	protected IOWorker(RegionStorageInfo regionStorageInfo, Path path, boolean bl) {
		this.storage = new RegionFileStorage(regionStorageInfo, path, bl);
		this.consecutiveExecutor = new PriorityConsecutiveExecutor(IOWorker.Priority.values().length, Util.ioPool(), "IOWorker-" + regionStorageInfo.type());
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
		return compoundTag.contains("DataVersion", 99) && compoundTag.getInt("DataVersion") >= 3441 ? compoundTag.contains("blending_data", 10) : true;
	}

	public CompletableFuture<Void> store(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
		return this.store(chunkPos, () -> compoundTag);
	}

	public CompletableFuture<Void> store(ChunkPos chunkPos, Supplier<CompoundTag> supplier) {
		return this.submitTask(
				() -> {
					CompoundTag compoundTag = (CompoundTag)supplier.get();
					IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites
						.computeIfAbsent(chunkPos, chunkPosxx -> new IOWorker.PendingStore(compoundTag));
					pendingStore.data = compoundTag;
					return pendingStore.result;
				}
			)
			.thenCompose(Function.identity());
	}

	public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos chunkPos) {
		return this.submitThrowingTask(() -> {
			IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.get(chunkPos);
			if (pendingStore != null) {
				return Optional.ofNullable(pendingStore.copyData());
			} else {
				try {
					CompoundTag compoundTag = this.storage.read(chunkPos);
					return Optional.ofNullable(compoundTag);
				} catch (Exception var4) {
					LOGGER.warn("Failed to read chunk {}", chunkPos, var4);
					throw var4;
				}
			}
		});
	}

	public CompletableFuture<Void> synchronize(boolean bl) {
		CompletableFuture<Void> completableFuture = this.submitTask(
				() -> CompletableFuture.allOf(
						(CompletableFuture[])this.pendingWrites.values().stream().map(pendingStore -> pendingStore.result).toArray(CompletableFuture[]::new)
					)
			)
			.thenCompose(Function.identity());
		return bl ? completableFuture.thenCompose(void_ -> this.<Void>submitThrowingTask(() -> {
				try {
					this.storage.flush();
					return null;
				} catch (Exception var2x) {
					LOGGER.warn("Failed to synchronize chunks", (Throwable)var2x);
					throw var2x;
				}
			})) : completableFuture.thenCompose(void_ -> this.submitTask(() -> null));
	}

	@Override
	public CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) {
		return this.submitThrowingTask(() -> {
			try {
				IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.get(chunkPos);
				if (pendingStore != null) {
					if (pendingStore.data != null) {
						pendingStore.data.acceptAsRoot(streamTagVisitor);
					}
				} else {
					this.storage.scanChunk(chunkPos, streamTagVisitor);
				}

				return null;
			} catch (Exception var4) {
				LOGGER.warn("Failed to bulk scan chunk {}", chunkPos, var4);
				throw var4;
			}
		});
	}

	private <T> CompletableFuture<T> submitThrowingTask(IOWorker.ThrowingSupplier<T> throwingSupplier) {
		return this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.FOREGROUND.ordinal(), completableFuture -> {
			if (!this.shutdownRequested.get()) {
				try {
					completableFuture.complete(throwingSupplier.get());
				} catch (Exception var4) {
					completableFuture.completeExceptionally(var4);
				}
			}

			this.tellStorePending();
		});
	}

	private <T> CompletableFuture<T> submitTask(Supplier<T> supplier) {
		return this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.FOREGROUND.ordinal(), completableFuture -> {
			if (!this.shutdownRequested.get()) {
				completableFuture.complete(supplier.get());
			}

			this.tellStorePending();
		});
	}

	private void storePendingChunk() {
		Entry<ChunkPos, IOWorker.PendingStore> entry = this.pendingWrites.pollFirstEntry();
		if (entry != null) {
			this.runStore((ChunkPos)entry.getKey(), (IOWorker.PendingStore)entry.getValue());
			this.tellStorePending();
		}
	}

	private void tellStorePending() {
		this.consecutiveExecutor.schedule(new StrictQueue.RunnableWithPriority(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
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
			this.waitForShutdown();
			this.consecutiveExecutor.close();

			try {
				this.storage.close();
			} catch (Exception var2) {
				LOGGER.error("Failed to close storage", (Throwable)var2);
			}
		}
	}

	private void waitForShutdown() {
		this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.SHUTDOWN.ordinal(), completableFuture -> completableFuture.complete(Unit.INSTANCE)).join();
	}

	public RegionStorageInfo storageInfo() {
		return this.storage.info();
	}

	static class PendingStore {
		@Nullable
		CompoundTag data;
		final CompletableFuture<Void> result = new CompletableFuture();

		public PendingStore(@Nullable CompoundTag compoundTag) {
			this.data = compoundTag;
		}

		@Nullable
		CompoundTag copyData() {
			CompoundTag compoundTag = this.data;
			return compoundTag == null ? null : compoundTag.copy();
		}
	}

	static enum Priority {
		FOREGROUND,
		BACKGROUND,
		SHUTDOWN;
	}

	@FunctionalInterface
	interface ThrowingSupplier<T> {
		@Nullable
		T get() throws Exception;
	}
}
