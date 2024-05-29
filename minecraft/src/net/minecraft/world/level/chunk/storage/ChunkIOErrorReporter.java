package net.minecraft.world.level.chunk.storage;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.level.ChunkPos;

public interface ChunkIOErrorReporter {
	void reportChunkLoadFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos);

	void reportChunkSaveFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos);

	static ReportedException createMisplacedChunkReport(ChunkPos chunkPos, ChunkPos chunkPos2) {
		CrashReport crashReport = CrashReport.forThrowable(
			new IllegalStateException("Retrieved chunk position " + chunkPos + " does not match requested " + chunkPos2), "Chunk found in invalid location"
		);
		CrashReportCategory crashReportCategory = crashReport.addCategory("Misplaced Chunk");
		crashReportCategory.setDetail("Stored Position", chunkPos::toString);
		return new ReportedException(crashReport);
	}

	default void reportMisplacedChunk(ChunkPos chunkPos, ChunkPos chunkPos2, RegionStorageInfo regionStorageInfo) {
		this.reportChunkLoadFailure(createMisplacedChunkReport(chunkPos, chunkPos2), regionStorageInfo, chunkPos2);
	}
}
