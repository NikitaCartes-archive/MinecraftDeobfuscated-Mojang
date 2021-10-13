package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
	@Nullable
	protected static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int i, int j) {
		boolean bl = serverLevel.dimensionType().hasCeiling();
		LevelChunk levelChunk = serverLevel.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
		int k = bl ? serverLevel.getChunkSource().getGenerator().getSpawnHeight(serverLevel) : levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 15, j & 15);
		if (k < serverLevel.getMinBuildHeight()) {
			return null;
		} else {
			int l = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 15, j & 15);
			if (l <= k && l > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 15, j & 15)) {
				return null;
			} else {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int m = k + 1; m >= serverLevel.getMinBuildHeight(); m--) {
					mutableBlockPos.set(i, m, j);
					BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
					if (!blockState.getFluidState().isEmpty()) {
						break;
					}

					if (Block.isFaceFull(blockState.getCollisionShape(serverLevel, mutableBlockPos), Direction.UP)) {
						return mutableBlockPos.above().immutable();
					}
				}

				return null;
			}
		}
	}

	@Nullable
	public static BlockPos getSpawnPosInChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
		if (SharedConstants.debugVoidTerrain(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ())) {
			return null;
		} else {
			for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); i++) {
				for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); j++) {
					BlockPos blockPos = getOverworldRespawnPos(serverLevel, i, j);
					if (blockPos != null) {
						return blockPos;
					}
				}
			}

			return null;
		}
	}
}
