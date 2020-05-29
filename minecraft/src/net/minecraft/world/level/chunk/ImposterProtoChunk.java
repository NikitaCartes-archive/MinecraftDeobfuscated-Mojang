package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class ImposterProtoChunk extends ProtoChunk {
	private final LevelChunk wrapped;

	public ImposterProtoChunk(LevelChunk levelChunk) {
		super(levelChunk.getPos(), UpgradeData.EMPTY);
		this.wrapped = levelChunk;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return this.wrapped.getBlockEntity(blockPos);
	}

	@Nullable
	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return this.wrapped.getBlockState(blockPos);
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.wrapped.getFluidState(blockPos);
	}

	@Override
	public int getMaxLightLevel() {
		return this.wrapped.getMaxLightLevel();
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		return null;
	}

	@Override
	public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
	}

	@Override
	public void addEntity(Entity entity) {
	}

	@Override
	public void setStatus(ChunkStatus chunkStatus) {
	}

	@Override
	public LevelChunkSection[] getSections() {
		return this.wrapped.getSections();
	}

	@Nullable
	@Override
	public LevelLightEngine getLightEngine() {
		return this.wrapped.getLightEngine();
	}

	@Override
	public void setHeightmap(Heightmap.Types types, long[] ls) {
	}

	private Heightmap.Types fixType(Heightmap.Types types) {
		if (types == Heightmap.Types.WORLD_SURFACE_WG) {
			return Heightmap.Types.WORLD_SURFACE;
		} else {
			return types == Heightmap.Types.OCEAN_FLOOR_WG ? Heightmap.Types.OCEAN_FLOOR : types;
		}
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		return this.wrapped.getHeight(this.fixType(types), i, j);
	}

	@Override
	public ChunkPos getPos() {
		return this.wrapped.getPos();
	}

	@Override
	public void setLastSaveTime(long l) {
	}

	@Nullable
	@Override
	public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
		return this.wrapped.getStartForFeature(structureFeature);
	}

	@Override
	public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
	}

	@Override
	public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
		return this.wrapped.getAllStarts();
	}

	@Override
	public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map) {
	}

	@Override
	public LongSet getReferencesForFeature(StructureFeature<?> structureFeature) {
		return this.wrapped.getReferencesForFeature(structureFeature);
	}

	@Override
	public void addReferenceForFeature(StructureFeature<?> structureFeature, long l) {
	}

	@Override
	public Map<StructureFeature<?>, LongSet> getAllReferences() {
		return this.wrapped.getAllReferences();
	}

	@Override
	public void setAllReferences(Map<StructureFeature<?>, LongSet> map) {
	}

	@Override
	public ChunkBiomeContainer getBiomes() {
		return this.wrapped.getBiomes();
	}

	@Override
	public void setUnsaved(boolean bl) {
	}

	@Override
	public boolean isUnsaved() {
		return false;
	}

	@Override
	public ChunkStatus getStatus() {
		return this.wrapped.getStatus();
	}

	@Override
	public void removeBlockEntity(BlockPos blockPos) {
	}

	@Override
	public void markPosForPostprocessing(BlockPos blockPos) {
	}

	@Override
	public void setBlockEntityNbt(CompoundTag compoundTag) {
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
		return this.wrapped.getBlockEntityNbt(blockPos);
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
		return this.wrapped.getBlockEntityNbtForSaving(blockPos);
	}

	@Override
	public void setBiomes(ChunkBiomeContainer chunkBiomeContainer) {
	}

	@Override
	public Stream<BlockPos> getLights() {
		return this.wrapped.getLights();
	}

	@Override
	public ProtoTickList<Block> getBlockTicks() {
		return new ProtoTickList<>(block -> block.defaultBlockState().isAir(), this.getPos());
	}

	@Override
	public ProtoTickList<Fluid> getLiquidTicks() {
		return new ProtoTickList<>(fluid -> fluid == Fluids.EMPTY, this.getPos());
	}

	@Override
	public BitSet getCarvingMask(GenerationStep.Carving carving) {
		throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
	}

	@Override
	public BitSet getOrCreateCarvingMask(GenerationStep.Carving carving) {
		throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
	}

	public LevelChunk getWrapped() {
		return this.wrapped;
	}

	@Override
	public boolean isLightCorrect() {
		return this.wrapped.isLightCorrect();
	}

	@Override
	public void setLightCorrect(boolean bl) {
		this.wrapped.setLightCorrect(bl);
	}
}
