package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class RegenProtoChunk extends ProtoChunk {
	private final LevelChunk wrapped;

	public RegenProtoChunk(LevelChunk levelChunk) {
		super(levelChunk.getPos(), UpgradeData.EMPTY, levelChunk.getLevel());
		this.wrapped = levelChunk;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return this.wrapped.getBlockEntity(blockPos);
	}

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

	@Override
	public LevelChunkSection getOrCreateSection(int i) {
		return this.wrapped.getOrCreateSection(i);
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		return this.wrapped.setBlockState(blockPos, blockState, bl);
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		this.wrapped.setBlockEntity(blockEntity);
	}

	@Override
	public void addEntity(Entity entity) {
		this.wrapped.addEntity(entity);
	}

	@Override
	public LevelChunkSection[] getSections() {
		return this.wrapped.getSections();
	}

	@Override
	public void setHeightmap(Heightmap.Types types, long[] ls) {
		super.setHeightmap(types, ls);
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
		return super.getHeight(types, i, j);
	}

	@Override
	public BlockPos getHeighestPosition(Heightmap.Types types) {
		return super.getHeighestPosition(types);
	}

	@Override
	public ChunkPos getPos() {
		return this.wrapped.getPos();
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
	public Stream<BlockPos> getLights() {
		return this.wrapped.getLights();
	}

	@Override
	public ProtoTickList<Block> getBlockTicks() {
		return new ProtoTickList<>(block -> block.defaultBlockState().isAir(), this.getPos(), this.wrapped.getLevel());
	}

	@Override
	public ProtoTickList<Fluid> getLiquidTicks() {
		return new ProtoTickList<>(fluid -> fluid == Fluids.EMPTY, this.getPos(), this.wrapped.getLevel());
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
