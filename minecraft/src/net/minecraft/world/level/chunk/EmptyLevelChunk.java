package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyLevelChunk extends LevelChunk {
	private final Holder<Biome> biome;

	public EmptyLevelChunk(Level level, ChunkPos chunkPos, Holder<Biome> holder) {
		super(level, chunkPos);
		this.biome = holder;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return Blocks.VOID_AIR.defaultBlockState();
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		return null;
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public int getLightEmission(BlockPos blockPos) {
		return 0;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos, LevelChunk.EntityCreationType entityCreationType) {
		return null;
	}

	@Override
	public void addAndRegisterBlockEntity(BlockEntity blockEntity) {
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
	}

	@Override
	public void removeBlockEntity(BlockPos blockPos) {
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean isYSpaceEmpty(int i, int j) {
		return true;
	}

	@Override
	public ChunkHolder.FullChunkStatus getFullStatus() {
		return ChunkHolder.FullChunkStatus.BORDER;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k) {
		return this.biome;
	}
}
