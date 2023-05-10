package net.minecraft.world.level.entity;

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;

@FunctionalInterface
public interface ChunkStatusUpdateListener {
	void onChunkStatusChange(ChunkPos chunkPos, FullChunkStatus fullChunkStatus);
}
