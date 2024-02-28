package net.minecraft.server.level;

import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkLevel {
	private static final int FULL_CHUNK_LEVEL = 33;
	private static final int BLOCK_TICKING_LEVEL = 32;
	private static final int ENTITY_TICKING_LEVEL = 31;
	public static final int MAX_LEVEL = 33 + ChunkStatus.maxDistance();

	public static ChunkStatus generationStatus(int i) {
		return i < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(i - 33);
	}

	public static int byStatus(ChunkStatus chunkStatus) {
		return 33 + ChunkStatus.getDistance(chunkStatus);
	}

	public static FullChunkStatus fullStatus(int i) {
		if (i <= 31) {
			return FullChunkStatus.ENTITY_TICKING;
		} else if (i <= 32) {
			return FullChunkStatus.BLOCK_TICKING;
		} else {
			return i <= 33 ? FullChunkStatus.FULL : FullChunkStatus.INACCESSIBLE;
		}
	}

	public static int byStatus(FullChunkStatus fullChunkStatus) {
		return switch (fullChunkStatus) {
			case INACCESSIBLE -> MAX_LEVEL;
			case FULL -> 33;
			case BLOCK_TICKING -> 32;
			case ENTITY_TICKING -> 31;
		};
	}

	public static boolean isEntityTicking(int i) {
		return i <= 31;
	}

	public static boolean isBlockTicking(int i) {
		return i <= 32;
	}

	public static boolean isLoaded(int i) {
		return i <= MAX_LEVEL;
	}
}
