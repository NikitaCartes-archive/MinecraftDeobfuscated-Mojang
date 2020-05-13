/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public class NormalDimension
extends Dimension {
    public NormalDimension(Level level, DimensionType dimensionType) {
        super(level, dimensionType, 0.0f);
    }

    @Override
    public DimensionType getType() {
        return DimensionType.OVERWORLD;
    }

    @Override
    @Nullable
    public BlockPos getSpawnPosInChunk(long l, ChunkPos chunkPos, boolean bl) {
        for (int i = chunkPos.getMinBlockX(); i <= chunkPos.getMaxBlockX(); ++i) {
            for (int j = chunkPos.getMinBlockZ(); j <= chunkPos.getMaxBlockZ(); ++j) {
                BlockPos blockPos = this.getValidSpawnPosition(l, i, j, bl);
                if (blockPos == null) continue;
                return blockPos;
            }
        }
        return null;
    }

    @Override
    @Nullable
    public BlockPos getValidSpawnPosition(long l, int i, int j, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, 0, j);
        Biome biome = this.level.getBiome(mutableBlockPos);
        BlockState blockState = biome.getSurfaceBuilderConfig().getTopMaterial();
        if (bl && !blockState.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        }
        LevelChunk levelChunk = this.level.getChunk(i >> 4, j >> 4);
        int k = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 0xF, j & 0xF);
        if (k < 0) {
            return null;
        }
        if (levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 0xF, j & 0xF) > levelChunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 0xF, j & 0xF)) {
            return null;
        }
        for (int m = k + 1; m >= 0; --m) {
            mutableBlockPos.set(i, m, j);
            BlockState blockState2 = this.level.getBlockState(mutableBlockPos);
            if (!blockState2.getFluidState().isEmpty()) break;
            if (!blockState2.equals(blockState)) continue;
            return mutableBlockPos.above().immutable();
        }
        return null;
    }

    @Override
    public float getTimeOfDay(long l, float f) {
        double d = Mth.frac((double)l / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float)(d * 2.0 + e) / 3.0f;
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @Override
    public boolean mayRespawn() {
        return true;
    }
}

