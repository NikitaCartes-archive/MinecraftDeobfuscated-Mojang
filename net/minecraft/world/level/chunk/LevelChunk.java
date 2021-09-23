/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.EuclideanGameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LevelChunk
extends ChunkAccess {
    static final Logger LOGGER = LogManager.getLogger();
    private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity(){

        @Override
        public void tick() {
        }

        @Override
        public boolean isRemoved() {
            return true;
        }

        @Override
        public BlockPos getPos() {
            return BlockPos.ZERO;
        }

        @Override
        public String getType() {
            return "<null>";
        }
    };
    private final Map<BlockPos, RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
    private boolean loaded;
    final Level level;
    @Nullable
    private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
    @Nullable
    private Consumer<LevelChunk> postLoad;
    private final Int2ObjectMap<GameEventDispatcher> gameEventDispatcherSections;

    public LevelChunk(Level level, ChunkPos chunkPos) {
        this(level, chunkPos, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, null, null);
    }

    public LevelChunk(Level level, ChunkPos chunkPos, UpgradeData upgradeData, TickList<Block> tickList, TickList<Fluid> tickList2, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable Consumer<LevelChunk> consumer) {
        super(chunkPos, upgradeData, level, level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), l, levelChunkSections, tickList, tickList2);
        this.level = level;
        this.gameEventDispatcherSections = new Int2ObjectOpenHashMap<GameEventDispatcher>();
        for (Heightmap.Types types : Heightmap.Types.values()) {
            if (!ChunkStatus.FULL.heightmapsAfter().contains(types)) continue;
            this.heightmaps.put(types, new Heightmap(this, types));
        }
        this.postLoad = consumer;
    }

    public LevelChunk(ServerLevel serverLevel, ProtoChunk protoChunk, @Nullable Consumer<LevelChunk> consumer) {
        this(serverLevel, protoChunk.getPos(), protoChunk.getUpgradeData(), protoChunk.getBlockTicks(), protoChunk.getLiquidTicks(), protoChunk.getInhabitedTime(), protoChunk.getSections(), consumer);
        for (BlockEntity blockEntity : protoChunk.getBlockEntities().values()) {
            this.setBlockEntity(blockEntity);
        }
        this.pendingBlockEntities.putAll(protoChunk.getBlockEntityNbts());
        for (int i = 0; i < protoChunk.getPostProcessing().length; ++i) {
            this.postProcessing[i] = protoChunk.getPostProcessing()[i];
        }
        this.setAllStarts(protoChunk.getAllStarts());
        this.setAllReferences(protoChunk.getAllReferences());
        for (Map.Entry<Heightmap.Types, Heightmap> entry : protoChunk.getHeightmaps()) {
            if (!ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) continue;
            this.setHeightmap(entry.getKey(), entry.getValue().getRawData());
        }
        this.setLightCorrect(protoChunk.isLightCorrect());
        this.unsaved = true;
    }

    @Override
    public GameEventDispatcher getEventDispatcher(int i2) {
        return this.gameEventDispatcherSections.computeIfAbsent(i2, i -> new EuclideanGameEventDispatcher(this.level));
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        if (this.level.isDebug()) {
            BlockState blockState = null;
            if (j == 60) {
                blockState = Blocks.BARRIER.defaultBlockState();
            }
            if (j == 70) {
                blockState = DebugLevelSource.getBlockStateFor(i, k);
            }
            return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
        }
        try {
            LevelChunkSection levelChunkSection;
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sections.length && !(levelChunkSection = this.sections[l]).hasOnlyAir()) {
                return levelChunkSection.getBlockState(i & 0xF, j & 0xF, k & 0xF);
            }
            return Blocks.AIR.defaultBlockState();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting block state");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, i, j, k));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.getFluidState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public FluidState getFluidState(int i, int j, int k) {
        try {
            LevelChunkSection levelChunkSection;
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sections.length && !(levelChunkSection = this.sections[l]).hasOnlyAir()) {
                return levelChunkSection.getFluidState(i & 0xF, j & 0xF, k & 0xF);
            }
            return Fluids.EMPTY.defaultFluidState();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting fluid state");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, i, j, k));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
        int l;
        int k;
        int i = blockPos.getY();
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(i));
        boolean bl2 = levelChunkSection.hasOnlyAir();
        if (bl2 && blockState.isAir()) {
            return null;
        }
        int j = blockPos.getX() & 0xF;
        BlockState blockState2 = levelChunkSection.setBlockState(j, k = i & 0xF, l = blockPos.getZ() & 0xF, blockState);
        if (blockState2 == blockState) {
            return null;
        }
        Block block = blockState.getBlock();
        ((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING)).update(j, i, l, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)).update(j, i, l, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR)).update(j, i, l, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.WORLD_SURFACE)).update(j, i, l, blockState);
        boolean bl3 = levelChunkSection.hasOnlyAir();
        if (bl2 != bl3) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus(blockPos, bl3);
        }
        boolean bl4 = blockState2.hasBlockEntity();
        if (!this.level.isClientSide) {
            blockState2.onRemove(this.level, blockPos, blockState, bl);
        } else if (!blockState2.is(block) && bl4) {
            this.removeBlockEntity(blockPos);
        }
        if (!levelChunkSection.getBlockState(j, k, l).is(block)) {
            return null;
        }
        if (!this.level.isClientSide) {
            blockState.onPlace(this.level, blockPos, blockState2, bl);
        }
        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = this.getBlockEntity(blockPos, EntityCreationType.CHECK);
            if (blockEntity == null) {
                blockEntity = ((EntityBlock)((Object)block)).newBlockEntity(blockPos, blockState);
                if (blockEntity != null) {
                    this.addAndRegisterBlockEntity(blockEntity);
                }
            } else {
                blockEntity.setBlockState(blockState);
                this.updateBlockEntityTicker(blockEntity);
            }
        }
        this.unsaved = true;
        return blockState2;
    }

    @Override
    @Deprecated
    public void addEntity(Entity entity) {
    }

    @Nullable
    private BlockEntity createBlockEntity(BlockPos blockPos) {
        BlockState blockState = this.getBlockState(blockPos);
        if (!blockState.hasBlockEntity()) {
            return null;
        }
        return ((EntityBlock)((Object)blockState.getBlock())).newBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.getBlockEntity(blockPos, EntityCreationType.CHECK);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos, EntityCreationType entityCreationType) {
        BlockEntity blockEntity2;
        CompoundTag compoundTag;
        BlockEntity blockEntity = (BlockEntity)this.blockEntities.get(blockPos);
        if (blockEntity == null && (compoundTag = (CompoundTag)this.pendingBlockEntities.remove(blockPos)) != null && (blockEntity2 = this.promotePendingBlockEntity(blockPos, compoundTag)) != null) {
            return blockEntity2;
        }
        if (blockEntity == null) {
            if (entityCreationType == EntityCreationType.IMMEDIATE && (blockEntity = this.createBlockEntity(blockPos)) != null) {
                this.addAndRegisterBlockEntity(blockEntity);
            }
        } else if (blockEntity.isRemoved()) {
            this.blockEntities.remove(blockPos);
            return null;
        }
        return blockEntity;
    }

    public void addAndRegisterBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
        if (this.isInLevel()) {
            this.addGameEventListener(blockEntity);
            this.updateBlockEntityTicker(blockEntity);
        }
    }

    private boolean isInLevel() {
        return this.loaded || this.level.isClientSide();
    }

    boolean isTicking(BlockPos blockPos) {
        if (!this.level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return this.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && serverLevel.areEntitiesLoaded(ChunkPos.asLong(blockPos));
        }
        return true;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getBlockPos();
        if (!this.getBlockState(blockPos).hasBlockEntity()) {
            return;
        }
        blockEntity.setLevel(this.level);
        blockEntity.clearRemoved();
        BlockEntity blockEntity2 = this.blockEntities.put(blockPos.immutable(), blockEntity);
        if (blockEntity2 != null && blockEntity2 != blockEntity) {
            blockEntity2.setRemoved();
        }
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity != null && !blockEntity.isRemoved()) {
            CompoundTag compoundTag = blockEntity.saveWithFullMetadata();
            compoundTag.putBoolean("keepPacked", false);
            return compoundTag;
        }
        CompoundTag compoundTag = (CompoundTag)this.pendingBlockEntities.get(blockPos);
        if (compoundTag != null) {
            compoundTag = compoundTag.copy();
            compoundTag.putBoolean("keepPacked", true);
        }
        return compoundTag;
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity blockEntity;
        if (this.isInLevel() && (blockEntity = (BlockEntity)this.blockEntities.remove(blockPos)) != null) {
            this.removeGameEventListener(blockEntity);
            blockEntity.setRemoved();
        }
        this.removeBlockEntityTicker(blockPos);
    }

    private <T extends BlockEntity> void removeGameEventListener(T blockEntity) {
        GameEventListener gameEventListener;
        if (this.level.isClientSide) {
            return;
        }
        Block block = blockEntity.getBlockState().getBlock();
        if (block instanceof EntityBlock && (gameEventListener = ((EntityBlock)((Object)block)).getListener(this.level, blockEntity)) != null) {
            int i = SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY());
            GameEventDispatcher gameEventDispatcher = this.getEventDispatcher(i);
            gameEventDispatcher.unregister(gameEventListener);
            if (gameEventDispatcher.isEmpty()) {
                this.gameEventDispatcherSections.remove(i);
            }
        }
    }

    private void removeBlockEntityTicker(BlockPos blockPos) {
        RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper = this.tickersInLevel.remove(blockPos);
        if (rebindableTickingBlockEntityWrapper != null) {
            rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER);
        }
    }

    public void runPostLoad() {
        if (this.postLoad != null) {
            this.postLoad.accept(this);
            this.postLoad = null;
        }
    }

    public boolean isEmpty() {
        return false;
    }

    public void replaceWithPacketData(FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag2, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        this.clearAllBlockEntities();
        for (LevelChunkSection levelChunkSection : this.sections) {
            levelChunkSection.read(friendlyByteBuf);
        }
        for (Heightmap.Types types : Heightmap.Types.values()) {
            String string = types.getSerializationKey();
            if (!compoundTag2.contains(string, 12)) continue;
            this.setHeightmap(types, compoundTag2.getLongArray(string));
        }
        consumer.accept((blockPos, blockEntityType, compoundTag) -> {
            BlockEntity blockEntity = this.getBlockEntity(blockPos, EntityCreationType.IMMEDIATE);
            if (blockEntity != null && compoundTag != null && blockEntity.getType() == blockEntityType) {
                blockEntity.load(compoundTag);
            }
        });
    }

    public void setLoaded(boolean bl) {
        this.loaded = bl;
    }

    public Level getLevel() {
        return this.level;
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    @Override
    public Stream<BlockPos> getLights() {
        return StreamSupport.stream(BlockPos.betweenClosed(this.chunkPos.getMinBlockX(), this.getMinBuildHeight(), this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), this.getMaxBuildHeight() - 1, this.chunkPos.getMaxBlockZ()).spliterator(), false).filter(blockPos -> this.getBlockState((BlockPos)blockPos).getLightEmission() != 0);
    }

    public void postProcessGeneration() {
        ChunkPos chunkPos = this.getPos();
        for (int i = 0; i < this.postProcessing.length; ++i) {
            if (this.postProcessing[i] == null) continue;
            for (Short short_ : this.postProcessing[i]) {
                BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, this.getSectionYFromSectionIndex(i), chunkPos);
                BlockState blockState = this.getBlockState(blockPos);
                BlockState blockState2 = Block.updateFromNeighbourShapes(blockState, this.level, blockPos);
                this.level.setBlock(blockPos, blockState2, 20);
            }
            this.postProcessing[i].clear();
        }
        this.unpackTicks();
        for (BlockPos blockPos2 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
            this.getBlockEntity(blockPos2);
        }
        this.pendingBlockEntities.clear();
        this.upgradeData.upgrade(this);
    }

    @Nullable
    private BlockEntity promotePendingBlockEntity(BlockPos blockPos, CompoundTag compoundTag) {
        BlockEntity blockEntity;
        BlockState blockState = this.getBlockState(blockPos);
        if ("DUMMY".equals(compoundTag.getString("id"))) {
            if (blockState.hasBlockEntity()) {
                blockEntity = ((EntityBlock)((Object)blockState.getBlock())).newBlockEntity(blockPos, blockState);
            } else {
                blockEntity = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", (Object)blockPos, (Object)blockState);
            }
        } else {
            blockEntity = BlockEntity.loadStatic(blockPos, blockState, compoundTag);
        }
        if (blockEntity != null) {
            blockEntity.setLevel(this.level);
            this.addAndRegisterBlockEntity(blockEntity);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", (Object)blockState, (Object)blockPos);
        }
        return blockEntity;
    }

    public void unpackTicks() {
        if (this.blockTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.blockTicks).copyOut(this.level.getBlockTicks(), blockPos -> this.getBlockState((BlockPos)blockPos).getBlock());
            this.blockTicks = EmptyTickList.empty();
        } else if (this.blockTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
            this.blockTicks = EmptyTickList.empty();
        }
        if (this.liquidTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), blockPos -> this.getFluidState((BlockPos)blockPos).getType());
            this.liquidTicks = EmptyTickList.empty();
        } else if (this.liquidTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
            this.liquidTicks = EmptyTickList.empty();
        }
    }

    public void packTicks(ServerLevel serverLevel) {
        if (this.blockTicks == EmptyTickList.empty()) {
            this.blockTicks = new ChunkTickList<Block>(Registry.BLOCK::getKey, ((ServerTickList)serverLevel.getBlockTicks()).fetchTicksInChunk(this.chunkPos, true, false), serverLevel.getGameTime());
            this.setUnsaved(true);
        }
        if (this.liquidTicks == EmptyTickList.empty()) {
            this.liquidTicks = new ChunkTickList<Fluid>(Registry.FLUID::getKey, ((ServerTickList)serverLevel.getLiquidTicks()).fetchTicksInChunk(this.chunkPos, true, false), serverLevel.getGameTime());
            this.setUnsaved(true);
        }
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public ChunkHolder.FullChunkStatus getFullStatus() {
        if (this.fullStatus == null) {
            return ChunkHolder.FullChunkStatus.BORDER;
        }
        return this.fullStatus.get();
    }

    public void setFullStatus(Supplier<ChunkHolder.FullChunkStatus> supplier) {
        this.fullStatus = supplier;
    }

    public void clearAllBlockEntities() {
        this.blockEntities.values().forEach(BlockEntity::setRemoved);
        this.blockEntities.clear();
        this.tickersInLevel.values().forEach(rebindableTickingBlockEntityWrapper -> rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER));
        this.tickersInLevel.clear();
    }

    public void registerAllBlockEntitiesAfterLevelLoad() {
        this.blockEntities.values().forEach(blockEntity -> {
            this.addGameEventListener(blockEntity);
            this.updateBlockEntityTicker(blockEntity);
        });
    }

    private <T extends BlockEntity> void addGameEventListener(T blockEntity) {
        GameEventListener gameEventListener;
        if (this.level.isClientSide) {
            return;
        }
        Block block = blockEntity.getBlockState().getBlock();
        if (block instanceof EntityBlock && (gameEventListener = ((EntityBlock)((Object)block)).getListener(this.level, blockEntity)) != null) {
            GameEventDispatcher gameEventDispatcher = this.getEventDispatcher(SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY()));
            gameEventDispatcher.register(gameEventListener);
        }
    }

    private <T extends BlockEntity> void updateBlockEntityTicker(T blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        BlockEntityTicker<?> blockEntityTicker = blockState.getTicker(this.level, blockEntity.getType());
        if (blockEntityTicker == null) {
            this.removeBlockEntityTicker(blockEntity.getBlockPos());
        } else {
            this.tickersInLevel.compute(blockEntity.getBlockPos(), (blockPos, rebindableTickingBlockEntityWrapper) -> {
                TickingBlockEntity tickingBlockEntity = this.createTicker(blockEntity, blockEntityTicker);
                if (rebindableTickingBlockEntityWrapper != null) {
                    rebindableTickingBlockEntityWrapper.rebind(tickingBlockEntity);
                    return rebindableTickingBlockEntityWrapper;
                }
                if (this.isInLevel()) {
                    RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper2 = new RebindableTickingBlockEntityWrapper(tickingBlockEntity);
                    this.level.addBlockEntityTicker(rebindableTickingBlockEntityWrapper2);
                    return rebindableTickingBlockEntityWrapper2;
                }
                return null;
            });
        }
    }

    private <T extends BlockEntity> TickingBlockEntity createTicker(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
        return new BoundTickingBlockEntity(this, blockEntity, blockEntityTicker);
    }

    public static enum EntityCreationType {
        IMMEDIATE,
        QUEUED,
        CHECK;

    }

    class RebindableTickingBlockEntityWrapper
    implements TickingBlockEntity {
        private TickingBlockEntity ticker;

        RebindableTickingBlockEntityWrapper(TickingBlockEntity tickingBlockEntity) {
            this.ticker = tickingBlockEntity;
        }

        void rebind(TickingBlockEntity tickingBlockEntity) {
            this.ticker = tickingBlockEntity;
        }

        @Override
        public void tick() {
            this.ticker.tick();
        }

        @Override
        public boolean isRemoved() {
            return this.ticker.isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return this.ticker.getPos();
        }

        @Override
        public String getType() {
            return this.ticker.getType();
        }

        public String toString() {
            return this.ticker.toString() + " <wrapped>";
        }
    }

    static class BoundTickingBlockEntity<T extends BlockEntity>
    implements TickingBlockEntity {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean loggedInvalidBlockState;
        final /* synthetic */ LevelChunk field_27223;

        BoundTickingBlockEntity(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
            this.field_27223 = levelChunk;
            this.blockEntity = blockEntity;
            this.ticker = blockEntityTicker;
        }

        @Override
        public void tick() {
            BlockPos blockPos;
            if (!((BlockEntity)this.blockEntity).isRemoved() && ((BlockEntity)this.blockEntity).hasLevel() && this.field_27223.isTicking(blockPos = ((BlockEntity)this.blockEntity).getBlockPos())) {
                try {
                    ProfilerFiller profilerFiller = this.field_27223.level.getProfiler();
                    profilerFiller.push(this::getType);
                    BlockState blockState = this.field_27223.getBlockState(blockPos);
                    if (((BlockEntity)this.blockEntity).getType().isValid(blockState)) {
                        this.ticker.tick(this.field_27223.level, ((BlockEntity)this.blockEntity).getBlockPos(), blockState, this.blockEntity);
                        this.loggedInvalidBlockState = false;
                    } else if (!this.loggedInvalidBlockState) {
                        this.loggedInvalidBlockState = true;
                        LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", this::getType, this::getPos, () -> blockState);
                    }
                    profilerFiller.pop();
                } catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking block entity");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Block entity being ticked");
                    ((BlockEntity)this.blockEntity).fillCrashReportCategory(crashReportCategory);
                    throw new ReportedException(crashReport);
                }
            }
        }

        @Override
        public boolean isRemoved() {
            return ((BlockEntity)this.blockEntity).isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return ((BlockEntity)this.blockEntity).getBlockPos();
        }

        @Override
        public String getType() {
            return BlockEntityType.getKey(((BlockEntity)this.blockEntity).getType()).toString();
        }

        public String toString() {
            return "Level ticker for " + this.getType() + "@" + this.getPos();
        }
    }
}

