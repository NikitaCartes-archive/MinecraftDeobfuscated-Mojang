package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtoChunk implements ChunkAccess {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ChunkPos chunkPos;
	private volatile boolean isDirty;
	@Nullable
	private ChunkBiomeContainer biomes;
	@Nullable
	private volatile LevelLightEngine lightEngine;
	private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
	private volatile ChunkStatus status = ChunkStatus.EMPTY;
	private final Map<BlockPos, BlockEntity> blockEntities = Maps.<BlockPos, BlockEntity>newHashMap();
	private final Map<BlockPos, CompoundTag> blockEntityNbts = Maps.<BlockPos, CompoundTag>newHashMap();
	private final LevelChunkSection[] sections;
	private final List<CompoundTag> entities = Lists.<CompoundTag>newArrayList();
	private final List<BlockPos> lights = Lists.<BlockPos>newArrayList();
	private final ShortList[] postProcessing;
	private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.<StructureFeature<?>, StructureStart<?>>newHashMap();
	private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.<StructureFeature<?>, LongSet>newHashMap();
	private final UpgradeData upgradeData;
	private final ProtoTickList<Block> blockTicks;
	private final ProtoTickList<Fluid> liquidTicks;
	private final LevelHeightAccessor levelHeightAccessor;
	private long inhabitedTime;
	private final Map<GenerationStep.Carving, BitSet> carvingMasks = new Object2ObjectArrayMap<>();
	private volatile boolean isLightCorrect;

	public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor) {
		this(
			chunkPos,
			upgradeData,
			null,
			new ProtoTickList<>(block -> block == null || block.defaultBlockState().isAir(), chunkPos, levelHeightAccessor),
			new ProtoTickList<>(fluid -> fluid == null || fluid == Fluids.EMPTY, chunkPos, levelHeightAccessor),
			levelHeightAccessor
		);
	}

	public ProtoChunk(
		ChunkPos chunkPos,
		UpgradeData upgradeData,
		@Nullable LevelChunkSection[] levelChunkSections,
		ProtoTickList<Block> protoTickList,
		ProtoTickList<Fluid> protoTickList2,
		LevelHeightAccessor levelHeightAccessor
	) {
		this.chunkPos = chunkPos;
		this.upgradeData = upgradeData;
		this.blockTicks = protoTickList;
		this.liquidTicks = protoTickList2;
		this.levelHeightAccessor = levelHeightAccessor;
		this.sections = new LevelChunkSection[levelHeightAccessor.getSectionsCount()];
		if (levelChunkSections != null) {
			if (this.sections.length == levelChunkSections.length) {
				System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
			} else {
				LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", levelChunkSections.length, this.sections.length);
			}
		}

		this.postProcessing = new ShortList[levelHeightAccessor.getSectionsCount()];
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		int i = blockPos.getY();
		if (this.isOutsideBuildHeight(i)) {
			return Blocks.VOID_AIR.defaultBlockState();
		} else {
			LevelChunkSection levelChunkSection = this.getSections()[this.getSectionIndex(i)];
			return LevelChunkSection.isEmpty(levelChunkSection)
				? Blocks.AIR.defaultBlockState()
				: levelChunkSection.getBlockState(blockPos.getX() & 15, i & 15, blockPos.getZ() & 15);
		}
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		int i = blockPos.getY();
		if (this.isOutsideBuildHeight(i)) {
			return Fluids.EMPTY.defaultFluidState();
		} else {
			LevelChunkSection levelChunkSection = this.getSections()[this.getSectionIndex(i)];
			return LevelChunkSection.isEmpty(levelChunkSection)
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
			if (this.sections[l] == LevelChunk.EMPTY_SECTION && blockState.is(Blocks.AIR)) {
				return blockState;
			} else {
				if (blockState.getLightEmission() > 0) {
					this.lights.add(new BlockPos((i & 15) + this.getPos().getMinBlockX(), j, (k & 15) + this.getPos().getMinBlockZ()));
				}

				LevelChunkSection levelChunkSection = this.getOrCreateSection(l);
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

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet(this.blockEntityNbts.keySet());
		set.addAll(this.blockEntities.keySet());
		return set;
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

	public List<CompoundTag> getEntities() {
		return this.entities;
	}

	public void setBiomes(ChunkBiomeContainer chunkBiomeContainer) {
		this.biomes = chunkBiomeContainer;
	}

	@Nullable
	@Override
	public ChunkBiomeContainer getBiomes() {
		return this.biomes;
	}

	@Override
	public void setUnsaved(boolean bl) {
		this.isDirty = bl;
	}

	@Override
	public boolean isUnsaved() {
		return this.isDirty;
	}

	@Override
	public ChunkStatus getStatus() {
		return this.status;
	}

	public void setStatus(ChunkStatus chunkStatus) {
		this.status = chunkStatus;
		this.setUnsaved(true);
	}

	@Override
	public LevelChunkSection[] getSections() {
		return this.sections;
	}

	@Override
	public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return Collections.unmodifiableSet(this.heightmaps.entrySet());
	}

	@Override
	public void setHeightmap(Heightmap.Types types, long[] ls) {
		this.getOrCreateHeightmapUnprimed(types).setRawData(ls);
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
		return (Heightmap)this.heightmaps.computeIfAbsent(types, typesx -> new Heightmap(this, typesx));
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		Heightmap heightmap = (Heightmap)this.heightmaps.get(types);
		if (heightmap == null) {
			Heightmap.primeHeightmaps(this, EnumSet.of(types));
			heightmap = (Heightmap)this.heightmaps.get(types);
		}

		return heightmap.getFirstAvailable(i & 15, j & 15) - 1;
	}

	@Override
	public ChunkPos getPos() {
		return this.chunkPos;
	}

	@Nullable
	@Override
	public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
		return (StructureStart<?>)this.structureStarts.get(structureFeature);
	}

	@Override
	public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
		this.structureStarts.put(structureFeature, structureStart);
		this.isDirty = true;
	}

	@Override
	public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
		return Collections.unmodifiableMap(this.structureStarts);
	}

	@Override
	public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map) {
		this.structureStarts.clear();
		this.structureStarts.putAll(map);
		this.isDirty = true;
	}

	@Override
	public LongSet getReferencesForFeature(StructureFeature<?> structureFeature) {
		return (LongSet)this.structuresRefences.computeIfAbsent(structureFeature, structureFeaturex -> new LongOpenHashSet());
	}

	@Override
	public void addReferenceForFeature(StructureFeature<?> structureFeature, long l) {
		((LongSet)this.structuresRefences.computeIfAbsent(structureFeature, structureFeaturex -> new LongOpenHashSet())).add(l);
		this.isDirty = true;
	}

	@Override
	public Map<StructureFeature<?>, LongSet> getAllReferences() {
		return Collections.unmodifiableMap(this.structuresRefences);
	}

	@Override
	public void setAllReferences(Map<StructureFeature<?>, LongSet> map) {
		this.structuresRefences.clear();
		this.structuresRefences.putAll(map);
		this.isDirty = true;
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
	public ShortList[] getPostProcessing() {
		return this.postProcessing;
	}

	@Override
	public void addPackedPostProcess(short s, int i) {
		ChunkAccess.getOrCreateOffsetList(this.postProcessing, i).add(s);
	}

	public ProtoTickList<Block> getBlockTicks() {
		return this.blockTicks;
	}

	public ProtoTickList<Fluid> getLiquidTicks() {
		return this.liquidTicks;
	}

	@Override
	public UpgradeData getUpgradeData() {
		return this.upgradeData;
	}

	@Override
	public void setInhabitedTime(long l) {
		this.inhabitedTime = l;
	}

	@Override
	public long getInhabitedTime() {
		return this.inhabitedTime;
	}

	@Override
	public void setBlockEntityNbt(CompoundTag compoundTag) {
		this.blockEntityNbts.put(new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z")), compoundTag);
	}

	public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
		return Collections.unmodifiableMap(this.blockEntityNbts);
	}

	@Override
	public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
		return (CompoundTag)this.blockEntityNbts.get(blockPos);
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
		BlockEntity blockEntity = this.getBlockEntity(blockPos);
		return blockEntity != null ? blockEntity.save(new CompoundTag()) : (CompoundTag)this.blockEntityNbts.get(blockPos);
	}

	@Override
	public void removeBlockEntity(BlockPos blockPos) {
		this.blockEntities.remove(blockPos);
		this.blockEntityNbts.remove(blockPos);
	}

	@Nullable
	public BitSet getCarvingMask(GenerationStep.Carving carving) {
		return (BitSet)this.carvingMasks.get(carving);
	}

	public BitSet getOrCreateCarvingMask(GenerationStep.Carving carving) {
		return (BitSet)this.carvingMasks.computeIfAbsent(carving, carvingx -> new BitSet(98304));
	}

	public void setCarvingMask(GenerationStep.Carving carving, BitSet bitSet) {
		this.carvingMasks.put(carving, bitSet);
	}

	public void setLightEngine(LevelLightEngine levelLightEngine) {
		this.lightEngine = levelLightEngine;
	}

	@Override
	public boolean isLightCorrect() {
		return this.isLightCorrect;
	}

	@Override
	public void setLightCorrect(boolean bl) {
		this.isLightCorrect = bl;
		this.setUnsaved(true);
	}

	@Override
	public int getMinBuildHeight() {
		return this.levelHeightAccessor.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return this.levelHeightAccessor.getHeight();
	}
}
