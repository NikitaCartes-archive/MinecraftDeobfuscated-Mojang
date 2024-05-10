package net.minecraft.server.level;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public interface GeneratingChunkMap {
	GenerationChunkHolder acquireGeneration(long l);

	void releaseGeneration(GenerationChunkHolder generationChunkHolder);

	CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder generationChunkHolder, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D);

	ChunkGenerationTask scheduleGenerationTask(ChunkStatus chunkStatus, ChunkPos chunkPos);

	void runGenerationTasks();
}
