package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class ImposterProtoChunk extends ProtoChunk {
	private final LevelChunk wrapped;
	private final boolean allowWrites;

	public ImposterProtoChunk(LevelChunk levelChunk, boolean bl) {
		super(
			levelChunk.getPos(),
			UpgradeData.EMPTY,
			levelChunk.levelHeightAccessor,
			levelChunk.getLevel().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
			levelChunk.getBlendingData()
		);
		this.wrapped = levelChunk;
		this.allowWrites = bl;
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
	public LevelChunkSection getSection(int i) {
		return this.allowWrites ? this.wrapped.getSection(i) : super.getSection(i);
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		return this.allowWrites ? this.wrapped.setBlockState(blockPos, blockState, bl) : null;
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		if (this.allowWrites) {
			this.wrapped.setBlockEntity(blockEntity);
		}
	}

	@Override
	public void addEntity(Entity entity) {
		if (this.allowWrites) {
			this.wrapped.addEntity(entity);
		}
	}

	@Override
	public void setStatus(ChunkStatus chunkStatus) {
		if (this.allowWrites) {
			super.setStatus(chunkStatus);
		}
	}

	@Override
	public LevelChunkSection[] getSections() {
		return this.wrapped.getSections();
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
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
		return this.wrapped.getOrCreateHeightmapUnprimed(types);
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		return this.wrapped.getHeight(this.fixType(types), i, j);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return this.wrapped.getNoiseBiome(i, j, k);
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
	public TickContainerAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public ChunkAccess.TicksToSave getTicksForSerialization() {
		return this.wrapped.getTicksForSerialization();
	}

	@Nullable
	@Override
	public BlendingData getBlendingData() {
		return this.wrapped.getBlendingData();
	}

	@Override
	public void setBlendingData(BlendingData blendingData) {
		this.wrapped.setBlendingData(blendingData);
	}

	@Override
	public CarvingMask getCarvingMask(GenerationStep.Carving carving) {
		if (this.allowWrites) {
			return super.getCarvingMask(carving);
		} else {
			throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
		}
	}

	@Override
	public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving carving) {
		if (this.allowWrites) {
			return super.getOrCreateCarvingMask(carving);
		} else {
			throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
		}
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

	@Override
	public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
		if (this.allowWrites) {
			this.wrapped.fillBiomesFromNoise(biomeResolver, sampler);
		}
	}
}
