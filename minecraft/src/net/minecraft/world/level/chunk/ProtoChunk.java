package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunk extends ChunkAccess {
	@Nullable
	private volatile LevelLightEngine lightEngine;
	private volatile ChunkStatus status = ChunkStatus.EMPTY;
	private final List<CompoundTag> entities = Lists.<CompoundTag>newArrayList();
	private final List<BlockPos> lights = Lists.<BlockPos>newArrayList();
	private final Map<GenerationStep.Carving, CarvingMask> carvingMasks = new Object2ObjectArrayMap<>();
	@Nullable
	private BelowZeroRetrogen belowZeroRetrogen;
	private final ProtoChunkTicks<Block> blockTicks;
	private final ProtoChunkTicks<Fluid> fluidTicks;

	public ProtoChunk(
		ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, @Nullable BlendingData blendingData
	) {
		this(chunkPos, upgradeData, null, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), levelHeightAccessor, registry, blendingData);
	}

	public ProtoChunk(
		ChunkPos chunkPos,
		UpgradeData upgradeData,
		@Nullable LevelChunkSection[] levelChunkSections,
		ProtoChunkTicks<Block> protoChunkTicks,
		ProtoChunkTicks<Fluid> protoChunkTicks2,
		LevelHeightAccessor levelHeightAccessor,
		Registry<Biome> registry,
		@Nullable BlendingData blendingData
	) {
		super(chunkPos, upgradeData, levelHeightAccessor, registry, 0L, levelChunkSections, blendingData);
		this.blockTicks = protoChunkTicks;
		this.fluidTicks = protoChunkTicks2;
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		return this.blockTicks;
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return this.fluidTicks;
	}

	@Override
	public ChunkAccess.TicksToSave getTicksForSerialization() {
		return new ChunkAccess.TicksToSave(this.blockTicks, this.fluidTicks);
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		int i = blockPos.getY();
		if (this.isOutsideBuildHeight(i)) {
			return Blocks.VOID_AIR.defaultBlockState();
		} else {
			LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(i));
			return levelChunkSection.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : levelChunkSection.getBlockState(blockPos.getX() & 15, i & 15, blockPos.getZ() & 15);
		}
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		int i = blockPos.getY();
		if (this.isOutsideBuildHeight(i)) {
			return Fluids.EMPTY.defaultFluidState();
		} else {
			LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(i));
			return levelChunkSection.hasOnlyAir()
				? Fluids.EMPTY.defaultFluidState()
				: levelChunkSection.getFluidState(blockPos.getX() & 15, i & 15, blockPos.getZ() & 15);
		}
	}

	@Override
	public Stream<BlockPos> getLights() {
		return this.lights.stream();
	}

	public ShortList[] getPackedLights() {
		ShortList[] shortLists = new ShortList[this.getSectionsCount()];

		for (BlockPos blockPos : this.lights) {
			ChunkAccess.getOrCreateOffsetList(shortLists, this.getSectionIndex(blockPos.getY())).add(packOffsetCoordinates(blockPos));
		}

		return shortLists;
	}

	public void addLight(short s, int i) {
		this.addLight(unpackOffsetCoordinates(s, this.getSectionYFromSectionIndex(i), this.chunkPos));
	}

	public void addLight(BlockPos blockPos) {
		this.lights.add(blockPos.immutable());
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		if (j >= this.getMinBuildHeight() && j < this.getMaxBuildHeight()) {
			int l = this.getSectionIndex(j);
			if (this.sections[l].hasOnlyAir() && blockState.is(Blocks.AIR)) {
				return blockState;
			} else {
				if (blockState.getLightEmission() > 0) {
					this.lights.add(new BlockPos((i & 15) + this.getPos().getMinBlockX(), j, (k & 15) + this.getPos().getMinBlockZ()));
				}

				LevelChunkSection levelChunkSection = this.getSection(l);
				BlockState blockState2 = levelChunkSection.setBlockState(i & 15, j & 15, k & 15, blockState);
				if (this.status.isOrAfter(ChunkStatus.FEATURES)
					&& blockState != blockState2
					&& (
						blockState.getLightBlock(this, blockPos) != blockState2.getLightBlock(this, blockPos)
							|| blockState.getLightEmission() != blockState2.getLightEmission()
							|| blockState.useShapeForLightOcclusion()
							|| blockState2.useShapeForLightOcclusion()
					)) {
					this.lightEngine.checkBlock(blockPos);
				}

				EnumSet<Heightmap.Types> enumSet = this.getStatus().heightmapsAfter();
				EnumSet<Heightmap.Types> enumSet2 = null;

				for (Heightmap.Types types : enumSet) {
					Heightmap heightmap = (Heightmap)this.heightmaps.get(types);
					if (heightmap == null) {
						if (enumSet2 == null) {
							enumSet2 = EnumSet.noneOf(Heightmap.Types.class);
						}

						enumSet2.add(types);
					}
				}

				if (enumSet2 != null) {
					Heightmap.primeHeightmaps(this, enumSet2);
				}

				for (Heightmap.Types typesx : enumSet) {
					((Heightmap)this.heightmaps.get(typesx)).update(i & 15, j, k & 15, blockState);
				}

				return blockState2;
			}
		} else {
			return Blocks.VOID_AIR.defaultBlockState();
		}
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		this.blockEntities.put(blockEntity.getBlockPos(), blockEntity);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return (BlockEntity)this.blockEntities.get(blockPos);
	}

	public Map<BlockPos, BlockEntity> getBlockEntities() {
		return this.blockEntities;
	}

	public void addEntity(CompoundTag compoundTag) {
		this.entities.add(compoundTag);
	}

	@Override
	public void addEntity(Entity entity) {
		if (!entity.isPassenger()) {
			CompoundTag compoundTag = new CompoundTag();
			entity.save(compoundTag);
			this.addEntity(compoundTag);
		}
	}

	@Override
	public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
		BelowZeroRetrogen belowZeroRetrogen = this.getBelowZeroRetrogen();
		if (belowZeroRetrogen != null && structureStart.isValid()) {
			BoundingBox boundingBox = structureStart.getBoundingBox();
			LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();
			if (boundingBox.minY() < levelHeightAccessor.getMinBuildHeight() || boundingBox.maxY() >= levelHeightAccessor.getMaxBuildHeight()) {
				return;
			}
		}

		super.setStartForFeature(structureFeature, structureStart);
	}

	public List<CompoundTag> getEntities() {
		return this.entities;
	}

	@Override
	public ChunkStatus getStatus() {
		return this.status;
	}

	public void setStatus(ChunkStatus chunkStatus) {
		this.status = chunkStatus;
		if (this.belowZeroRetrogen != null && chunkStatus.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
			this.setBelowZeroRetrogen(null);
		}

		this.setUnsaved(true);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		if (!this.getStatus().isOrAfter(ChunkStatus.BIOMES)
			&& (this.belowZeroRetrogen == null || !this.belowZeroRetrogen.targetStatus().isOrAfter(ChunkStatus.BIOMES))) {
			throw new IllegalStateException("Asking for biomes before we have biomes");
		} else {
			return super.getNoiseBiome(i, j, k);
		}
	}

	public static short packOffsetCoordinates(BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		int l = i & 15;
		int m = j & 15;
		int n = k & 15;
		return (short)(l | m << 4 | n << 8);
	}

	public static BlockPos unpackOffsetCoordinates(short s, int i, ChunkPos chunkPos) {
		int j = SectionPos.sectionToBlockCoord(chunkPos.x, s & 15);
		int k = SectionPos.sectionToBlockCoord(i, s >>> 4 & 15);
		int l = SectionPos.sectionToBlockCoord(chunkPos.z, s >>> 8 & 15);
		return new BlockPos(j, k, l);
	}

	@Override
	public void markPosForPostprocessing(BlockPos blockPos) {
		if (!this.isOutsideBuildHeight(blockPos)) {
			ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(blockPos.getY())).add(packOffsetCoordinates(blockPos));
		}
	}

	@Override
	public void addPackedPostProcess(short s, int i) {
		ChunkAccess.getOrCreateOffsetList(this.postProcessing, i).add(s);
	}

	public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
		return Collections.unmodifiableMap(this.pendingBlockEntities);
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
		BlockEntity blockEntity = this.getBlockEntity(blockPos);
		return blockEntity != null ? blockEntity.saveWithFullMetadata() : (CompoundTag)this.pendingBlockEntities.get(blockPos);
	}

	@Override
	public void removeBlockEntity(BlockPos blockPos) {
		this.blockEntities.remove(blockPos);
		this.pendingBlockEntities.remove(blockPos);
	}

	@Nullable
	public CarvingMask getCarvingMask(GenerationStep.Carving carving) {
		return (CarvingMask)this.carvingMasks.get(carving);
	}

	public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving carving) {
		return (CarvingMask)this.carvingMasks.computeIfAbsent(carving, carvingx -> new CarvingMask(this.getHeight(), this.getMinBuildHeight()));
	}

	public void setCarvingMask(GenerationStep.Carving carving, CarvingMask carvingMask) {
		this.carvingMasks.put(carving, carvingMask);
	}

	public void setLightEngine(LevelLightEngine levelLightEngine) {
		this.lightEngine = levelLightEngine;
	}

	public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen) {
		this.belowZeroRetrogen = belowZeroRetrogen;
	}

	@Nullable
	@Override
	public BelowZeroRetrogen getBelowZeroRetrogen() {
		return this.belowZeroRetrogen;
	}

	private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> protoChunkTicks) {
		return new LevelChunkTicks<>(protoChunkTicks.scheduledTicks());
	}

	public LevelChunkTicks<Block> unpackBlockTicks() {
		return unpackTicks(this.blockTicks);
	}

	public LevelChunkTicks<Fluid> unpackFluidTicks() {
		return unpackTicks(this.fluidTicks);
	}

	@Override
	public LevelHeightAccessor getHeightAccessorForGeneration() {
		return (LevelHeightAccessor)(this.isUpgrading() ? BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR : this);
	}
}
