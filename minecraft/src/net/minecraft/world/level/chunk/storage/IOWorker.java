package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOWorker implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final AtomicBoolean shutdownRequested = new AtomicBoolean();
	private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
	private final RegionFileStorage storage;
	private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.<ChunkPos, IOWorker.PendingStore>newLinkedHashMap();

	protected IOWorker(File file, boolean bl, String string) {
		this.storage = new RegionFileStorage(file, bl);
		this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + string);
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

	@Nullable
	public CompoundTag load(ChunkPos chunkPos) throws IOException {
		CompletableFuture<CompoundTag> completableFuture = this.loadAsync(chunkPos);

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

	protected CompletableFuture<CompoundTag> loadAsync(ChunkPos chunkPos) {
		return this.submitTask(() -> {
			IOWorker.PendingStore pendingStore = (IOWorker.PendingStore)this.pendingWrites.get(chunkPos);
			if (pendingStore != null) {
				return Either.left(pendingStore.data);
			} else {
				try {
					CompoundTag compoundTag = this.storage.read(chunkPos);
					return Either.left(compoundTag);
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
