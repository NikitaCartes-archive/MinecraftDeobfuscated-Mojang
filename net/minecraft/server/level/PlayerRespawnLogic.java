/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public class PlayerRespawnLogic {
    @Nullable
    private static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int i, int j, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
        Biome biome = serverLevel.getBiome(mutableBlockPos);
        BlockState blockState = biome.getSurfaceBuilderConfig().getTopMaterial();
        if (bl && !blockState.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        }
        LevelChunk levelChunk = serverLevel.getChunk(i >> 4, j >> 4);
        int k = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 0xF, j & 0xF);
        if (k < 0) {
            return null;
        }
        if (levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 0xF, j & 0xF) > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 0xF, j & 0xF)) {
            return null;
        }
        for (int l = k + 1; l >= 0; --l) {
            mutableBlockPos.set(i, l, j);
            BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
            if (!blockState2.getFluidState().isEmpty()) break;
            if (!blockState2.equals(blockState)) continue;
            return mutableBlockPos.above().immutable();
        }
        return null;
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
        for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); ++i) {
            for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); ++j) {
                BlockPos blockPos = PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, i, j, bl);
                if (blockPos == null) continue;
                return blockPos;
            }
        }
        return null;
    }

    @Nullable
    protected static BlockPos validSpawnPosition(ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k) {
        if (serverLevel.dimensionType().isOverworld()) {
            return PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, blockPos.getX() + j - i, blockPos.getZ() + k - i, false);
        }
        if (serverLevel.dimensionType().isEnd()) {
            return PlayerRespawnLogic.getEndRespawnPos(serverLevel, serverLevel.getSeed(), blockPos.getX() + j - i, blockPos.getZ() + k - i);
        }
        return null;
    }
}

