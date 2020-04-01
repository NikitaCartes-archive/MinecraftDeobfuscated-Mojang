package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface ChunkProgressListener {
	ChunkProgressListener NULL = new ChunkProgressListener() {
		@Override
		public void updateSpawnPos(ChunkPos chunkPos) {
		}

		@Override
		public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
		}

		@Override
		public void stop() {
		}
	};

	void updateSpawnPos(ChunkPos chunkPos);

	void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus);

	void stop();
}
