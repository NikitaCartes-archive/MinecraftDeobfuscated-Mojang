/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
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
    protected static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int i, int j, boolean bl) {
        int k;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, serverLevel.getMinBuildHeight(), j);
        Biome biome = serverLevel.getBiome(mutableBlockPos);
        boolean bl2 = serverLevel.dimensionType().hasCeiling();
        BlockState blockState = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
        if (bl && !blockState.is(BlockTags.VALID_SPAWN)) {
            return null;
        }
        LevelChunk levelChunk = serverLevel.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
        int n = k = bl2 ? serverLevel.getChunkSource().getGenerator().getSpawnHeight() : levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 0xF, j & 0xF);
        if (k < serverLevel.getMinBuildHeight()) {
            return null;
        }
        int l = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 0xF, j & 0xF);
        if (l <= k && l > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 0xF, j & 0xF)) {
            return null;
        }
        for (int m = k + 1; m >= serverLevel.getMinBuildHeight(); --m) {
            mutableBlockPos.set(i, m, j);
            BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
            if (!blockState2.getFluidState().isEmpty()) break;
            if (!blockState2.equals(blockState)) continue;
            return ((BlockPos)mutableBlockPos.above()).immutable();
        }
        return null;
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
}

