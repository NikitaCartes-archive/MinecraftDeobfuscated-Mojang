package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOWorker implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Thread thread;
	private final AtomicBoolean shutdownRequested = new AtomicBoolean();
	private final Queue<Runnable> inbox = Queues.<Runnable>newConcurrentLinkedQueue();
	private final RegionFileStorage storage;
	private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.<ChunkPos, IOWorker.PendingStore>newLinkedHashMap();
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
				IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.computeIfAbsent(chunkPos, chunkPosxxx -> new IOWorker.PendingStore());
				pendingStore.data = compoundTag;
				pendingStore.result.whenComplete((void_, throwable) -> {
					if (throwable != null) {
						completableFuture.completeExceptionally(throwable);
					} else {
						completableFuture.complete(null);
					}
				});
			});
	}

	@Nullable
	public CompoundTag load(ChunkPos chunkPos) throws IOException {
		CompletableFuture<CompoundTag> completableFuture = this.submitTask(completableFuturex -> () -> {
				IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.get(chunkPos);
				if (pendingStore != null) {
					completableFuturex.complete(pendingStore.data);
				} else {
					try {
						CompoundTag compoundTag = this.storage.read(chunkPos);
						completableFuturex.complete(compoundTag);
					} catch (Exception var5) {
						LOGGER.warn("Failed to read chunk {}", chunkPos, var5);
						completableFuturex.completeExceptionally(var5);
					}
				}
			});

		try {
			return (CompoundTag)completableFuture.join();
		} catch (CompletionException var4) {
			if (var4.getCause() instanceof IOException) {
				throw (IOException)var4.getCause();
			} else {
				throw var4;
			}
		}
	}

	private CompletableFuture<Void> shutdown() {
		return this.submitTask(completableFuture -> () -> {
				this.running = false;
				this.shutdownListener = completableFuture;
			});
	}

	public CompletableFuture<Void> synchronize() {
		return this.submitTask(
			completableFuture -> () -> {
					CompletableFuture<?> completableFuture2 = CompletableFuture.allOf(
						(CompletableFuture[])this.pendingWrites.values().stream().map(pendingStore -> pendingStore.result).toArray(CompletableFuture[]::new)
					);
					completableFuture2.whenComplete((object, throwable) -> completableFuture.complete(null));
				}
		);
	}

	private <T> CompletableFuture<T> submitTask(Function<CompletableFuture<T>, Runnable> function) {
		CompletableFuture<T> completableFuture = new CompletableFuture();
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
				if (!bl && !bl2) {
					this.waitForQueueNonEmpty();
				}
			}

			this.processInbox();
			this.storeRemainingPendingChunks();
		} finally {
			this.closeStorage();
		}
	}

	private boolean storePendingChunk() {
		Iterator<Entry<ChunkPos, IOWorker.PendingStore>> iterator = this.pendingWrites.entrySet().iterator();
		if (!iterator.hasNext()) {
			return false;
		} else {
			Entry<ChunkPos, IOWorker.PendingStore> entry = (Entry<ChunkPos, IOWorker.PendingStore>)iterator.next();
			iterator.remove();
			this.runStore((ChunkPos)entry.getKey(), (IOWorker.PendingStore)entry.getValue());
			return true;
		}
	}

	private void storeRemainingPendingChunks() {
		this.pendingWrites.forEach(this::runStore);
		this.pendingWrites.clear();
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

	private void closeStorage() {
		try {
			this.storage.close();
			this.shutdownListener.complete(null);
		} catch (Exception var2) {
			LOGGER.error("Failed to close storage", (Throwable)var2);
			this.shutdownListener.completeExceptionally(var2);
		}
	}

	private boolean processInbox() {
		boolean bl = false;

		Runnable runnable;
		while ((runnable = (Runnable)this.inbox.poll()) != null) {
			bl = true;
			runnable.run();
		}

		return bl;
	}

	public void close() throws IOException {
		if (this.shutdownRequested.compareAndSet(false, true)) {
			try {
				this.shutdown().join();
			} catch (CompletionException var2) {
				if (var2.getCause() instanceof IOException) {
					throw (IOException)var2.getCause();
				} else {
					throw var2;
				}
			}
		}
	}

	static class PendingStore {
		private CompoundTag data;
		private final CompletableFuture<Void> result = new CompletableFuture();

		private PendingStore() {
		}
	}
}
