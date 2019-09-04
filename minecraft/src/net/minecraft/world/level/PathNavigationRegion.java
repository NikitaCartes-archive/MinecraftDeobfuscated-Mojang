package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class PathNavigationRegion implements BlockGetter, CollisionGetter {
	protected final int centerX;
	protected final int centerZ;
	protected final ChunkAccess[][] chunks;
	protected boolean allEmpty;
	protected final Level level;

	public PathNavigationRegion(Level level, BlockPos blockPos, BlockPos blockPos2) {
		this.level = level;
		this.centerX = blockPos.getX() >> 4;
		this.centerZ = blockPos.getZ() >> 4;
		int i = blockPos2.getX() >> 4;
		int j = blockPos2.getZ() >> 4;
		this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
		this.allEmpty = true;

		for (int k = this.centerX; k <= i; k++) {
			for (int l = this.centerZ; l <= j; l++) {
				this.chunks[k - this.centerX][l - this.centerZ] = level.getChunk(k, l, ChunkStatus.FULL, false);
			}
		}

		for (int k = blockPos.getX() >> 4; k <= blockPos2.getX() >> 4; k++) {
			for (int l = blockPos.getZ() >> 4; l <= blockPos2.getZ() >> 4; l++) {
				ChunkAccess chunkAccess = this.chunks[k - this.centerX][l - this.centerZ];
				if (chunkAccess != null && !chunkAccess.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) {
					this.allEmpty = false;
					return;
				}
			}
		}
	}

	private ChunkAccess getChunk(BlockPos blockPos) {
		return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	private ChunkAccess getChunk(int i, int j) {
		int k = i - this.centerX;
		int l = j - this.centerZ;
		if (k >= 0 && k < this.chunks.length && l >= 0 && l < this.chunks[k].length) {
			ChunkAccess chunkAccess = this.chunks[k][l];
			return (ChunkAccess)(chunkAccess != null ? chunkAccess : new EmptyLevelChunk(this.level, new ChunkPos(i, j)));
		} else {
			return new EmptyLevelChunk(this.level, new ChunkPos(i, j));
		}
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.level.getWorldBorder();
	}

	@Override
	public BlockGetter getChunkForCollisions(int i, int j) {
		return this.getChunk(i, j);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		ChunkAccess chunkAccess = this.getChunk(blockPos);
		return chunkAccess.getBlockEntity(blockPos);
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		if (Level.isOutsideBuildHeight(blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			ChunkAccess chunkAccess = this.getChunk(blockPos);
			return chunkAccess.getBlockState(blockPos);
		}
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		if (Level.isOutsideBuildHeight(blockPos)) {
			return Fluids.EMPTY.defaultFluidState();
		} else {
			ChunkAccess chunkAccess = this.getChunk(blockPos);
			return chunkAccess.getFluidState(blockPos);
		}
	}
}
