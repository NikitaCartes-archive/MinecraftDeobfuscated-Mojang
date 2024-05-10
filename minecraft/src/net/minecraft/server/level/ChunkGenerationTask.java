package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkGenerationTask {
	private final GeneratingChunkMap chunkMap;
	private final ChunkPos pos;
	@Nullable
	private ChunkStatus scheduledStatus = null;
	public final ChunkStatus targetStatus;
	private volatile boolean markedForCancellation;
	private final List<CompletableFuture<ChunkResult<ChunkAccess>>> scheduledLayer = new ArrayList();
	private final StaticCache2D<GenerationChunkHolder> cache;
	private boolean needsGeneration;

	private ChunkGenerationTask(
		GeneratingChunkMap generatingChunkMap, ChunkStatus chunkStatus, ChunkPos chunkPos, StaticCache2D<GenerationChunkHolder> staticCache2D
	) {
		this.chunkMap = generatingChunkMap;
		this.targetStatus = chunkStatus;
		this.pos = chunkPos;
		this.cache = staticCache2D;
	}

	public static ChunkGenerationTask create(GeneratingChunkMap generatingChunkMap, ChunkStatus chunkStatus, ChunkPos chunkPos) {
		int i = ChunkPyramid.GENERATION_PYRAMID.getStepTo(chunkStatus).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
		StaticCache2D<GenerationChunkHolder> staticCache2D = StaticCache2D.create(
			chunkPos.x, chunkPos.z, i, (ix, j) -> generatingChunkMap.acquireGeneration(ChunkPos.asLong(ix, j))
		);
		return new ChunkGenerationTask(generatingChunkMap, chunkStatus, chunkPos, staticCache2D);
	}

	@Nullable
	public CompletableFuture<?> runUntilWait() {
		while (true) {
			CompletableFuture<?> completableFuture = this.waitForScheduledLayer();
			if (completableFuture != null) {
				return completableFuture;
			}

			if (this.markedForCancellation || this.scheduledStatus == this.targetStatus) {
				this.releaseClaim();
				return null;
			}

			this.scheduleNextLayer();
		}
	}

	private void scheduleNextLayer() {
		ChunkStatus chunkStatus;
		if (this.scheduledStatus == null) {
			chunkStatus = ChunkStatus.EMPTY;
		} else if (!this.needsGeneration && this.scheduledStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
			this.needsGeneration = true;
			chunkStatus = ChunkStatus.EMPTY;
		} else {
			chunkStatus = (ChunkStatus)ChunkStatus.getStatusList().get(this.scheduledStatus.getIndex() + 1);
		}

		this.scheduleLayer(chunkStatus, this.needsGeneration);
		this.scheduledStatus = chunkStatus;
	}

	public void markForCancellation() {
		this.markedForCancellation = true;
	}

	private void releaseClaim() {
		GenerationChunkHolder generationChunkHolder = this.cache.get(this.pos.x, this.pos.z);
		generationChunkHolder.removeTask(this);
		this.cache.forEach(this.chunkMap::releaseGeneration);
	}

	private boolean canLoadWithoutGeneration() {
		if (this.targetStatus == ChunkStatus.EMPTY) {
			return true;
		} else {
			ChunkStatus chunkStatus = this.cache.get(this.pos.x, this.pos.z).getPersistedStatus();
			if (chunkStatus != null && !chunkStatus.isBefore(this.targetStatus)) {
				ChunkDependencies chunkDependencies = ChunkPyramid.LOADING_PYRAMID.getStepTo(this.targetStatus).accumulatedDependencies();
				int i = chunkDependencies.getRadius();

				for (int j = this.pos.x - i; j <= this.pos.x + i; j++) {
					for (int k = this.pos.z - i; k <= this.pos.z + i; k++) {
						int l = this.pos.getChessboardDistance(j, k);
						ChunkStatus chunkStatus2 = chunkDependencies.get(l);
						ChunkStatus chunkStatus3 = this.cache.get(j, k).getPersistedStatus();
						if (chunkStatus3 == null || chunkStatus3.isBefore(chunkStatus2)) {
							return false;
						}
					}
				}

				return true;
			} else {
				return false;
			}
		}
	}

	public GenerationChunkHolder getCenter() {
		return this.cache.get(this.pos.x, this.pos.z);
	}

	private void scheduleLayer(ChunkStatus chunkStatus, boolean bl) {
		int i = this.getRadiusForLayer(chunkStatus, bl);

		for (int j = this.pos.x - i; j <= this.pos.x + i; j++) {
			for (int k = this.pos.z - i; k <= this.pos.z + i; k++) {
				GenerationChunkHolder generationChunkHolder = this.cache.get(j, k);
				if (this.markedForCancellation || !this.scheduleChunkInLayer(chunkStatus, bl, generationChunkHolder)) {
					return;
				}
			}
		}
	}

	private int getRadiusForLayer(ChunkStatus chunkStatus, boolean bl) {
		ChunkPyramid chunkPyramid = bl ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
		return chunkPyramid.getStepTo(this.targetStatus).getAccumulatedRadiusOf(chunkStatus);
	}

	private boolean scheduleChunkInLayer(ChunkStatus chunkStatus, boolean bl, GenerationChunkHolder generationChunkHolder) {
		ChunkStatus chunkStatus2 = generationChunkHolder.getPersistedStatus();
		boolean bl2 = chunkStatus2 != null && chunkStatus.isAfter(chunkStatus2);
		ChunkPyramid chunkPyramid = bl2 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
		if (bl2 && !bl) {
			throw new IllegalStateException("Can't load chunk, but didn't expect to need to generate");
		} else {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = generationChunkHolder.applyStep(
				chunkPyramid.getStepTo(chunkStatus), this.chunkMap, this.cache
			);
			ChunkResult<ChunkAccess> chunkResult = (ChunkResult<ChunkAccess>)completableFuture.getNow(null);
			if (chunkResult == null) {
				this.scheduledLayer.add(completableFuture);
				return true;
			} else if (chunkResult.isSuccess()) {
				return true;
			} else {
				this.markForCancellation();
				return false;
			}
		}
	}

	@Nullable
	private CompletableFuture<?> waitForScheduledLayer() {
		while (!this.scheduledLayer.isEmpty()) {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.scheduledLayer.getLast();
			ChunkResult<ChunkAccess> chunkResult = (ChunkResult<ChunkAccess>)completableFuture.getNow(null);
			if (chunkResult == null) {
				return completableFuture;
			}

			this.scheduledLayer.removeLast();
			if (!chunkResult.isSuccess()) {
				this.markForCancellation();
			}
		}

		return null;
	}
}
