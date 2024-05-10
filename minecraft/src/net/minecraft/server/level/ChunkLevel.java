package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jetbrains.annotations.Contract;

public class ChunkLevel {
	private static final int FULL_CHUNK_LEVEL = 33;
	private static final int BLOCK_TICKING_LEVEL = 32;
	private static final int ENTITY_TICKING_LEVEL = 31;
	private static final ChunkStep FULL_CHUNK_STEP = ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL);
	public static final int RADIUS_AROUND_FULL_CHUNK = FULL_CHUNK_STEP.accumulatedDependencies().getRadius();
	public static final int MAX_LEVEL = 33 + RADIUS_AROUND_FULL_CHUNK;

	@Nullable
	public static ChunkStatus generationStatus(int i) {
		return getStatusAroundFullChunk(i - 33, null);
	}

	@Nullable
	@Contract("_,!null->!null;_,_->_")
	public static ChunkStatus getStatusAroundFullChunk(int i, @Nullable ChunkStatus chunkStatus) {
		if (i > RADIUS_AROUND_FULL_CHUNK) {
			return chunkStatus;
		} else {
			return i <= 0 ? ChunkStatus.FULL : FULL_CHUNK_STEP.accumulatedDependencies().get(i);
		}
	}

	public static ChunkStatus getStatusAroundFullChunk(int i) {
		return getStatusAroundFullChunk(i, ChunkStatus.EMPTY);
	}

	public static int byStatus(ChunkStatus chunkStatus) {
		return 33 + FULL_CHUNK_STEP.getAccumulatedRadiusOf(chunkStatus);
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
