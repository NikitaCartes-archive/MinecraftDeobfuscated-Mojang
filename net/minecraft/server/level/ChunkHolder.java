/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChunkBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
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
    private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>>> futures = new AtomicReferenceArray(CHUNK_STATUSES.size());
    private volatile CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    private final ChunkPos pos;
    private final short[] changedBlocks = new short[64];
    private int changes;
    private int changedSectionFilter;
    private int sectionsToForceSendLightFor;
    private int blockChangedLightSectionFilter;
    private int skyChangedLightSectionFilter;
    private final LevelLightEngine lightEngine;
    private final LevelChangeListener onLevelChange;
    private final PlayerProvider playerProvider;
    private boolean wasAccessibleSinceLastSave;

    public ChunkHolder(ChunkPos chunkPos, int i, LevelLightEngine levelLightEngine, LevelChangeListener levelChangeListener, PlayerProvider playerProvider) {
        this.pos = chunkPos;
        this.lightEngine = levelLightEngine;
        this.onLevelChange = levelChangeListener;
        this.playerProvider = playerProvider;
        this.ticketLevel = this.oldTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel(i);
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
    @Environment(value=EnvType.CLIENT)
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

    public void blockChanged(int i, int j, int k) {
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk == null) {
            return;
        }
        this.changedSectionFilter |= 1 << (j >> 4);
        if (this.changes < 64) {
            short s = (short)(i << 12 | k << 8 | j);
            for (int l = 0; l < this.changes; ++l) {
                if (this.changedBlocks[l] != s) continue;
                return;
            }
            this.changedBlocks[this.changes++] = s;
        }
    }

    public void sectionLightChanged(LightLayer lightLayer, int i) {
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk == null) {
            return;
        }
        levelChunk.setUnsaved(true);
        if (lightLayer == LightLayer.SKY) {
            this.skyChangedLightSectionFilter |= 1 << i - -1;
        } else {
            this.blockChangedLightSectionFilter |= 1 << i - -1;
        }
    }

    public void broadcastChanges(LevelChunk levelChunk) {
        int j;
        int i;
        if (this.changes == 0 && this.skyChangedLightSectionFilter == 0 && this.blockChangedLightSectionFilter == 0) {
            return;
        }
        Level level = levelChunk.getLevel();
        if (this.changes == 64) {
            this.sectionsToForceSendLightFor = -1;
        }
        if (this.skyChangedLightSectionFilter != 0 || this.blockChangedLightSectionFilter != 0) {
            this.broadcast(new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter & ~this.sectionsToForceSendLightFor, this.blockChangedLightSectionFilter & ~this.sectionsToForceSendLightFor), true);
            i = this.skyChangedLightSectionFilter & this.sectionsToForceSendLightFor;
            j = this.blockChangedLightSectionFilter & this.sectionsToForceSendLightFor;
            if (i != 0 || j != 0) {
                this.broadcast(new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, i, j), false);
            }
            this.skyChangedLightSectionFilter = 0;
            this.blockChangedLightSectionFilter = 0;
            this.sectionsToForceSendLightFor &= ~(this.skyChangedLightSectionFilter & this.blockChangedLightSectionFilter);
        }
        if (this.changes == 1) {
            i = (this.changedBlocks[0] >> 12 & 0xF) + this.pos.x * 16;
            j = this.changedBlocks[0] & 0xFF;
            int k = (this.changedBlocks[0] >> 8 & 0xF) + this.pos.z * 16;
            BlockPos blockPos = new BlockPos(i, j, k);
            this.broadcast(new ClientboundBlockUpdatePacket(level, blockPos), false);
            if (level.getBlockState(blockPos).getBlock().isEntityBlock()) {
                this.broadcastBlockEntity(level, blockPos);
            }
        } else if (this.changes == 64) {
            this.broadcast(new ClientboundLevelChunkPacket(levelChunk, this.changedSectionFilter), false);
        } else if (this.changes != 0) {
            this.broadcast(new ClientboundChunkBlocksUpdatePacket(this.changes, this.changedBlocks, levelChunk), false);
            for (i = 0; i < this.changes; ++i) {
                j = (this.changedBlocks[i] >> 12 & 0xF) + this.pos.x * 16;
                int k = this.changedBlocks[i] & 0xFF;
                int l = (this.changedBlocks[i] >> 8 & 0xF) + this.pos.z * 16;
                BlockPos blockPos2 = new BlockPos(j, k, l);
                if (!level.getBlockState(blockPos2).getBlock().isEntityBlock()) continue;
                this.broadcastBlockEntity(level, blockPos2);
            }
        }
        this.changes = 0;
        this.changedSectionFilter = 0;
    }

    private void broadcastBlockEntity(Level level, BlockPos blockPos) {
        ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null && (clientboundBlockEntityDataPacket = blockEntity.getUpdatePacket()) != null) {
            this.broadcast(clientboundBlockEntityDataPacket, false);
        }
    }

    private void broadcast(Packet<?> packet, boolean bl) {
        this.playerProvider.getPlayers(this.pos, bl).forEach(serverPlayer -> serverPlayer.connection.send(packet));
    }

    public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus chunkStatus, ChunkMap chunkMap) {
        Either either;
        int i = chunkStatus.getIndex();
        CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture = this.futures.get(i);
        if (completableFuture != null && ((either = (Either)completableFuture.getNow(null)) == null || either.left().isPresent())) {
            return completableFuture;
        }
        if (ChunkHolder.getStatus(this.ticketLevel).isOrAfter(chunkStatus)) {
            CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture2 = chunkMap.schedule(this, chunkStatus);
            this.updateChunkToSave(completableFuture2);
            this.futures.set(i, completableFuture2);
            return completableFuture2;
        }
        return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
    }

    private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkLoadingFailure>> completableFuture) {
        this.chunkToSave = this.chunkToSave.thenCombine(completableFuture, (chunkAccess2, either) -> either.map(chunkAccess -> chunkAccess, chunkLoadingFailure -> chunkAccess2));
    }

    @Environment(value=EnvType.CLIENT)
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

    protected void updateFutures(ChunkMap chunkMap) {
        CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> completableFuture;
        ChunkStatus chunkStatus = ChunkHolder.getStatus(this.oldTicketLevel);
        ChunkStatus chunkStatus2 = ChunkHolder.getStatus(this.ticketLevel);
        boolean bl = this.oldTicketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
        boolean bl2 = this.ticketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
        FullChunkStatus fullChunkStatus = ChunkHolder.getFullChunkStatus(this.oldTicketLevel);
        FullChunkStatus fullChunkStatus2 = ChunkHolder.getFullChunkStatus(this.ticketLevel);
        if (bl) {
            int i;
            Either either2 = Either.right(new ChunkLoadingFailure(){

                public String toString() {
                    return "Unloaded ticket level " + ChunkHolder.this.pos.toString();
                }
            });
            int n = i = bl2 ? chunkStatus2.getIndex() + 1 : 0;
            while (i <= chunkStatus.getIndex()) {
                completableFuture = this.futures.get(i);
                if (completableFuture != null) {
                    completableFuture.complete(either2);
                } else {
                    this.futures.set(i, CompletableFuture.completedFuture(either2));
                }
                ++i;
            }
        }
        boolean bl3 = fullChunkStatus.isOrAfter(FullChunkStatus.BORDER);
        boolean bl4 = fullChunkStatus2.isOrAfter(FullChunkStatus.BORDER);
        this.wasAccessibleSinceLastSave |= bl4;
        if (!bl3 && bl4) {
            this.fullChunkFuture = chunkMap.unpackTicks(this);
            this.updateChunkToSave(this.fullChunkFuture);
        }
        if (bl3 && !bl4) {
            completableFuture = this.fullChunkFuture;
            this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
            this.updateChunkToSave((CompletableFuture<? extends Either<? extends ChunkAccess, ChunkLoadingFailure>>)completableFuture.thenApply(either -> either.ifLeft(chunkMap::packTicks)));
        }
        boolean bl5 = fullChunkStatus.isOrAfter(FullChunkStatus.TICKING);
        boolean bl6 = fullChunkStatus2.isOrAfter(FullChunkStatus.TICKING);
        if (!bl5 && bl6) {
            this.tickingChunkFuture = chunkMap.postProcess(this);
            this.updateChunkToSave(this.tickingChunkFuture);
        }
        if (bl5 && !bl6) {
            this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean bl7 = fullChunkStatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean bl8 = fullChunkStatus2.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        if (!bl7 && bl8) {
            if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw new IllegalStateException();
            }
            this.entityTickingChunkFuture = chunkMap.getEntityTickingRangeFuture(this.pos);
            this.updateChunkToSave(this.entityTickingChunkFuture);
        }
        if (bl7 && !bl8) {
            this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public static ChunkStatus getStatus(int i) {
        if (i < 33) {
            return ChunkStatus.FULL;
        }
        return ChunkStatus.getStatus(i - 33);
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
        this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(imposterProtoChunk.getWrapped())));
    }

    public static interface PlayerProvider {
        public Stream<ServerPlayer> getPlayers(ChunkPos var1, boolean var2);
    }

    public static interface LevelChangeListener {
        public void onLevelChange(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public static interface ChunkLoadingFailure {
        public static final ChunkLoadingFailure UNLOADED = new ChunkLoadingFailure(){

            public String toString() {
                return "UNLOADED";
            }
        };
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
}

