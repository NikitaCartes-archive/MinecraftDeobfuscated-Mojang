/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class PathNavigationRegion
implements BlockGetter,
CollisionGetter {
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;

    public PathNavigationRegion(Level level, BlockPos blockPos, BlockPos blockPos2) {
        int l;
        int k;
        this.level = level;
        this.centerX = blockPos.getX() >> 4;
        this.centerZ = blockPos.getZ() >> 4;
        int i = blockPos2.getX() >> 4;
        int j = blockPos2.getZ() >> 4;
        this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
        ChunkSource chunkSource = level.getChunkSource();
        this.allEmpty = true;
        for (k = this.centerX; k <= i; ++k) {
            for (l = this.centerZ; l <= j; ++l) {
                this.chunks[k - this.centerX][l - this.centerZ] = chunkSource.getChunkNow(k, l);
            }
        }
        for (k = blockPos.getX() >> 4; k <= blockPos2.getX() >> 4; ++k) {
            for (l = blockPos.getZ() >> 4; l <= blockPos2.getZ() >> 4; ++l) {
                ChunkAccess chunkAccess = this.chunks[k - this.centerX][l - this.centerZ];
                if (chunkAccess == null || chunkAccess.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) continue;
                this.allEmpty = false;
                return;
            }
        }
    }

    private ChunkAccess getChunk(BlockPos blockPos) {
        return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    private ChunkAccess getChunk(int i, int j) {
        int k = i - this.centerX;
        int l = j - this.centerZ;
        if (k < 0 || k >= this.chunks.length || l < 0 || l >= this.chunks[k].length) {
            return new EmptyLevelChunk(this.level, new ChunkPos(i, j));
        }
        ChunkAccess chunkAccess = this.chunks[k][l];
        return chunkAccess != null ? chunkAccess : new EmptyLevelChunk(this.level, new ChunkPos(i, j));
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int i, int j) {
        return this.getChunk(i, j);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        return chunkAccess.getBlockEntity(blockPos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        return chunkAccess.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        return chunkAccess.getFluidState(blockPos);
    }
}

