package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;

public record ChunkStep(
	ChunkStatus targetStatus, ChunkDependencies directDependencies, ChunkDependencies accumulatedDependencies, int blockStateWriteRadius, ChunkStatusTask task
) {

	public int getAccumulatedRadiusOf(ChunkStatus chunkStatus) {
		return chunkStatus == this.targetStatus ? 0 : this.accumulatedDependencies.getRadiusOf(chunkStatus);
	}

	public CompletableFuture<ChunkAccess> apply(WorldGenContext worldGenContext, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
		if (chunkAccess.getPersistedStatus().isBefore(this.targetStatus)) {
			ProfiledDuration profiledDuration = JvmProfiler.INSTANCE
				.onChunkGenerate(chunkAccess.getPos(), worldGenContext.level().dimension(), this.targetStatus.getName());
			return this.task
				.doWork(worldGenContext, this, staticCache2D, chunkAccess)
				.thenApply(chunkAccessx -> this.completeChunkGeneration(chunkAccessx, profiledDuration));
		} else {
			return this.task.doWork(worldGenContext, this, staticCache2D, chunkAccess);
		}
	}

	private ChunkAccess completeChunkGeneration(ChunkAccess chunkAccess, @Nullable ProfiledDuration profiledDuration) {
		if (chunkAccess instanceof ProtoChunk protoChunk && protoChunk.getPersistedStatus().isBefore(this.targetStatus)) {
			protoChunk.setPersistedStatus(this.targetStatus);
		}

		if (profiledDuration != null) {
			profiledDuration.finish();
		}

		return chunkAccess;
	}

	public static class Builder {
		private final ChunkStatus status;
		@Nullable
		private final ChunkStep parent;
		private ChunkStatus[] directDependenciesByRadius;
		private int blockStateWriteRadius = -1;
		private ChunkStatusTask task = ChunkStatusTasks::passThrough;

		protected Builder(ChunkStatus chunkStatus) {
			if (chunkStatus.getParent() != chunkStatus) {
				throw new IllegalArgumentException("Not starting with the first status: " + chunkStatus);
			} else {
				this.status = chunkStatus;
				this.parent = null;
				this.directDependenciesByRadius = new ChunkStatus[0];
			}
		}

		protected Builder(ChunkStatus chunkStatus, ChunkStep chunkStep) {
			if (chunkStep.targetStatus.getIndex() != chunkStatus.getIndex() - 1) {
				throw new IllegalArgumentException("Out of order status: " + chunkStatus);
			} else {
				this.status = chunkStatus;
				this.parent = chunkStep;
				this.directDependenciesByRadius = new ChunkStatus[]{chunkStep.targetStatus};
			}
		}

		public ChunkStep.Builder addRequirement(ChunkStatus chunkStatus, int i) {
			if (chunkStatus.isOrAfter(this.status)) {
				throw new IllegalArgumentException("Status " + chunkStatus + " can not be required by " + this.status);
			} else {
				ChunkStatus[] chunkStatuss = this.directDependenciesByRadius;
				int j = i + 1;
				if (j > chunkStatuss.length) {
					this.directDependenciesByRadius = new ChunkStatus[j];
					Arrays.fill(this.directDependenciesByRadius, chunkStatus);
				}

				for (int k = 0; k < Math.min(j, chunkStatuss.length); k++) {
					this.directDependenciesByRadius[k] = ChunkStatus.max(chunkStatuss[k], chunkStatus);
				}

				return this;
			}
		}

		public ChunkStep.Builder blockStateWriteRadius(int i) {
			this.blockStateWriteRadius = i;
			return this;
		}

		public ChunkStep.Builder setTask(ChunkStatusTask chunkStatusTask) {
			this.task = chunkStatusTask;
			return this;
		}

		public ChunkStep build() {
			return new ChunkStep(
				this.status,
				new ChunkDependencies(ImmutableList.copyOf(this.directDependenciesByRadius)),
				new ChunkDependencies(ImmutableList.copyOf(this.buildAccumulatedDependencies())),
				this.blockStateWriteRadius,
				this.task
			);
		}

		private ChunkStatus[] buildAccumulatedDependencies() {
			if (this.parent == null) {
				return this.directDependenciesByRadius;
			} else {
				int i = this.getRadiusOfParent(this.parent.targetStatus);
				ChunkDependencies chunkDependencies = this.parent.accumulatedDependencies;
				ChunkStatus[] chunkStatuss = new ChunkStatus[Math.max(i + chunkDependencies.size(), this.directDependenciesByRadius.length)];

				for (int j = 0; j < chunkStatuss.length; j++) {
					int k = j - i;
					if (k < 0 || k >= chunkDependencies.size()) {
						chunkStatuss[j] = this.directDependenciesByRadius[j];
					} else if (j >= this.directDependenciesByRadius.length) {
						chunkStatuss[j] = chunkDependencies.get(k);
					} else {
						chunkStatuss[j] = ChunkStatus.max(this.directDependenciesByRadius[j], chunkDependencies.get(k));
					}
				}

				return chunkStatuss;
			}
		}

		private int getRadiusOfParent(ChunkStatus chunkStatus) {
			for (int i = this.directDependenciesByRadius.length - 1; i >= 0; i--) {
				if (this.directDependenciesByRadius[i].isOrAfter(chunkStatus)) {
					return i;
				}
			}

			return 0;
		}
	}
}
