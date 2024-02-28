package net.minecraft.world.level.chunk.status;

import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.chunk.ChunkAccess;

@FunctionalInterface
public interface ToFullChunk {
	CompletableFuture<ChunkAccess> apply(ChunkAccess chunkAccess);
}
