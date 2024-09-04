package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public abstract class GenerationChunkHolder {
	private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
	private static final ChunkResult<ChunkAccess> NOT_DONE_YET = ChunkResult.error("Not done yet");
	public static final ChunkResult<ChunkAccess> UNLOADED_CHUNK = ChunkResult.error("Unloaded chunk");
	public static final CompletableFuture<ChunkResult<ChunkAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
	protected final ChunkPos pos;
	@Nullable
	private volatile ChunkStatus highestAllowedStatus;
	private final AtomicReference<ChunkStatus> startedWork = new AtomicReference();
	private final AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new AtomicReferenceArray(CHUNK_STATUSES.size());
	private final AtomicReference<ChunkGenerationTask> task = new AtomicReference();
	private final AtomicInteger generationRefCount = new AtomicInteger();
	private volatile CompletableFuture<Void> generationSaveSyncFuture = CompletableFuture.completedFuture(null);

	public GenerationChunkHolder(ChunkPos chunkPos) {
		this.pos = chunkPos;
	}

	public CompletableFuture<ChunkResult<ChunkAccess>> scheduleChunkGenerationTask(ChunkStatus chunkStatus, ChunkMap chunkMap) {
		if (this.isStatusDisallowed(chunkStatus)) {
			return UNLOADED_CHUNK_FUTURE;
		} else {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.getOrCreateFuture(chunkStatus);
			if (completableFuture.isDone()) {
				return completableFuture;
			} else {
				ChunkGenerationTask chunkGenerationTask = (ChunkGenerationTask)this.task.get();
				if (chunkGenerationTask == null || chunkStatus.isAfter(chunkGenerationTask.targetStatus)) {
					this.rescheduleChunkTask(chunkMap, chunkStatus);
				}

				return completableFuture;
			}
		}
	}

	CompletableFuture<ChunkResult<ChunkAccess>> applyStep(
		ChunkStep chunkStep, GeneratingChunkMap generatingChunkMap, StaticCache2D<GenerationChunkHolder> staticCache2D
	) {
		if (this.isStatusDisallowed(chunkStep.targetStatus())) {
			return UNLOADED_CHUNK_FUTURE;
		} else {
			return this.acquireStatusBump(chunkStep.targetStatus()) ? generatingChunkMap.applyStep(this, chunkStep, staticCache2D).handle((chunkAccess, throwable) -> {
				if (throwable != null) {
					CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception chunk generation/loading");
					MinecraftServer.setFatalException(new ReportedException(crashReport));
				} else {
					this.completeFuture(chunkStep.targetStatus(), chunkAccess);
				}

				return ChunkResult.of(chunkAccess);
			}) : this.getOrCreateFuture(chunkStep.targetStatus());
		}
	}

	protected void updateHighestAllowedStatus(ChunkMap chunkMap) {
		ChunkStatus chunkStatus = this.highestAllowedStatus;
		ChunkStatus chunkStatus2 = ChunkLevel.generationStatus(this.getTicketLevel());
		this.highestAllowedStatus = chunkStatus2;
		boolean bl = chunkStatus != null && (chunkStatus2 == null || chunkStatus2.isBefore(chunkStatus));
		if (bl) {
			this.failAndClearPendingFuturesBetween(chunkStatus2, chunkStatus);
			if (this.task.get() != null) {
				this.rescheduleChunkTask(chunkMap, this.findHighestStatusWithPendingFuture(chunkStatus2));
			}
		}
	}

	public void replaceProtoChunk(ImposterProtoChunk imposterProtoChunk) {
		CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = CompletableFuture.completedFuture(ChunkResult.of(imposterProtoChunk));

		for (int i = 0; i < this.futures.length() - 1; i++) {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture2 = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(i);
			Objects.requireNonNull(completableFuture2);
			ChunkAccess chunkAccess = (ChunkAccess)((ChunkResult)completableFuture2.getNow(NOT_DONE_YET)).orElse(null);
			if (!(chunkAccess instanceof ProtoChunk)) {
				throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + chunkAccess);
			}

			if (!this.futures.compareAndSet(i, completableFuture2, completableFuture)) {
				throw new IllegalStateException("Future changed by other thread while trying to replace it");
			}
		}
	}

	void removeTask(ChunkGenerationTask chunkGenerationTask) {
		this.task.compareAndSet(chunkGenerationTask, null);
	}

	private void rescheduleChunkTask(ChunkMap chunkMap, @Nullable ChunkStatus chunkStatus) {
		ChunkGenerationTask chunkGenerationTask;
		if (chunkStatus != null) {
			chunkGenerationTask = chunkMap.scheduleGenerationTask(chunkStatus, this.getPos());
		} else {
			chunkGenerationTask = null;
		}

		ChunkGenerationTask chunkGenerationTask2 = (ChunkGenerationTask)this.task.getAndSet(chunkGenerationTask);
		if (chunkGenerationTask2 != null) {
			chunkGenerationTask2.markForCancellation();
		}
	}

	private CompletableFuture<ChunkResult<ChunkAccess>> getOrCreateFuture(ChunkStatus chunkStatus) {
		if (this.isStatusDisallowed(chunkStatus)) {
			return UNLOADED_CHUNK_FUTURE;
		} else {
			int i = chunkStatus.getIndex();
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(i);

			while (completableFuture == null) {
				CompletableFuture<ChunkResult<ChunkAccess>> completableFuture2 = new CompletableFuture();
				completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.compareAndExchange(i, null, completableFuture2);
				if (completableFuture == null) {
					if (this.isStatusDisallowed(chunkStatus)) {
						this.failAndClearPendingFuture(i, completableFuture2);
						return UNLOADED_CHUNK_FUTURE;
					}

					return completableFuture2;
				}
			}

			return completableFuture;
		}
	}

	private void failAndClearPendingFuturesBetween(@Nullable ChunkStatus chunkStatus, ChunkStatus chunkStatus2) {
		int i = chunkStatus == null ? 0 : chunkStatus.getIndex() + 1;
		int j = chunkStatus2.getIndex();

		for (int k = i; k <= j; k++) {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(k);
			if (completableFuture != null) {
				this.failAndClearPendingFuture(k, completableFuture);
			}
		}
	}

	private void failAndClearPendingFuture(int i, CompletableFuture<ChunkResult<ChunkAccess>> completableFuture) {
		if (completableFuture.complete(UNLOADED_CHUNK) && !this.futures.compareAndSet(i, completableFuture, null)) {
			throw new IllegalStateException("Nothing else should replace the future here");
		}
	}

	private void completeFuture(ChunkStatus chunkStatus, ChunkAccess chunkAccess) {
		ChunkResult<ChunkAccess> chunkResult = ChunkResult.of(chunkAccess);
		int i = chunkStatus.getIndex();

		while (true) {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(i);
			if (completableFuture == null) {
				if (this.futures.compareAndSet(i, null, CompletableFuture.completedFuture(chunkResult))) {
					return;
				}
			} else {
				if (completableFuture.complete(chunkResult)) {
					return;
				}

				if (((ChunkResult)completableFuture.getNow(NOT_DONE_YET)).isSuccess()) {
					throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
				}

				Thread.yield();
			}
		}
	}

	@Nullable
	private ChunkStatus findHighestStatusWithPendingFuture(@Nullable ChunkStatus chunkStatus) {
		if (chunkStatus == null) {
			return null;
		} else {
			ChunkStatus chunkStatus2 = chunkStatus;

			for (ChunkStatus chunkStatus3 = (ChunkStatus)this.startedWork.get();
				chunkStatus3 == null || chunkStatus2.isAfter(chunkStatus3);
				chunkStatus2 = chunkStatus2.getParent()
			) {
				if (this.futures.get(chunkStatus2.getIndex()) != null) {
					return chunkStatus2;
				}

				if (chunkStatus2 == ChunkStatus.EMPTY) {
					break;
				}
			}

			return null;
		}
	}

	private boolean acquireStatusBump(ChunkStatus chunkStatus) {
		ChunkStatus chunkStatus2 = chunkStatus == ChunkStatus.EMPTY ? null : chunkStatus.getParent();
		ChunkStatus chunkStatus3 = (ChunkStatus)this.startedWork.compareAndExchange(chunkStatus2, chunkStatus);
		if (chunkStatus3 == chunkStatus2) {
			return true;
		} else if (chunkStatus3 != null && !chunkStatus.isAfter(chunkStatus3)) {
			return false;
		} else {
			throw new IllegalStateException("Unexpected last startedWork status: " + chunkStatus3 + " while trying to start: " + chunkStatus);
		}
	}

	private boolean isStatusDisallowed(ChunkStatus chunkStatus) {
		ChunkStatus chunkStatus2 = this.highestAllowedStatus;
		return chunkStatus2 == null || chunkStatus.isAfter(chunkStatus2);
	}

	protected abstract void addSaveDependency(CompletableFuture<?> completableFuture);

	public void increaseGenerationRefCount() {
		if (this.generationRefCount.getAndIncrement() == 0) {
			this.generationSaveSyncFuture = new CompletableFuture();
			this.addSaveDependency(this.generationSaveSyncFuture);
		}
	}

	public void decreaseGenerationRefCount() {
		CompletableFuture<Void> completableFuture = this.generationSaveSyncFuture;
		int i = this.generationRefCount.decrementAndGet();
		if (i == 0) {
			completableFuture.complete(null);
		}

		if (i < 0) {
			throw new IllegalStateException("More releases than claims. Count: " + i);
		}
	}

	@Nullable
	public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus chunkStatus) {
		CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(chunkStatus.getIndex());
		return completableFuture == null ? null : (ChunkAccess)((ChunkResult)completableFuture.getNow(NOT_DONE_YET)).orElse(null);
	}

	@Nullable
	public ChunkAccess getChunkIfPresent(ChunkStatus chunkStatus) {
		return this.isStatusDisallowed(chunkStatus) ? null : this.getChunkIfPresentUnchecked(chunkStatus);
	}

	@Nullable
	public ChunkAccess getLatestChunk() {
		ChunkStatus chunkStatus = (ChunkStatus)this.startedWork.get();
		if (chunkStatus == null) {
			return null;
		} else {
			ChunkAccess chunkAccess = this.getChunkIfPresentUnchecked(chunkStatus);
			return chunkAccess != null ? chunkAccess : this.getChunkIfPresentUnchecked(chunkStatus.getParent());
		}
	}

	@Nullable
	public ChunkStatus getPersistedStatus() {
		CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(ChunkStatus.EMPTY.getIndex());
		ChunkAccess chunkAccess = completableFuture == null ? null : (ChunkAccess)((ChunkResult)completableFuture.getNow(NOT_DONE_YET)).orElse(null);
		return chunkAccess == null ? null : chunkAccess.getPersistedStatus();
	}

	public ChunkPos getPos() {
		return this.pos;
	}

	public FullChunkStatus getFullStatus() {
		return ChunkLevel.fullStatus(this.getTicketLevel());
	}

	public abstract int getTicketLevel();

	public abstract int getQueueLevel();

	@VisibleForDebug
	public List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> getAllFutures() {
		List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> list = new ArrayList();

		for (int i = 0; i < CHUNK_STATUSES.size(); i++) {
			list.add(Pair.of((ChunkStatus)CHUNK_STATUSES.get(i), (CompletableFuture)this.futures.get(i)));
		}

		return list;
	}

	@Nullable
	@VisibleForDebug
	public ChunkStatus getLatestStatus() {
		for (int i = CHUNK_STATUSES.size() - 1; i >= 0; i--) {
			ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
			ChunkAccess chunkAccess = this.getChunkIfPresentUnchecked(chunkStatus);
			if (chunkAccess != null) {
				return chunkStatus;
			}
		}

		return null;
	}
}
