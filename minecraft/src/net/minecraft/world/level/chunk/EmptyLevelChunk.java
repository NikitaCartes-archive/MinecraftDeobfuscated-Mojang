package net.minecraft.world.level.chunk;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyLevelChunk extends LevelChunk {
	private static final Biome[] BIOMES = Util.make(new Biome[ChunkBiomeContainer.BIOMES_SIZE], biomes -> Arrays.fill(biomes, Biomes.PLAINS));

	public EmptyLevelChunk(Level level, ChunkPos chunkPos) {
		super(level, chunkPos, new ChunkBiomeContainer(level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), BIOMES));
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

	@Nullable
	@Override
	public LevelLightEngine getLightEngine() {
		return null;
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
	public void markUnsaved() {
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
}
