/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkAccess
implements BlockGetter,
BiomeManager.NoiseBiomeSource,
FeatureAccess {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final ShortList[] postProcessing;
    protected volatile boolean unsaved;
    private volatile boolean isLightCorrect;
    protected final ChunkPos chunkPos;
    private long inhabitedTime;
    @Nullable
    @Deprecated
    private Biome carverBiome;
    @Nullable
    protected NoiseChunk noiseChunk;
    protected final UpgradeData upgradeData;
    @Nullable
    protected BlendingData blendingData;
    protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
    private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.newHashMap();
    protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    protected final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    protected final LevelHeightAccessor levelHeightAccessor;
    protected final LevelChunkSection[] sections;

    public ChunkAccess(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
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
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", (Object)levelChunkSections.length, (Object)this.sections.length);
            }
        }
        ChunkAccess.replaceMissingSections(levelHeightAccessor, registry, this.sections);
    }

    private static void replaceMissingSections(LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, LevelChunkSection[] levelChunkSections) {
        for (int i = 0; i < levelChunkSections.length; ++i) {
            if (levelChunkSections[i] != null) continue;
            levelChunkSections[i] = new LevelChunkSection(levelHeightAccessor.getSectionYFromSectionIndex(i), registry);
        }
    }

    public GameEventDispatcher getEventDispatcher(int i) {
        return GameEventDispatcher.NOOP;
    }

    @Nullable
    public abstract BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    public abstract void setBlockEntity(BlockEntity var1);

    public abstract void addEntity(Entity var1);

    @Nullable
    public LevelChunkSection getHighestSection() {
        LevelChunkSection[] levelChunkSections = this.getSections();
        for (int i = levelChunkSections.length - 1; i >= 0; --i) {
            LevelChunkSection levelChunkSection = levelChunkSections[i];
            if (levelChunkSection.hasOnlyAir()) continue;
            return levelChunkSection;
        }
        return null;
    }

    public int getHighestSectionPosition() {
        LevelChunkSection levelChunkSection = this.getHighestSection();
        return levelChunkSection == null ? this.getMinBuildHeight() : levelChunkSection.bottomBlockY();
    }

    public Set<BlockPos> getBlockEntitiesPos() {
        HashSet<BlockPos> set = Sets.newHashSet(this.pendingBlockEntities.keySet());
        set.addAll(this.blockEntities.keySet());
        return set;
    }

    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    public LevelChunkSection getSection(int i) {
        return this.getSections()[i];
    }

    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public void setHeightmap(Heightmap.Types types, long[] ls) {
        this.getOrCreateHeightmapUnprimed(types).setRawData(this, types, ls);
    }

    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types2) {
        return this.heightmaps.computeIfAbsent(types2, types -> new Heightmap(this, (Heightmap.Types)types));
    }

    public boolean hasPrimedHeightmap(Heightmap.Types types) {
        return this.heightmaps.get(types) != null;
    }

    public int getHeight(Heightmap.Types types, int i, int j) {
        Heightmap heightmap = this.heightmaps.get(types);
        if (heightmap == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE && this instanceof LevelChunk) {
                LOGGER.error("Unprimed heightmap: " + types + " " + i + " " + j);
            }
            Heightmap.primeHeightmaps(this, EnumSet.of(types));
            heightmap = this.heightmaps.get(types);
        }
        return heightmap.getFirstAvailable(i & 0xF, j & 0xF) - 1;
    }

    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @Override
    @Nullable
    public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
        return this.structureStarts.get(structureFeature);
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
    public LongSet getReferencesForFeature(StructureFeature<?> structureFeature2) {
        return this.structuresRefences.computeIfAbsent(structureFeature2, structureFeature -> new LongOpenHashSet());
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> structureFeature2, long l) {
        this.structuresRefences.computeIfAbsent(structureFeature2, structureFeature -> new LongOpenHashSet()).add(l);
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
            if (this.getSection(this.getSectionIndex(k)).hasOnlyAir()) continue;
            return false;
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

    public abstract void removeBlockEntity(BlockPos var1);

    public void markPosForPostprocessing(BlockPos blockPos) {
        LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)blockPos);
    }

    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void addPackedPostProcess(short s, int i) {
        ChunkAccess.getOrCreateOffsetList(this.getPostProcessing(), i).add(s);
    }

    public void setBlockEntityNbt(CompoundTag compoundTag) {
        this.pendingBlockEntities.put(BlockEntity.getPosFromTag(compoundTag), compoundTag);
    }

    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.pendingBlockEntities.get(blockPos);
    }

    @Nullable
    public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos var1);

    public abstract Stream<BlockPos> getLights();

    public abstract TickContainerAccess<Block> getBlockTicks();

    public abstract TickContainerAccess<Fluid> getFluidTicks();

    public abstract TicksToSave getTicksForSerialization();

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

    public NoiseChunk getOrCreateNoiseChunk(NoiseSampler noiseSampler, Supplier<NoiseChunk.NoiseFiller> supplier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender) {
        if (this.noiseChunk == null) {
            this.noiseChunk = NoiseChunk.forChunk(this, noiseSampler, supplier, noiseGeneratorSettings, fluidPicker, blender);
        }
        return this.noiseChunk;
    }

    @Deprecated
    public Biome carverBiome(Supplier<Biome> supplier) {
        if (this.carverBiome == null) {
            this.carverBiome = supplier.get();
        }
        return this.carverBiome;
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k) {
        try {
            int l = QuartPos.fromBlock(this.getMinBuildHeight());
            int m = l + QuartPos.fromBlock(this.getHeight()) - 1;
            int n = Mth.clamp(j, l, m);
            int o = this.getSectionIndex(QuartPos.toBlock(n));
            return this.sections[o].getNoiseBiome(i & 3, n & 3, k & 3);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting biome");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Biome being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, i, j, k));
            throw new ReportedException(crashReport);
        }
    }

    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
        ChunkPos chunkPos = this.getPos();
        int i = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int j = QuartPos.fromBlock(chunkPos.getMinBlockZ());
        LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();
        for (int k = levelHeightAccessor.getMinSection(); k < levelHeightAccessor.getMaxSection(); ++k) {
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

    public record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids) {
    }
}

