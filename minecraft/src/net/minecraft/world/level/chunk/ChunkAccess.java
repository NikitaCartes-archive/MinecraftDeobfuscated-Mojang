package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public abstract class ChunkAccess implements BlockGetter, BiomeManager.NoiseBiomeSource, FeatureAccess {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final ShortList[] postProcessing;
	protected volatile boolean unsaved;
	private volatile boolean isLightCorrect;
	protected final ChunkPos chunkPos;
	private long inhabitedTime;
	@Nullable
	@Deprecated
	private Holder<Biome> carverBiome;
	@Nullable
	protected NoiseChunk noiseChunk;
	protected final UpgradeData upgradeData;
	@Nullable
	protected BlendingData blendingData;
	protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
	private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.<StructureFeature<?>, StructureStart<?>>newHashMap();
	private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.<StructureFeature<?>, LongSet>newHashMap();
	protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.<BlockPos, CompoundTag>newHashMap();
	protected final Map<BlockPos, BlockEntity> blockEntities = Maps.<BlockPos, BlockEntity>newHashMap();
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
		if (levelChunkSections != null) {
			if (this.sections.length == levelChunkSections.length) {
				System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
			} else {
				LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", levelChunkSections.length, this.sections.length);
			}
		}

		replaceMissingSections(levelHeightAccessor, registry, this.sections);
	}

	private static void replaceMissingSections(LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, LevelChunkSection[] levelChunkSections) {
		for (int i = 0; i < levelChunkSections.length; i++) {
			if (levelChunkSections[i] == null) {
				levelChunkSections[i] = new LevelChunkSection(levelHeightAccessor.getSectionYFromSectionIndex(i), registry);
			}
		}
	}

	public GameEventDispatcher getEventDispatcher(int i) {
		return GameEventDispatcher.NOOP;
	}

	@Nullable
	public abstract BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl);

	public abstract void setBlockEntity(BlockEntity blockEntity);

	public abstract void addEntity(Entity entity);

	@Nullable
	public LevelChunkSection getHighestSection() {
		LevelChunkSection[] levelChunkSections = this.getSections();

		for (int i = levelChunkSections.length - 1; i >= 0; i--) {
			LevelChunkSection levelChunkSection = levelChunkSections[i];
			if (!levelChunkSection.hasOnlyAir()) {
				return levelChunkSection;
			}
		}

		return null;
	}

	public int getHighestSectionPosition() {
		LevelChunkSection levelChunkSection = this.getHighestSection();
		return levelChunkSection == null ? this.getMinBuildHeight() : levelChunkSection.bottomBlockY();
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
	public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
		return (StructureStart<?>)this.structureStarts.get(structureFeature);
	}

	@Override
	public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
		this.structureStarts.put(structureFeature, structureStart);
		this.unsaved = true;
	}

	public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
		return Collections.unmodifiableMap(this.structureStarts);
	}

	public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map) {
		this.structureStarts.clear();
		this.structureStarts.putAll(map);
		this.unsaved = true;
	}

	@Override
	public LongSet getReferencesForFeature(StructureFeature<?> structureFeature) {
		return (LongSet)this.structuresRefences.computeIfAbsent(structureFeature, structureFeaturex -> new LongOpenHashSet());
	}

	@Override
	public void addReferenceForFeature(StructureFeature<?> structureFeature, long l) {
		((LongSet)this.structuresRefences.computeIfAbsent(structureFeature, structureFeaturex -> new LongOpenHashSet())).add(l);
		this.unsaved = true;
	}

	@Override
	public Map<StructureFeature<?>, LongSet> getAllReferences() {
		return Collections.unmodifiableMap(this.structuresRefences);
	}

	@Override
	public void setAllReferences(Map<StructureFeature<?>, LongSet> map) {
		this.structuresRefences.clear();
		this.structuresRefences.putAll(map);
		this.unsaved = true;
	}

	public boolean isYSpaceEmpty(int i, int j) {
		if (i < this.getMinBuildHeight()) {
			i = this.getMinBuildHeight();
		}

		if (j >= this.getMaxBuildHeight()) {
			j = this.getMaxBuildHeight() - 1;
		}

		for (int k = i; k <= j; k += 16) {
			if (!this.getSection(this.getSectionIndex(k)).hasOnlyAir()) {
				return false;
			}
		}

		return true;
	}

	public void setUnsaved(boolean bl) {
		this.unsaved = bl;
	}

	public boolean isUnsaved() {
		return this.unsaved;
	}

	public abstract ChunkStatus getStatus();

	public abstract void removeBlockEntity(BlockPos blockPos);

	public void markPosForPostprocessing(BlockPos blockPos) {
		LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", blockPos);
	}

	public ShortList[] getPostProcessing() {
		return this.postProcessing;
	}

	public void addPackedPostProcess(short s, int i) {
		getOrCreateOffsetList(this.getPostProcessing(), i).add(s);
	}

	public void setBlockEntityNbt(CompoundTag compoundTag) {
		this.pendingBlockEntities.put(BlockEntity.getPosFromTag(compoundTag), compoundTag);
	}

	@Nullable
	public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
		return (CompoundTag)this.pendingBlockEntities.get(blockPos);
	}

	@Nullable
	public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos);

	public abstract Stream<BlockPos> getLights();

	public abstract TickContainerAccess<Block> getBlockTicks();

	public abstract TickContainerAccess<Fluid> getFluidTicks();

	public abstract ChunkAccess.TicksToSave getTicksForSerialization();

	public UpgradeData getUpgradeData() {
		return this.upgradeData;
	}

	public boolean isOldNoiseGeneration() {
		return this.blendingData != null && this.blendingData.oldNoise();
	}

	@Nullable
	public BlendingData getBlendingData() {
		return this.blendingData;
	}

	public void setBlendingData(BlendingData blendingData) {
		this.blendingData = blendingData;
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

	public NoiseChunk getOrCreateNoiseChunk(
		NoiseSampler noiseSampler,
		Supplier<NoiseChunk.NoiseFiller> supplier,
		NoiseGeneratorSettings noiseGeneratorSettings,
		Aquifer.FluidPicker fluidPicker,
		Blender blender
	) {
		if (this.noiseChunk == null) {
			this.noiseChunk = NoiseChunk.forChunk(this, noiseSampler, supplier, noiseGeneratorSettings, fluidPicker, blender);
		}

		return this.noiseChunk;
	}

	@Deprecated
	public Holder<Biome> carverBiome(Supplier<Holder<Biome>> supplier) {
		if (this.carverBiome == null) {
			this.carverBiome = (Holder<Biome>)supplier.get();
		}

		return this.carverBiome;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k) {
		try {
			int l = QuartPos.fromBlock(this.getMinBuildHeight());
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

		for (int k = levelHeightAccessor.getMinSection(); k < levelHeightAccessor.getMaxSection(); k++) {
			LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(k));
			levelChunkSection.fillBiomesFromNoise(biomeResolver, sampler, i, j);
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

	public static record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids) {
	}
}
