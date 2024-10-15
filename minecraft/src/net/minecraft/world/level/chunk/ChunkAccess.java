package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public abstract class ChunkAccess implements BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess {
	public static final int NO_FILLED_SECTION = -1;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final LongSet EMPTY_REFERENCE_SET = new LongOpenHashSet();
	protected final ShortList[] postProcessing;
	private volatile boolean unsaved;
	private volatile boolean isLightCorrect;
	protected final ChunkPos chunkPos;
	private long inhabitedTime;
	@Nullable
	@Deprecated
	private BiomeGenerationSettings carverBiomeSettings;
	@Nullable
	protected NoiseChunk noiseChunk;
	protected final UpgradeData upgradeData;
	@Nullable
	protected BlendingData blendingData;
	protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
	protected ChunkSkyLightSources skyLightSources;
	private final Map<Structure, StructureStart> structureStarts = Maps.<Structure, StructureStart>newHashMap();
	private final Map<Structure, LongSet> structuresRefences = Maps.<Structure, LongSet>newHashMap();
	protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.<BlockPos, CompoundTag>newHashMap();
	protected final Map<BlockPos, BlockEntity> blockEntities = new Object2ObjectOpenHashMap<>();
	protected final LevelHeightAccessor levelHeightAccessor;
	protected final LevelChunkSection[] sections;

	public ChunkAccess(
		ChunkPos chunkPos,
		UpgradeData upgradeData,
		LevelHeightAccessor levelHeightAccessor,
		Registry<Biome> registry,
		long l,
		@Nullable LevelChunkSection[] levelChunkSections,
		@Nullable BlendingData blendingData
	) {
		this.chunkPos = chunkPos;
		this.upgradeData = upgradeData;
		this.levelHeightAccessor = levelHeightAccessor;
		this.sections = new LevelChunkSection[levelHeightAccessor.getSectionsCount()];
		this.inhabitedTime = l;
		this.postProcessing = new ShortList[levelHeightAccessor.getSectionsCount()];
		this.blendingData = blendingData;
		this.skyLightSources = new ChunkSkyLightSources(levelHeightAccessor);
		if (levelChunkSections != null) {
			if (this.sections.length == levelChunkSections.length) {
				System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
			} else {
				LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", levelChunkSections.length, this.sections.length);
			}
		}

		replaceMissingSections(registry, this.sections);
	}

	private static void replaceMissingSections(Registry<Biome> registry, LevelChunkSection[] levelChunkSections) {
		for (int i = 0; i < levelChunkSections.length; i++) {
			if (levelChunkSections[i] == null) {
				levelChunkSections[i] = new LevelChunkSection(registry);
			}
		}
	}

	public GameEventListenerRegistry getListenerRegistry(int i) {
		return GameEventListenerRegistry.NOOP;
	}

	@Nullable
	public abstract BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl);

	public abstract void setBlockEntity(BlockEntity blockEntity);

	public abstract void addEntity(Entity entity);

	public int getHighestFilledSectionIndex() {
		LevelChunkSection[] levelChunkSections = this.getSections();

		for (int i = levelChunkSections.length - 1; i >= 0; i--) {
			LevelChunkSection levelChunkSection = levelChunkSections[i];
			if (!levelChunkSection.hasOnlyAir()) {
				return i;
			}
		}

		return -1;
	}

	@Deprecated(
		forRemoval = true
	)
	public int getHighestSectionPosition() {
		int i = this.getHighestFilledSectionIndex();
		return i == -1 ? this.getMinY() : SectionPos.sectionToBlockCoord(this.getSectionYFromSectionIndex(i));
	}

	public Set<BlockPos> getBlockEntitiesPos() {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet(this.pendingBlockEntities.keySet());
		set.addAll(this.blockEntities.keySet());
		return set;
	}

	public LevelChunkSection[] getSections() {
		return this.sections;
	}

	public LevelChunkSection getSection(int i) {
		return this.getSections()[i];
	}

	public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return Collections.unmodifiableSet(this.heightmaps.entrySet());
	}

	public void setHeightmap(Heightmap.Types types, long[] ls) {
		this.getOrCreateHeightmapUnprimed(types).setRawData(this, types, ls);
	}

	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
		return (Heightmap)this.heightmaps.computeIfAbsent(types, typesx -> new Heightmap(this, typesx));
	}

	public boolean hasPrimedHeightmap(Heightmap.Types types) {
		return this.heightmaps.get(types) != null;
	}

	public int getHeight(Heightmap.Types types, int i, int j) {
		Heightmap heightmap = (Heightmap)this.heightmaps.get(types);
		if (heightmap == null) {
			if (SharedConstants.IS_RUNNING_IN_IDE && this instanceof LevelChunk) {
				LOGGER.error("Unprimed heightmap: " + types + " " + i + " " + j);
			}

			Heightmap.primeHeightmaps(this, EnumSet.of(types));
			heightmap = (Heightmap)this.heightmaps.get(types);
		}

		return heightmap.getFirstAvailable(i & 15, j & 15) - 1;
	}

	public ChunkPos getPos() {
		return this.chunkPos;
	}

	@Nullable
	@Override
	public StructureStart getStartForStructure(Structure structure) {
		return (StructureStart)this.structureStarts.get(structure);
	}

	@Override
	public void setStartForStructure(Structure structure, StructureStart structureStart) {
		this.structureStarts.put(structure, structureStart);
		this.markUnsaved();
	}

	public Map<Structure, StructureStart> getAllStarts() {
		return Collections.unmodifiableMap(this.structureStarts);
	}

	public void setAllStarts(Map<Structure, StructureStart> map) {
		this.structureStarts.clear();
		this.structureStarts.putAll(map);
		this.markUnsaved();
	}

	@Override
	public LongSet getReferencesForStructure(Structure structure) {
		return (LongSet)this.structuresRefences.getOrDefault(structure, EMPTY_REFERENCE_SET);
	}

	@Override
	public void addReferenceForStructure(Structure structure, long l) {
		((LongSet)this.structuresRefences.computeIfAbsent(structure, structurex -> new LongOpenHashSet())).add(l);
		this.markUnsaved();
	}

	@Override
	public Map<Structure, LongSet> getAllReferences() {
		return Collections.unmodifiableMap(this.structuresRefences);
	}

	@Override
	public void setAllReferences(Map<Structure, LongSet> map) {
		this.structuresRefences.clear();
		this.structuresRefences.putAll(map);
		this.markUnsaved();
	}

	public boolean isYSpaceEmpty(int i, int j) {
		if (i < this.getMinY()) {
			i = this.getMinY();
		}

		if (j > this.getMaxY()) {
			j = this.getMaxY();
		}

		for (int k = i; k <= j; k += 16) {
			if (!this.getSection(this.getSectionIndex(k)).hasOnlyAir()) {
				return false;
			}
		}

		return true;
	}

	public boolean isSectionEmpty(int i) {
		return this.getSection(this.getSectionIndexFromSectionY(i)).hasOnlyAir();
	}

	public void markUnsaved() {
		this.unsaved = true;
	}

	public boolean tryMarkSaved() {
		if (this.unsaved) {
			this.unsaved = false;
			return true;
		} else {
			return false;
		}
	}

	public boolean isUnsaved() {
		return this.unsaved;
	}

	public abstract ChunkStatus getPersistedStatus();

	public ChunkStatus getHighestGeneratedStatus() {
		ChunkStatus chunkStatus = this.getPersistedStatus();
		BelowZeroRetrogen belowZeroRetrogen = this.getBelowZeroRetrogen();
		if (belowZeroRetrogen != null) {
			ChunkStatus chunkStatus2 = belowZeroRetrogen.targetStatus();
			return ChunkStatus.max(chunkStatus2, chunkStatus);
		} else {
			return chunkStatus;
		}
	}

	public abstract void removeBlockEntity(BlockPos blockPos);

	public void markPosForPostprocessing(BlockPos blockPos) {
		LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", blockPos);
	}

	public ShortList[] getPostProcessing() {
		return this.postProcessing;
	}

	public void addPackedPostProcess(ShortList shortList, int i) {
		getOrCreateOffsetList(this.getPostProcessing(), i).addAll(shortList);
	}

	public void setBlockEntityNbt(CompoundTag compoundTag) {
		this.pendingBlockEntities.put(BlockEntity.getPosFromTag(compoundTag), compoundTag);
	}

	@Nullable
	public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
		return (CompoundTag)this.pendingBlockEntities.get(blockPos);
	}

	@Nullable
	public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider);

	@Override
	public final void findBlockLightSources(BiConsumer<BlockPos, BlockState> biConsumer) {
		this.findBlocks(blockState -> blockState.getLightEmission() != 0, biConsumer);
	}

	public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> biConsumer) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = this.getMinSectionY(); i <= this.getMaxSectionY(); i++) {
			LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(i));
			if (levelChunkSection.maybeHas(predicate)) {
				BlockPos blockPos = SectionPos.of(this.chunkPos, i).origin();

				for (int j = 0; j < 16; j++) {
					for (int k = 0; k < 16; k++) {
						for (int l = 0; l < 16; l++) {
							BlockState blockState = levelChunkSection.getBlockState(l, j, k);
							if (predicate.test(blockState)) {
								biConsumer.accept(mutableBlockPos.setWithOffset(blockPos, l, j, k), blockState);
							}
						}
					}
				}
			}
		}
	}

	public abstract TickContainerAccess<Block> getBlockTicks();

	public abstract TickContainerAccess<Fluid> getFluidTicks();

	public boolean canBeSerialized() {
		return true;
	}

	public abstract ChunkAccess.PackedTicks getTicksForSerialization(long l);

	public UpgradeData getUpgradeData() {
		return this.upgradeData;
	}

	public boolean isOldNoiseGeneration() {
		return this.blendingData != null;
	}

	@Nullable
	public BlendingData getBlendingData() {
		return this.blendingData;
	}

	public long getInhabitedTime() {
		return this.inhabitedTime;
	}

	public void incrementInhabitedTime(long l) {
		this.inhabitedTime += l;
	}

	public void setInhabitedTime(long l) {
		this.inhabitedTime = l;
	}

	public static ShortList getOrCreateOffsetList(ShortList[] shortLists, int i) {
		if (shortLists[i] == null) {
			shortLists[i] = new ShortArrayList();
		}

		return shortLists[i];
	}

	public boolean isLightCorrect() {
		return this.isLightCorrect;
	}

	public void setLightCorrect(boolean bl) {
		this.isLightCorrect = bl;
		this.markUnsaved();
	}

	@Override
	public int getMinY() {
		return this.levelHeightAccessor.getMinY();
	}

	@Override
	public int getHeight() {
		return this.levelHeightAccessor.getHeight();
	}

	public NoiseChunk getOrCreateNoiseChunk(Function<ChunkAccess, NoiseChunk> function) {
		if (this.noiseChunk == null) {
			this.noiseChunk = (NoiseChunk)function.apply(this);
		}

		return this.noiseChunk;
	}

	@Deprecated
	public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> supplier) {
		if (this.carverBiomeSettings == null) {
			this.carverBiomeSettings = (BiomeGenerationSettings)supplier.get();
		}

		return this.carverBiomeSettings;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k) {
		try {
			int l = QuartPos.fromBlock(this.getMinY());
			int m = l + QuartPos.fromBlock(this.getHeight()) - 1;
			int n = Mth.clamp(j, l, m);
			int o = this.getSectionIndex(QuartPos.toBlock(n));
			return this.sections[o].getNoiseBiome(i & 3, n & 3, k & 3);
		} catch (Throwable var8) {
			CrashReport crashReport = CrashReport.forThrowable(var8, "Getting biome");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Biome being got");
			crashReportCategory.setDetail("Location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this, i, j, k)));
			throw new ReportedException(crashReport);
		}
	}

	public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
		ChunkPos chunkPos = this.getPos();
		int i = QuartPos.fromBlock(chunkPos.getMinBlockX());
		int j = QuartPos.fromBlock(chunkPos.getMinBlockZ());
		LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();

		for (int k = levelHeightAccessor.getMinSectionY(); k <= levelHeightAccessor.getMaxSectionY(); k++) {
			LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(k));
			int l = QuartPos.fromSection(k);
			levelChunkSection.fillBiomesFromNoise(biomeResolver, sampler, i, l, j);
		}
	}

	public boolean hasAnyStructureReferences() {
		return !this.getAllReferences().isEmpty();
	}

	@Nullable
	public BelowZeroRetrogen getBelowZeroRetrogen() {
		return null;
	}

	public boolean isUpgrading() {
		return this.getBelowZeroRetrogen() != null;
	}

	public LevelHeightAccessor getHeightAccessorForGeneration() {
		return this;
	}

	public void initializeLightSources() {
		this.skyLightSources.fillFrom(this);
	}

	@Override
	public ChunkSkyLightSources getSkyLightSources() {
		return this.skyLightSources;
	}

	public static record PackedTicks(List<SavedTick<Block>> blocks, List<SavedTick<Fluid>> fluids) {
	}
}
