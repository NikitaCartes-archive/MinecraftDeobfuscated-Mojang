package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
	@Nullable
	protected static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int i, int j, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
		Biome biome = serverLevel.getBiome(mutableBlockPos);
		boolean bl2 = serverLevel.dimensionType().hasCeiling();
		BlockState blockState = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
		if (bl && !blockState.is(BlockTags.VALID_SPAWN)) {
			return null;
		} else {
			LevelChunk levelChunk = serverLevel.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
			int k = bl2 ? serverLevel.getChunkSource().getGenerator().getSpawnHeight() : levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 15, j & 15);
			if (k < serverLevel.getMinBuildHeight()) {
				return null;
			} else {
				int l = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 15, j & 15);
				if (l <= k && l > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 15, j & 15)) {
					return null;
				} else {
					for (int m = k + 1; m >= serverLevel.getMinBuildHeight(); m--) {
						mutableBlockPos.set(i, m, j);
						BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
						if (!blockState2.getFluidState().isEmpty()) {
							break;
						}

						if (blockState2.equals(blockState)) {
							return mutableBlockPos.above().immutable();
						}
					}

					return null;
				}
			}
		}
	}

	@Nullable
	public static BlockPos getSpawnPosInChunk(ServerLevel serverLevel, ChunkPos chunkPos, boolean bl) {
		for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); i++) {
			for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); j++) {
				BlockPos blockPos = getOverworldRespawnPos(serverLevel, i, j, bl);
				if (blockPos != null) {
					return blockPos;
				}
			}
		}

		return null;
	}
}
