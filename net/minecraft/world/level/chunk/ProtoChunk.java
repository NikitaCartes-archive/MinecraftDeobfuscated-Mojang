/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ProtoChunk
implements ChunkAccess {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ChunkPos chunkPos;
    private volatile boolean isDirty;
    @Nullable
    private ChunkBiomeContainer biomes;
    @Nullable
    private volatile LevelLightEngine lightEngine;
    private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    private final Map<BlockPos, CompoundTag> blockEntityNbts = Maps.newHashMap();
    private final LevelChunkSection[] sections = new LevelChunkSection[16];
    private final List<CompoundTag> entities = Lists.newArrayList();
    private final List<BlockPos> lights = Lists.newArrayList();
    private final ShortList[] postProcessing = new ShortList[16];
    private final Map<String, StructureStart> structureStarts = Maps.newHashMap();
    private final Map<String, LongSet> structuresRefences = Maps.newHashMap();
    private final UpgradeData upgradeData;
    private final ProtoTickList<Block> blockTicks;
    private final ProtoTickList<Fluid> liquidTicks;
    private long inhabitedTime;
    private final Map<GenerationStep.Carving, BitSet> carvingMasks = Maps.newHashMap();
    private volatile boolean isLightCorrect;

    public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData) {
        this(chunkPos, upgradeData, null, new ProtoTickList<Block>(block -> block == null || block.defaultBlockState().isAir(), chunkPos), new ProtoTickList<Fluid>(fluid -> fluid == null || fluid == Fluids.EMPTY, chunkPos));
    }

    public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, @Nullable LevelChunkSection[] levelChunkSections, ProtoTickList<Block> protoTickList, ProtoTickList<Fluid> protoTickList2) {
        this.chunkPos = chunkPos;
        this.upgradeData = upgradeData;
        this.blockTicks = protoTickList;
        this.liquidTicks = protoTickList2;
        if (levelChunkSections != null) {
            if (this.sections.length == levelChunkSections.length) {
                System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", (Object)levelChunkSections.length, (Object)this.sections.length);
            }
        }
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int i = blockPos.getY();
        if (Level.isOutsideBuildHeight(i)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunkSection levelChunkSection = this.getSections()[i >> 4];
        if (LevelChunkSection.isEmpty(levelChunkSection)) {
            return Blocks.AIR.defaultBlockState();
        }
        return levelChunkSection.getBlockState(blockPos.getX() & 0xF, i & 0xF, blockPos.getZ() & 0xF);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        int i = blockPos.getY();
        if (Level.isOutsideBuildHeight(i)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunkSection levelChunkSection = this.getSections()[i >> 4];
        if (LevelChunkSection.isEmpty(levelChunkSection)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return levelChunkSection.getFluidState(blockPos.getX() & 0xF, i & 0xF, blockPos.getZ() & 0xF);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return this.lights.stream();
    }

    public ShortList[] getPackedLights() {
        ShortList[] shortLists = new ShortList[16];
        for (BlockPos blockPos : this.lights) {
            ChunkAccess.getOrCreateOffsetList(shortLists, blockPos.getY() >> 4).add(ProtoChunk.packOffsetCoordinates(blockPos));
        }
        return shortLists;
    }

    public void addLight(short s, int i) {
        this.addLight(ProtoChunk.unpackOffsetCoordinates(s, i, this.chunkPos));
    }

    public void addLight(BlockPos blockPos) {
        this.lights.add(blockPos.immutable());
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        if (j < 0 || j >= 256) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        if (this.sections[j >> 4] == LevelChunk.EMPTY_SECTION && blockState.is(Blocks.AIR)) {
            return blockState;
        }
        if (blockState.getLightEmission() > 0) {
            this.lights.add(new BlockPos((i & 0xF) + this.getPos().getMinBlockX(), j, (k & 0xF) + this.getPos().getMinBlockZ()));
        }
        LevelChunkSection levelChunkSection = this.getOrCreateSection(j >> 4);
        BlockState blockState2 = levelChunkSection.setBlockState(i & 0xF, j & 0xF, k & 0xF, blockState);
        if (this.status.isOrAfter(ChunkStatus.FEATURES) && blockState != blockState2 && (blockState.getLightBlock(this, blockPos) != blockState2.getLightBlock(this, blockPos) || blockState.getLightEmission() != blockState2.getLightEmission() || blockState.useShapeForLightOcclusion() || blockState2.useShapeForLightOcclusion())) {
            LevelLightEngine levelLightEngine = this.getLightEngine();
            levelLightEngine.checkBlock(blockPos);
        }
        EnumSet<Heightmap.Types> enumSet = this.getStatus().heightmapsAfter();
        EnumSet<Heightmap.Types> enumSet2 = null;
        for (Heightmap.Types types : enumSet) {
            Heightmap heightmap = this.heightmaps.get((Object)types);
            if (heightmap != null) continue;
            if (enumSet2 == null) {
                enumSet2 = EnumSet.noneOf(Heightmap.Types.class);
            }
            enumSet2.add(types);
        }
        if (enumSet2 != null) {
            Heightmap.primeHeightmaps(this, enumSet2);
        }
        for (Heightmap.Types types : enumSet) {
            this.heightmaps.get((Object)types).update(i & 0xF, j, k & 0xF, blockState);
        }
        return blockState2;
    }

    public LevelChunkSection getOrCreateSection(int i) {
        if (this.sections[i] == LevelChunk.EMPTY_SECTION) {
            this.sections[i] = new LevelChunkSection(i << 4);
        }
        return this.sections[i];
    }

    @Override
    public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        blockEntity.setPosition(blockPos);
        this.blockEntities.put(blockPos, blockEntity);
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos() {
        HashSet<BlockPos> set = Sets.newHashSet(this.blockEntityNbts.keySet());
        set.addAll(this.blockEntities.keySet());
        return set;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.blockEntities.get(blockPos);
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(CompoundTag compoundTag) {
        this.entities.add(compoundTag);
    }

    @Override
    public void addEntity(Entity entity) {
        if (entity.isPassenger()) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        entity.save(compoundTag);
        this.addEntity(compoundTag);
    }

    public List<CompoundTag> getEntities() {
        return this.entities;
    }

    public void setBiomes(ChunkBiomeContainer chunkBiomeContainer) {
        this.biomes = chunkBiomeContainer;
    }

    @Override
    @Nullable
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

    @Nullable
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Override
    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    @Override
    public void setHeightmap(Heightmap.Types types, long[] ls) {
        this.getOrCreateHeightmapUnprimed(types).setRawData(ls);
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types2) {
        return this.heightmaps.computeIfAbsent(types2, types -> new Heightmap(this, (Heightmap.Types)((Object)types)));
    }

    @Override
    public int getHeight(Heightmap.Types types, int i, int j) {
        Heightmap heightmap = this.heightmaps.get((Object)types);
        if (heightmap == null) {
            Heightmap.primeHeightmaps(this, EnumSet.of(types));
            heightmap = this.heightmaps.get((Object)types);
        }
        return heightmap.getFirstAvailable(i & 0xF, j & 0xF) - 1;
    }

    @Override
    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @Override
    public void setLastSaveTime(long l) {
    }

    @Override
    @Nullable
    public StructureStart getStartForFeature(String string) {
        return this.structureStarts.get(string);
    }

    @Override
    public void setStartForFeature(String string, StructureStart structureStart) {
        this.structureStarts.put(string, structureStart);
        this.isDirty = true;
    }

    @Override
    public Map<String, StructureStart> getAllStarts() {
        return Collections.unmodifiableMap(this.structureStarts);
    }

    @Override
    public void setAllStarts(Map<String, StructureStart> map) {
        this.structureStarts.clear();
        this.structureStarts.putAll(map);
        this.isDirty = true;
    }

    @Override
    public LongSet getReferencesForFeature(String string2) {
        return this.structuresRefences.computeIfAbsent(string2, string -> new LongOpenHashSet());
    }

    @Override
    public void addReferenceForFeature(String string2, long l) {
        this.structuresRefences.computeIfAbsent(string2, string -> new LongOpenHashSet()).add(l);
        this.isDirty = true;
    }

    @Override
    public Map<String, LongSet> getAllReferences() {
        return Collections.unmodifiableMap(this.structuresRefences);
    }

    @Override
    public void setAllReferences(Map<String, LongSet> map) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(map);
        this.isDirty = true;
    }

    public static short packOffsetCoordinates(BlockPos blockPos) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        int l = i & 0xF;
        int m = j & 0xF;
        int n = k & 0xF;
        return (short)(l | m << 4 | n << 8);
    }

    public static BlockPos unpackOffsetCoordinates(short s, int i, ChunkPos chunkPos) {
        int j = (s & 0xF) + (chunkPos.x << 4);
        int k = (s >>> 4 & 0xF) + (i << 4);
        int l = (s >>> 8 & 0xF) + (chunkPos.z << 4);
        return new BlockPos(j, k, l);
    }

    @Override
    public void markPosForPostprocessing(BlockPos blockPos) {
        if (!Level.isOutsideBuildHeight(blockPos)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, blockPos.getY() >> 4).add(ProtoChunk.packOffsetCoordinates(blockPos));
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
        return this.blockEntityNbts.get(blockPos);
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity != null) {
            return blockEntity.save(new CompoundTag());
        }
        return this.blockEntityNbts.get(blockPos);
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
        this.blockEntities.remove(blockPos);
        this.blockEntityNbts.remove(blockPos);
    }

    @Override
    public BitSet getCarvingMask(GenerationStep.Carving carving2) {
        return this.carvingMasks.computeIfAbsent(carving2, carving -> new BitSet(65536));
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

    public /* synthetic */ TickList getLiquidTicks() {
        return this.getLiquidTicks();
    }

    public /* synthetic */ TickList getBlockTicks() {
        return this.getBlockTicks();
    }
}

