package net.minecraft.world.level;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion implements BlockGetter, CollisionGetter {
	protected final int centerX;
	protected final int centerZ;
	protected final ChunkAccess[][] chunks;
	protected boolean allEmpty;
	protected final Level level;

	public PathNavigationRegion(Level level, BlockPos blockPos, BlockPos blockPos2) {
		this.level = level;
		this.centerX = SectionPos.blockToSectionCoord(blockPos.getX());
		this.centerZ = SectionPos.blockToSectionCoord(blockPos.getZ());
		int i = SectionPos.blockToSectionCoord(blockPos2.getX());
		int j = SectionPos.blockToSectionCoord(blockPos2.getZ());
		this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
		ChunkSource chunkSource = level.getChunkSource();
		this.allEmpty = true;

		for (int k = this.centerX; k <= i; k++) {
			for (int l = this.centerZ; l <= j; l++) {
				this.chunks[k - this.centerX][l - this.centerZ] = chunkSource.getChunkNow(k, l);
			}
		}

		for (int k = SectionPos.blockToSectionCoord(blockPos.getX()); k <= SectionPos.blockToSectionCoord(blockPos2.getX()); k++) {
			for (int l = SectionPos.blockToSectionCoord(blockPos.getZ()); l <= SectionPos.blockToSectionCoord(blockPos2.getZ()); l++) {
				ChunkAccess chunkAccess = this.chunks[k - this.centerX][l - this.centerZ];
				if (chunkAccess != null && !chunkAccess.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) {
					this.allEmpty = false;
					return;
				}
			}
		}
	}

	private ChunkAccess getChunk(BlockPos blockPos) {
		return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
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
		if (this.isOutsideBuildHeight(blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			ChunkAccess chunkAccess = this.getChunk(blockPos);
			return chunkAccess.getBlockState(blockPos);
		}
	}

	@Override
	public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return Stream.empty();
	}

	@Override
	public Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return this.getBlockCollisions(entity, aABB);
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		if (this.isOutsideBuildHeight(blockPos)) {
			return Fluids.EMPTY.defaultFluidState();
		} else {
			ChunkAccess chunkAccess = this.getChunk(blockPos);
			return chunkAccess.getFluidState(blockPos);
		}
	}

	@Override
	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return this.level.getHeight();
	}
}
