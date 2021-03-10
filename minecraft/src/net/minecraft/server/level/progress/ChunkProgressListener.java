package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface ChunkProgressListener {
	void updateSpawnPos(ChunkPos chunkPos);

	void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus);

	@Environment(EnvType.CLIENT)
	void start();

	void stop();
}
