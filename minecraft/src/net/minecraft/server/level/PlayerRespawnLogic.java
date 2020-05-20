package net.minecraft.server.level;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
	@Nullable
	private static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int i, int j, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
		Biome biome = serverLevel.getBiome(mutableBlockPos);
		BlockState blockState = biome.getSurfaceBuilderConfig().getTopMaterial();
		if (bl && !blockState.getBlock().is(BlockTags.VALID_SPAWN)) {
			return null;
		} else {
			LevelChunk levelChunk = serverLevel.getChunk(i >> 4, j >> 4);
			int k = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 15, j & 15);
			if (k < 0) {
				return null;
			} else if (levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 15, j & 15) > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 15, j & 15)) {
				return null;
			} else {
				for (int l = k + 1; l >= 0; l--) {
					mutableBlockPos.set(i, l, j);
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

	@Nullable
	private static BlockPos getEndRespawnPos(ServerLevel serverLevel, long l, int i, int j) {
		ChunkPos chunkPos = new ChunkPos(i >> 4, j >> 4);
		Random random = new Random(l);
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX() + random.nextInt(15), 0, chunkPos.getMaxBlockZ() + random.nextInt(15));
		return serverLevel.getTopBlockState(blockPos).getMaterial().blocksMotion() ? blockPos : null;
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

	@Nullable
	protected static BlockPos validSpawnPosition(ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k) {
		if (serverLevel.dimensionType().isOverworld()) {
			return getOverworldRespawnPos(serverLevel, blockPos.getX() + j - i, blockPos.getZ() + k - i, false);
		} else {
			return serverLevel.dimensionType().isEnd() ? getEndRespawnPos(serverLevel, serverLevel.getSeed(), blockPos.getX() + j - i, blockPos.getZ() + k - i) : null;
		}
	}
}
