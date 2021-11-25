/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public class ChunkHolder {
    public static final Either<ChunkAccess, ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkLoadingFailure.UNLOADED);
    public static final CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
    public static final Either<LevelChunk, ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkLoadingFailure.UNLOADED);
    private static final CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_LEVEL_CHUNK);
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private static final FullChunkStatus[] FULL_CHUNK_STATUSES = FullChunkStatus.values();
    private static final int BLOCKS_BEFORE_RESEND_FUDGE = 64;
    private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>>> futures = new AtomicReferenceArray(CHUNK_STATUSES.size());
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
    @Nullable
    private final DebugBuffer<ChunkSaveDebug> chunkToSaveHistory = null;
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    final ChunkPos pos;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter = new BitSet();
    private final BitSet skyChangedLightSectionFilter = new BitSet();
    private final LevelLightEngine lightEngine;
    private final LevelChangeListener onLevelChange;
    private final PlayerProvider playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private boolean resendLight;
    private CompletableFuture<Void> pendingFullStateConfirmation = CompletableFuture.completedFuture(null);

    public ChunkHolder(ChunkPos chunkPos, int i, LevelHeightAccessor levelHeightAccessor, LevelLightEngine levelLightEngine, LevelChangeListener levelChangeListener, PlayerProvider playerProvider) {
        this.pos = chunkPos;
        this.levelHeightAccessor = levelHeightAccessor;
        this.lightEngine = levelLightEngine;
        this.onLevelChange = levelChangeListener;
        this.playerProvider = playerProvider;
        this.ticketLevel = this.oldTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel(i);
        this.changedBlocksPerSection = new ShortSet[levelHeightAccessor.getSectionsCount()];
    }

    public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus chunkStatus) {
        CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.futures.get(chunkStatus.getIndex());
        return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus chunkStatus) {
        if (ChunkHolder.getStatus(this.ticketLevel).isOrAfter(chunkStatus)) {
            return this.getFutureIfPresentUnchecked(chunkStatus);
        }
        return UNLOADED_CHUNK_FUTURE;
    }

    public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getTickingChunkFuture() {
        return this.tickingChunkFuture;
    }

    public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getEntityTickingChunkFuture() {
        return this.entityTickingChunkFuture;
    }

    public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getFullChunkFuture() {
        return this.fullChunkFuture;
    }

    @Nullable
    public LevelChunk getTickingChunk() {
        CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> completableFuture = this.getTickingChunkFuture();
        Either either = completableFuture.getNow(null);
        if (either == null) {
            return null;
        }
        return either.left().orElse(null);
    }

    @Nullable
    public ChunkStatus getLastAvailableStatus() {
        for (int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkStatus = CHUNK_STATUSES.get(i);
            CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.getFutureIfPresentUnchecked(chunkStatus);
            if (!completableFuture.getNow(UNLOADED_CHUNK).left().isPresent()) continue;
            return chunkStatus;
        }
        return null;
    }

    @Nullable
    public ChunkAccess getLastAvailable() {
        for (int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            Optional<ChunkAccess> optional;
            ChunkStatus chunkStatus = CHUNK_STATUSES.get(i);
            CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.getFutureIfPresentUnchecked(chunkStatus);
            if (completableFuture.isCompletedExceptionally() || !(optional = completableFuture.getNow(UNLOADED_CHUNK).left()).isPresent()) continue;
            return optional.get();
        }
        return null;
    }

    public CompletableFuture<ChunkAccess> getChunkToSave() {
        return this.chunkToSave;
    }

    public void blockChanged(BlockPos blockPos) {
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk == null) {
            return;
        }
        int i = this.levelHeightAccessor.getSectionIndex(blockPos.getY());
        if (this.changedBlocksPerSection[i] == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[i] = new ShortOpenHashSet();
        }
        this.changedBlocksPerSection[i].add(SectionPos.sectionRelativePos(blockPos));
    }

    public void sectionLightChanged(LightLayer lightLayer, int i) {
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk == null) {
            return;
        }
        levelChunk.setUnsaved(true);
        int j = this.lightEngine.getMinLightSection();
        int k = this.lightEngine.getMaxLightSection();
        if (i < j || i > k) {
            return;
        }
        int l = i - j;
        if (lightLayer == LightLayer.SKY) {
            this.skyChangedLightSectionFilter.set(l);
        } else {
            this.blockChangedLightSectionFilter.set(l);
        }
    }

    public void broadcastChanges(LevelChunk levelChunk) {
        int j;
        if (!this.hasChangedSections && this.skyChangedLightSectionFilter.isEmpty() && this.blockChangedLightSectionFilter.isEmpty()) {
            return;
        }
        Level level = levelChunk.getLevel();
        int i = 0;
        for (j = 0; j < this.changedBlocksPerSection.length; ++j) {
            i += this.changedBlocksPerSection[j] != null ? this.changedBlocksPerSection[j].size() : 0;
        }
        this.resendLight |= i >= 64;
        if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            this.broadcast(new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter, true), !this.resendLight);
            this.skyChangedLightSectionFilter.clear();
            this.blockChangedLightSectionFilter.clear();
        }
        for (j = 0; j < this.changedBlocksPerSection.length; ++j) {
            ShortSet shortSet = this.changedBlocksPerSection[j];
            if (shortSet == null) continue;
            int k = this.levelHeightAccessor.getSectionYFromSectionIndex(j);
            SectionPos sectionPos = SectionPos.of(levelChunk.getPos(), k);
            if (shortSet.size() == 1) {
                BlockPos blockPos2 = sectionPos.relativeToBlockPos(shortSet.iterator().nextShort());
                BlockState blockState2 = level.getBlockState(blockPos2);
                this.broadcast(new ClientboundBlockUpdatePacket(blockPos2, blockState2), false);
                this.broadcastBlockEntityIfNeeded(level, blockPos2, blockState2);
            } else {
                LevelChunkSection levelChunkSection = levelChunk.getSection(j);
                ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket = new ClientboundSectionBlocksUpdatePacket(sectionPos, shortSet, levelChunkSection, this.resendLight);
                this.broadcast(clientboundSectionBlocksUpdatePacket, false);
                clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) -> this.broadcastBlockEntityIfNeeded(level, (BlockPos)blockPos, (BlockState)blockState));
            }
            this.changedBlocksPerSection[j] = null;
        }
        this.hasChangedSections = false;
    }

    private void broadcastBlockEntityIfNeeded(Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState.hasBlockEntity()) {
            this.broadcastBlockEntity(level, blockPos);
        }
    }

    private void broadcastBlockEntity(Level level, BlockPos blockPos) {
        Packet<ClientGamePacketListener> packet;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null && (packet = blockEntity.getUpdatePacket()) != null) {
            this.broadcast(packet, false);
        }
    }

    private void broadcast(Packet<?> packet, boolean bl) {
        this.playerProvider.getPlayers(this.pos, bl).forEach(serverPlayer -> serverPlayer.connection.send(packet));
    }

    public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus chunkStatus, ChunkMap chunkMap) {
        int i = chunkStatus.getIndex();
        CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.futures.get(i);
        if (completableFuture != null) {
            boolean bl;
            Either either = completableFuture.getNow(null);
            boolean bl2 = bl = either != null && either.right().isPresent();
            if (!bl) {
                return completableFuture;
            }
        }
        if (ChunkHolder.getStatus(this.ticketLevel).isOrAfter(chunkStatus)) {
            CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture2 = chunkMap.schedule(this, chunkStatus);
            this.updateChunkToSave(completableFuture2, "schedule " + chunkStatus);
            this.futures.set(i, completableFuture2);
            return completableFuture2;
        }
        return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
    }

    protected void addSaveDependency(String string, CompletableFuture<?> completableFuture) {
        if (this.chunkToSaveHistory != null) {
            this.chunkToSaveHistory.push(new ChunkSaveDebug(Thread.currentThread(), completableFuture, string));
        }
        this.chunkToSave = this.chunkToSave.thenCombine(completableFuture, (chunkAccess, object) -> chunkAccess);
    }

    private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkLoadingFailure>> completableFuture, String string) {
        if (this.chunkToSaveHistory != null) {
            this.chunkToSaveHistory.push(new ChunkSaveDebug(Thread.currentThread(), completableFuture, string));
        }
        this.chunkToSave = this.chunkToSave.thenCombine(completableFuture, (chunkAccess2, either) -> either.map(chunkAccess -> chunkAccess, chunkLoadingFailure -> chunkAccess2));
    }

    public FullChunkStatus getFullStatus() {
        return ChunkHolder.getFullChunkStatus(this.ticketLevel);
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int i) {
        this.queueLevel = i;
    }

    public void setTicketLevel(int i) {
        this.ticketLevel = i;
    }

    private void scheduleFullChunkPromotion(ChunkMap chunkMap, CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> completableFuture, Executor executor, FullChunkStatus fullChunkStatus) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture completableFuture2 = new CompletableFuture();
        completableFuture2.thenRunAsync(() -> chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus), executor);
        this.pendingFullStateConfirmation = completableFuture2;
        completableFuture.thenAccept(either -> either.ifLeft(levelChunk -> completableFuture2.complete(null)));
    }

    private void demoteFullChunk(ChunkMap chunkMap, FullChunkStatus fullChunkStatus) {
        this.pendingFullStateConfirmation.cancel(false);
        chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus);
    }

    protected void updateFutures(ChunkMap chunkMap, Executor executor) {
        ChunkStatus chunkStatus = ChunkHolder.getStatus(this.oldTicketLevel);
        ChunkStatus chunkStatus2 = ChunkHolder.getStatus(this.ticketLevel);
        boolean bl = this.oldTicketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
        boolean bl2 = this.ticketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
        FullChunkStatus fullChunkStatus = ChunkHolder.getFullChunkStatus(this.oldTicketLevel);
        FullChunkStatus fullChunkStatus2 = ChunkHolder.getFullChunkStatus(this.ticketLevel);
        if (bl) {
            int i;
            Either either = Either.right(new ChunkLoadingFailure(){

                public String toString() {
                    return "Unloaded ticket level " + ChunkHolder.this.pos;
                }
            });
            int n = i = bl2 ? chunkStatus2.getIndex() + 1 : 0;
            while (i <= chunkStatus.getIndex()) {
                CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.futures.get(i);
                if (completableFuture == null) {
                    this.futures.set(i, CompletableFuture.completedFuture(either));
                }
                ++i;
            }
        }
        boolean bl3 = fullChunkStatus.isOrAfter(FullChunkStatus.BORDER);
        boolean bl4 = fullChunkStatus2.isOrAfter(FullChunkStatus.BORDER);
        this.wasAccessibleSinceLastSave |= bl4;
        if (!bl3 && bl4) {
            this.fullChunkFuture = chunkMap.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion(chunkMap, this.fullChunkFuture, executor, FullChunkStatus.BORDER);
            this.updateChunkToSave(this.fullChunkFuture, "full");
        }
        if (bl3 && !bl4) {
            this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean bl5 = fullChunkStatus.isOrAfter(FullChunkStatus.TICKING);
        boolean bl6 = fullChunkStatus2.isOrAfter(FullChunkStatus.TICKING);
        if (!bl5 && bl6) {
            this.tickingChunkFuture = chunkMap.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion(chunkMap, this.tickingChunkFuture, executor, FullChunkStatus.TICKING);
            this.updateChunkToSave(this.tickingChunkFuture, "ticking");
        }
        if (bl5 && !bl6) {
            this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean bl7 = fullChunkStatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean bl8 = fullChunkStatus2.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        if (!bl7 && bl8) {
            if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw Util.pauseInIde(new IllegalStateException());
            }
            this.entityTickingChunkFuture = chunkMap.prepareEntityTickingChunk(this.pos);
            this.scheduleFullChunkPromotion(chunkMap, this.entityTickingChunkFuture, executor, FullChunkStatus.ENTITY_TICKING);
            this.updateChunkToSave(this.entityTickingChunkFuture, "entity ticking");
        }
        if (bl7 && !bl8) {
            this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        if (!fullChunkStatus2.isOrAfter(fullChunkStatus)) {
            this.demoteFullChunk(chunkMap, fullChunkStatus2);
        }
        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public static ChunkStatus getStatus(int i) {
        if (i < 33) {
            return ChunkStatus.FULL;
        }
        return ChunkStatus.getStatusAroundFullChunk(i - 33);
    }

    public static FullChunkStatus getFullChunkStatus(int i) {
        return FULL_CHUNK_STATUSES[Mth.clamp(33 - i + 1, 0, FULL_CHUNK_STATUSES.length - 1)];
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = ChunkHolder.getFullChunkStatus(this.ticketLevel).isOrAfter(FullChunkStatus.BORDER);
    }

    public void replaceProtoChunk(ImposterProtoChunk imposterProtoChunk) {
        for (int i = 0; i < this.futures.length(); ++i) {
            Optional<ChunkAccess> optional;
            CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.futures.get(i);
            if (completableFuture == null || !(optional = completableFuture.getNow(UNLOADED_CHUNK).left()).isPresent() || !(optional.get() instanceof ProtoChunk)) continue;
            this.futures.set(i, CompletableFuture.completedFuture(Either.left(imposterProtoChunk)));
        }
        this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(imposterProtoChunk.getWrapped())), "replaceProto");
    }

    @FunctionalInterface
    public static interface LevelChangeListener {
        public void onLevelChange(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public static interface PlayerProvider {
        public List<ServerPlayer> getPlayers(ChunkPos var1, boolean var2);
    }

    static final class ChunkSaveDebug {
        private final Thread thread;
        private final CompletableFuture<?> future;
        private final String source;

        ChunkSaveDebug(Thread thread, CompletableFuture<?> completableFuture, String string) {
            this.thread = thread;
            this.future = completableFuture;
            this.source = string;
        }
    }

    public static enum FullChunkStatus {
        INACCESSIBLE,
        BORDER,
        TICKING,
        ENTITY_TICKING;


        public boolean isOrAfter(FullChunkStatus fullChunkStatus) {
            return this.ordinal() >= fullChunkStatus.ordinal();
        }
    }

    public static interface ChunkLoadingFailure {
        public static final ChunkLoadingFailure UNLOADED = new ChunkLoadingFailure(){

            public String toString() {
                return "UNLOADED";
            }
        };
    }
}

