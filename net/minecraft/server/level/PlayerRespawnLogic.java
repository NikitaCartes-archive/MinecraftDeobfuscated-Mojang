/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public class PlayerRespawnLogic {
    @Nullable
    protected static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int i, int j) {
        int k;
        boolean bl = serverLevel.dimensionType().hasCeiling();
        LevelChunk levelChunk = serverLevel.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
        int n = k = bl ? serverLevel.getChunkSource().getGenerator().getSpawnHeight(serverLevel) : levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 0xF, j & 0xF);
        if (k < serverLevel.getMinBuildHeight()) {
            return null;
        }
        int l = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 0xF, j & 0xF);
        if (l <= k && l > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 0xF, j & 0xF)) {
            return null;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int m = k + 1; m >= serverLevel.getMinBuildHeight(); --m) {
            mutableBlockPos.set(i, m, j);
            BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
            if (!blockState.getFluidState().isEmpty()) break;
            if (!Block.isFaceFull(blockState.getCollisionShape(serverLevel, mutableBlockPos), Direction.UP)) continue;
            return ((BlockPos)mutableBlockPos.above()).immutable();
        }
        return null;
    }

    @Nullable
    public static BlockPos getSpawnPosInChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
        if (SharedConstants.debugVoidTerrain(chunkPos)) {
            return null;
        }
        for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); ++i) {
            for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); ++j) {
                BlockPos blockPos = PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, i, j);
                if (blockPos == null) continue;
                return blockPos;
            }
        }
        return null;
    }
}

