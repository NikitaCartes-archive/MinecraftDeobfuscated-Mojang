package net.minecraft.world.level;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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

public class PathNavigationRegion implements CollisionGetter {
	protected final int centerX;
	protected final int centerZ;
	protected final ChunkAccess[][] chunks;
	protected boolean allEmpty;
	protected final Level level;
	private final Supplier<Holder<Biome>> plains;

	public PathNavigationRegion(Level level, BlockPos blockPos, BlockPos blockPos2) {
		this.level = level;
		this.plains = Suppliers.memoize(() -> level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
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
			return (ChunkAccess)(chunkAccess != null ? chunkAccess : new EmptyLevelChunk(this.level, new ChunkPos(i, j), (Holder<Biome>)this.plains.get()));
		} else {
			return new EmptyLevelChunk(this.level, new ChunkPos(i, j), (Holder<Biome>)this.plains.get());
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

	@Override
	public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB) {
		return List.of();
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
	public FluidState getFluidState(BlockPos blockPos) {
		if (this.isOutsideBuildHeight(blockPos)) {
			return Fluids.EMPTY.defaultFluidState();
		} else {
			ChunkAccess chunkAccess = this.getChunk(blockPos);
			return chunkAccess.getFluidState(blockPos);
		}
	}

	@Override
	public int getMinY() {
		return this.level.getMinY();
	}

	@Override
	public int getHeight() {
		return this.level.getHeight();
	}

	public ProfilerFiller getProfiler() {
		return this.level.getProfiler();
	}
}
