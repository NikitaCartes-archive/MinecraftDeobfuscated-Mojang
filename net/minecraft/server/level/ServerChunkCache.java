/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;

public class ServerChunkCache
extends ChunkSource {
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private final DistanceManager distanceManager;
    final ServerLevel level;
    final Thread mainThread;
    final ThreadedLevelLightEngine lightEngine;
    private final MainThreadExecutor mainThreadProcessor;
    public final ChunkMap chunkMap;
    private final DimensionDataStorage dataStorage;
    private long lastInhabitedUpdate;
    private boolean spawnEnemies = true;
    private boolean spawnFriendlies = true;
    private static final int CACHE_SIZE = 4;
    private final long[] lastChunkPos = new long[4];
    private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
    private final ChunkAccess[] lastChunk = new ChunkAccess[4];
    @Nullable
    @VisibleForDebug
    private NaturalSpawner.SpawnState lastSpawnState;

    public ServerChunkCache(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ChunkGenerator chunkGenerator, int i, int j, boolean bl, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier<DimensionDataStorage> supplier) {
        this.level = serverLevel;
        this.mainThreadProcessor = new MainThreadExecutor(serverLevel);
        this.mainThread = Thread.currentThread();
        File file = levelStorageAccess.getDimensionPath(serverLevel.dimension()).resolve("data").toFile();
        file.mkdirs();
        this.dataStorage = new DimensionDataStorage(file, dataFixer);
        this.chunkMap = new ChunkMap(serverLevel, levelStorageAccess, dataFixer, structureTemplateManager, executor, this.mainThreadProcessor, this, chunkGenerator, chunkProgressListener, chunkStatusUpdateListener, supplier, i, bl);
        this.lightEngine = this.chunkMap.getLightEngine();
        this.distanceManager = this.chunkMap.getDistanceManager();
        this.distanceManager.updateSimulationDistance(j);
        this.clearCache();
    }

    @Override
    public ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    private ChunkHolder getVisibleChunkIfPresent(long l) {
        return this.chunkMap.getVisibleChunkIfPresent(l);
    }

    public int getTickingGenerated() {
        return this.chunkMap.getTickingGenerated();
    }

    private void storeInCache(long l, ChunkAccess chunkAccess, ChunkStatus chunkStatus) {
        for (int i = 3; i > 0; --i) {
            this.lastChunkPos[i] = this.lastChunkPos[i - 1];
            this.lastChunkStatus[i] = this.lastChunkStatus[i - 1];
            this.lastChunk[i] = this.lastChunk[i - 1];
        }
        this.lastChunkPos[0] = l;
        this.lastChunkStatus[0] = chunkStatus;
        this.lastChunk[0] = chunkAccess;
    }

    @Override
    @Nullable
    public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        ChunkAccess chunkAccess2;
        if (Thread.currentThread() != this.mainThread) {
            return CompletableFuture.supplyAsync(() -> this.getChunk(i, j, chunkStatus, bl), this.mainThreadProcessor).join();
        }
        ProfilerFiller profilerFiller = this.level.getProfiler();
        profilerFiller.incrementCounter("getChunk");
        long l = ChunkPos.asLong(i, j);
        for (int k = 0; k < 4; ++k) {
            if (l != this.lastChunkPos[k] || chunkStatus != this.lastChunkStatus[k] || (chunkAccess2 = this.lastChunk[k]) == null && bl) continue;
            return chunkAccess2;
        }
        profilerFiller.incrementCounter("getChunkCacheMiss");
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkFutureMainThread(i, j, chunkStatus, bl);
        this.mainThreadProcessor.managedBlock(completableFuture::isDone);
        chunkAccess2 = completableFuture.join().map(chunkAccess -> chunkAccess, chunkLoadingFailure -> {
            if (bl) {
                throw Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + chunkLoadingFailure));
            }
            return null;
        });
        this.storeInCache(l, chunkAccess2, chunkStatus);
        return chunkAccess2;
    }

    @Override
    @Nullable
    public LevelChunk getChunkNow(int i, int j) {
        if (Thread.currentThread() != this.mainThread) {
            return null;
        }
        this.level.getProfiler().incrementCounter("getChunkNow");
        long l = ChunkPos.asLong(i, j);
        for (int k = 0; k < 4; ++k) {
            if (l != this.lastChunkPos[k] || this.lastChunkStatus[k] != ChunkStatus.FULL) continue;
            ChunkAccess chunkAccess = this.lastChunk[k];
            return chunkAccess instanceof LevelChunk ? (LevelChunk)chunkAccess : null;
        }
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (chunkHolder == null) {
            return null;
        }
        Either either = chunkHolder.getFutureIfPresent(ChunkStatus.FULL).getNow(null);
        if (either == null) {
            return null;
        }
        ChunkAccess chunkAccess2 = either.left().orElse(null);
        if (chunkAccess2 != null) {
            this.storeInCache(l, chunkAccess2, ChunkStatus.FULL);
            if (chunkAccess2 instanceof LevelChunk) {
                return (LevelChunk)chunkAccess2;
            }
        }
        return null;
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunkStatus, null);
        Arrays.fill(this.lastChunk, null);
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        CompletionStage<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture2;
        boolean bl2;
        boolean bl3 = bl2 = Thread.currentThread() == this.mainThread;
        if (bl2) {
            completableFuture2 = this.getChunkFutureMainThread(i, j, chunkStatus, bl);
            this.mainThreadProcessor.managedBlock(() -> completableFuture2.isDone());
        } else {
            completableFuture2 = CompletableFuture.supplyAsync(() -> this.getChunkFutureMainThread(i, j, chunkStatus, bl), this.mainThreadProcessor).thenCompose(completableFuture -> completableFuture);
        }
        return completableFuture2;
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        ChunkPos chunkPos = new ChunkPos(i, j);
        long l = chunkPos.toLong();
        int k = 33 + ChunkStatus.getDistance(chunkStatus);
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (bl) {
            this.distanceManager.addTicket(TicketType.UNKNOWN, chunkPos, k, chunkPos);
            if (this.chunkAbsent(chunkHolder, k)) {
                ProfilerFiller profilerFiller = this.level.getProfiler();
                profilerFiller.push("chunkLoad");
                this.runDistanceManagerUpdates();
                chunkHolder = this.getVisibleChunkIfPresent(l);
                profilerFiller.pop();
                if (this.chunkAbsent(chunkHolder, k)) {
                    throw Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        if (this.chunkAbsent(chunkHolder, k)) {
            return ChunkHolder.UNLOADED_CHUNK_FUTURE;
        }
        return chunkHolder.getOrScheduleFuture(chunkStatus, this.chunkMap);
    }

    private boolean chunkAbsent(@Nullable ChunkHolder chunkHolder, int i) {
        return chunkHolder == null || chunkHolder.getTicketLevel() > i;
    }

    @Override
    public boolean hasChunk(int i, int j) {
        int k;
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(new ChunkPos(i, j).toLong());
        return !this.chunkAbsent(chunkHolder, k = 33 + ChunkStatus.getDistance(ChunkStatus.FULL));
    }

    @Override
    public BlockGetter getChunkForLighting(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (chunkHolder == null) {
            return null;
        }
        int k = CHUNK_STATUSES.size() - 1;
        while (true) {
            ChunkStatus chunkStatus;
            Optional<ChunkAccess> optional;
            if ((optional = chunkHolder.getFutureIfPresentUnchecked(chunkStatus = CHUNK_STATUSES.get(k)).getNow(ChunkHolder.UNLOADED_CHUNK).left()).isPresent()) {
                return optional.get();
            }
            if (chunkStatus == ChunkStatus.LIGHT.getParent()) break;
            --k;
        }
        return null;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public boolean pollTask() {
        return this.mainThreadProcessor.pollTask();
    }

    boolean runDistanceManagerUpdates() {
        boolean bl = this.distanceManager.runAllUpdates(this.chunkMap);
        boolean bl2 = this.chunkMap.promoteChunkMap();
        if (bl || bl2) {
            this.clearCache();
            return true;
        }
        return false;
    }

    public boolean isPositionTicking(long l) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (chunkHolder == null) {
            return false;
        }
        if (!this.level.shouldTickBlocksAt(l)) {
            return false;
        }
        Either either = chunkHolder.getTickingChunkFuture().getNow(null);
        return either != null && either.left().isPresent();
    }

    public void save(boolean bl) {
        this.runDistanceManagerUpdates();
        this.chunkMap.saveAllChunks(bl);
    }

    @Override
    public void close() throws IOException {
        this.save(true);
        this.lightEngine.close();
        this.chunkMap.close();
    }

    @Override
    public void tick(BooleanSupplier booleanSupplier, boolean bl) {
        this.level.getProfiler().push("purge");
        this.distanceManager.purgeStaleTickets();
        this.runDistanceManagerUpdates();
        this.level.getProfiler().popPush("chunks");
        if (bl) {
            this.tickChunks();
        }
        this.level.getProfiler().popPush("unload");
        this.chunkMap.tick(booleanSupplier);
        this.level.getProfiler().pop();
        this.clearCache();
    }

    private void tickChunks() {
        NaturalSpawner.SpawnState spawnState;
        long l = this.level.getGameTime();
        long m = l - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = l;
        boolean bl = this.level.isDebug();
        if (bl) {
            this.chunkMap.tick();
            return;
        }
        LevelData levelData = this.level.getLevelData();
        ProfilerFiller profilerFiller = this.level.getProfiler();
        profilerFiller.push("pollingChunks");
        int i = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        boolean bl2 = levelData.getGameTime() % 400L == 0L;
        profilerFiller.push("naturalSpawnCount");
        int j = this.distanceManager.getNaturalSpawnChunkCount();
        this.lastSpawnState = spawnState = NaturalSpawner.createState(j, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
        profilerFiller.popPush("filteringLoadedChunks");
        ArrayList<ChunkAndHolder> list = Lists.newArrayListWithCapacity(j);
        for (ChunkHolder chunkHolder : this.chunkMap.getChunks()) {
            LevelChunk levelChunk = chunkHolder.getTickingChunk();
            if (levelChunk == null) continue;
            list.add(new ChunkAndHolder(levelChunk, chunkHolder));
        }
        profilerFiller.popPush("spawnAndTick");
        boolean bl3 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        Collections.shuffle(list);
        for (ChunkAndHolder chunkAndHolder2 : list) {
            LevelChunk levelChunk2 = chunkAndHolder2.chunk;
            ChunkPos chunkPos = levelChunk2.getPos();
            if (!this.level.isNaturalSpawningAllowed(chunkPos) || !this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkPos)) continue;
            levelChunk2.incrementInhabitedTime(m);
            if (bl3 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkPos)) {
                NaturalSpawner.spawnForChunk(this.level, levelChunk2, spawnState, this.spawnFriendlies, this.spawnEnemies, bl2);
            }
            if (!this.level.shouldTickBlocksAt(chunkPos.toLong())) continue;
            this.level.tickChunk(levelChunk2, i);
        }
        profilerFiller.popPush("customSpawners");
        if (bl3) {
            this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
        }
        profilerFiller.popPush("broadcast");
        list.forEach(chunkAndHolder -> chunkAndHolder.holder.broadcastChanges(chunkAndHolder.chunk));
        profilerFiller.pop();
        profilerFiller.pop();
        this.chunkMap.tick();
    }

    private void getFullChunk(long l, Consumer<LevelChunk> consumer) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (chunkHolder != null) {
            chunkHolder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().ifPresent(consumer);
        }
    }

    @Override
    public String gatherStats() {
        return Integer.toString(this.getLoadedChunksCount());
    }

    @VisibleForTesting
    public int getPendingTasksCount() {
        return this.mainThreadProcessor.getPendingTasksCount();
    }

    public ChunkGenerator getGenerator() {
        return this.chunkMap.generator();
    }

    public ChunkGeneratorStructureState getGeneratorState() {
        return this.chunkMap.generatorState();
    }

    public RandomState randomState() {
        return this.chunkMap.randomState();
    }

    @Override
    public int getLoadedChunksCount() {
        return this.chunkMap.size();
    }

    public void blockChanged(BlockPos blockPos) {
        int j;
        int i = SectionPos.blockToSectionCoord(blockPos.getX());
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j = SectionPos.blockToSectionCoord(blockPos.getZ())));
        if (chunkHolder != null) {
            chunkHolder.blockChanged(blockPos);
        }
    }

    @Override
    public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
        this.mainThreadProcessor.execute(() -> {
            ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(sectionPos.chunk().toLong());
            if (chunkHolder != null) {
                chunkHolder.sectionLightChanged(lightLayer, sectionPos.y());
            }
        });
    }

    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        this.distanceManager.addRegionTicket(ticketType, chunkPos, i, object);
    }

    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        this.distanceManager.removeRegionTicket(ticketType, chunkPos, i, object);
    }

    @Override
    public void updateChunkForced(ChunkPos chunkPos, boolean bl) {
        this.distanceManager.updateChunkForced(chunkPos, bl);
    }

    public void move(ServerPlayer serverPlayer) {
        if (!serverPlayer.isRemoved()) {
            this.chunkMap.move(serverPlayer);
        }
    }

    public void removeEntity(Entity entity) {
        this.chunkMap.removeEntity(entity);
    }

    public void addEntity(Entity entity) {
        this.chunkMap.addEntity(entity);
    }

    public void broadcastAndSend(Entity entity, Packet<?> packet) {
        this.chunkMap.broadcastAndSend(entity, packet);
    }

    public void broadcast(Entity entity, Packet<?> packet) {
        this.chunkMap.broadcast(entity, packet);
    }

    public void setViewDistance(int i) {
        this.chunkMap.setViewDistance(i);
    }

    public void setSimulationDistance(int i) {
        this.distanceManager.updateSimulationDistance(i);
    }

    @Override
    public void setSpawnSettings(boolean bl, boolean bl2) {
        this.spawnEnemies = bl;
        this.spawnFriendlies = bl2;
    }

    public String getChunkDebugData(ChunkPos chunkPos) {
        return this.chunkMap.getChunkDebugData(chunkPos);
    }

    public DimensionDataStorage getDataStorage() {
        return this.dataStorage;
    }

    public PoiManager getPoiManager() {
        return this.chunkMap.getPoiManager();
    }

    public ChunkScanAccess chunkScanner() {
        return this.chunkMap.chunkScanner();
    }

    @Nullable
    @VisibleForDebug
    public NaturalSpawner.SpawnState getLastSpawnState() {
        return this.lastSpawnState;
    }

    public void removeTicketsOnClosing() {
        this.distanceManager.removeTicketsOnClosing();
    }

    @Override
    public /* synthetic */ LevelLightEngine getLightEngine() {
        return this.getLightEngine();
    }

    @Override
    public /* synthetic */ BlockGetter getLevel() {
        return this.getLevel();
    }

    final class MainThreadExecutor
    extends BlockableEventLoop<Runnable> {
        MainThreadExecutor(Level level) {
            super("Chunk source main thread executor for " + level.dimension().location());
        }

        @Override
        protected Runnable wrapRunnable(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean shouldRun(Runnable runnable) {
            return true;
        }

        @Override
        protected boolean scheduleExecutables() {
            return true;
        }

        @Override
        protected Thread getRunningThread() {
            return ServerChunkCache.this.mainThread;
        }

        @Override
        protected void doRunTask(Runnable runnable) {
            ServerChunkCache.this.level.getProfiler().incrementCounter("runTask");
            super.doRunTask(runnable);
        }

        @Override
        protected boolean pollTask() {
            if (ServerChunkCache.this.runDistanceManagerUpdates()) {
                return true;
            }
            ServerChunkCache.this.lightEngine.tryScheduleUpdate();
            return super.pollTask();
        }
    }

    record ChunkAndHolder(LevelChunk chunk, ChunkHolder holder) {
    }
}

