package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ServerChunkCache extends ChunkSource {
	private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
	private final DistanceManager distanceManager;
	final ServerLevel level;
	final Thread mainThread;
	final ThreadedLevelLightEngine lightEngine;
	private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
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

	public ServerChunkCache(
		ServerLevel serverLevel,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		DataFixer dataFixer,
		StructureTemplateManager structureTemplateManager,
		Executor executor,
		ChunkGenerator chunkGenerator,
		int i,
		int j,
		boolean bl,
		ChunkProgressListener chunkProgressListener,
		ChunkStatusUpdateListener chunkStatusUpdateListener,
		Supplier<DimensionDataStorage> supplier
	) {
		this.level = serverLevel;
		this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor(serverLevel);
		this.mainThread = Thread.currentThread();
		File file = levelStorageAccess.getDimensionPath(serverLevel.dimension()).resolve("data").toFile();
		file.mkdirs();
		this.dataStorage = new DimensionDataStorage(file, dataFixer, serverLevel.registryAccess());
		this.chunkMap = new ChunkMap(
			serverLevel,
			levelStorageAccess,
			dataFixer,
			structureTemplateManager,
			executor,
			this.mainThreadProcessor,
			this,
			chunkGenerator,
			chunkProgressListener,
			chunkStatusUpdateListener,
			supplier,
			i,
			bl
		);
		this.lightEngine = this.chunkMap.getLightEngine();
		this.distanceManager = this.chunkMap.getDistanceManager();
		this.distanceManager.updateSimulationDistance(j);
		this.clearCache();
	}

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

	private void storeInCache(long l, @Nullable ChunkAccess chunkAccess, ChunkStatus chunkStatus) {
		for (int i = 3; i > 0; i--) {
			this.lastChunkPos[i] = this.lastChunkPos[i - 1];
			this.lastChunkStatus[i] = this.lastChunkStatus[i - 1];
			this.lastChunk[i] = this.lastChunk[i - 1];
		}

		this.lastChunkPos[0] = l;
		this.lastChunkStatus[0] = chunkStatus;
		this.lastChunk[0] = chunkAccess;
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		if (Thread.currentThread() != this.mainThread) {
			return (ChunkAccess)CompletableFuture.supplyAsync(() -> this.getChunk(i, j, chunkStatus, bl), this.mainThreadProcessor).join();
		} else {
			ProfilerFiller profilerFiller = this.level.getProfiler();
			profilerFiller.incrementCounter("getChunk");
			long l = ChunkPos.asLong(i, j);

			for (int k = 0; k < 4; k++) {
				if (l == this.lastChunkPos[k] && chunkStatus == this.lastChunkStatus[k]) {
					ChunkAccess chunkAccess = this.lastChunk[k];
					if (chunkAccess != null || !bl) {
						return chunkAccess;
					}
				}
			}

			profilerFiller.incrementCounter("getChunkCacheMiss");
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.getChunkFutureMainThread(i, j, chunkStatus, bl);
			this.mainThreadProcessor.managedBlock(completableFuture::isDone);
			ChunkResult<ChunkAccess> chunkResult = (ChunkResult<ChunkAccess>)completableFuture.join();
			ChunkAccess chunkAccess2 = chunkResult.orElse(null);
			if (chunkAccess2 == null && bl) {
				throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + chunkResult.getError()));
			} else {
				this.storeInCache(l, chunkAccess2, chunkStatus);
				return chunkAccess2;
			}
		}
	}

	@Nullable
	@Override
	public LevelChunk getChunkNow(int i, int j) {
		if (Thread.currentThread() != this.mainThread) {
			return null;
		} else {
			this.level.getProfiler().incrementCounter("getChunkNow");
			long l = ChunkPos.asLong(i, j);

			for (int k = 0; k < 4; k++) {
				if (l == this.lastChunkPos[k] && this.lastChunkStatus[k] == ChunkStatus.FULL) {
					ChunkAccess chunkAccess = this.lastChunk[k];
					return chunkAccess instanceof LevelChunk ? (LevelChunk)chunkAccess : null;
				}
			}

			ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
			if (chunkHolder == null) {
				return null;
			} else {
				ChunkResult<ChunkAccess> chunkResult = (ChunkResult<ChunkAccess>)chunkHolder.getFutureIfPresent(ChunkStatus.FULL).getNow(null);
				if (chunkResult == null) {
					return null;
				} else {
					ChunkAccess chunkAccess2 = chunkResult.orElse(null);
					if (chunkAccess2 != null) {
						this.storeInCache(l, chunkAccess2, ChunkStatus.FULL);
						if (chunkAccess2 instanceof LevelChunk) {
							return (LevelChunk)chunkAccess2;
						}
					}

					return null;
				}
			}
		}
	}

	private void clearCache() {
		Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
		Arrays.fill(this.lastChunkStatus, null);
		Arrays.fill(this.lastChunk, null);
	}

	public CompletableFuture<ChunkResult<ChunkAccess>> getChunkFuture(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		boolean bl2 = Thread.currentThread() == this.mainThread;
		CompletableFuture<ChunkResult<ChunkAccess>> completableFuture;
		if (bl2) {
			completableFuture = this.getChunkFutureMainThread(i, j, chunkStatus, bl);
			this.mainThreadProcessor.managedBlock(completableFuture::isDone);
		} else {
			completableFuture = CompletableFuture.supplyAsync(() -> this.getChunkFutureMainThread(i, j, chunkStatus, bl), this.mainThreadProcessor)
				.thenCompose(completableFuturex -> completableFuturex);
		}

		return completableFuture;
	}

	private CompletableFuture<ChunkResult<ChunkAccess>> getChunkFutureMainThread(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		ChunkPos chunkPos = new ChunkPos(i, j);
		long l = chunkPos.toLong();
		int k = ChunkLevel.byStatus(chunkStatus);
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
					throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
				}
			}
		}

		return this.chunkAbsent(chunkHolder, k) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkHolder.getOrScheduleFuture(chunkStatus, this.chunkMap);
	}

	private boolean chunkAbsent(@Nullable ChunkHolder chunkHolder, int i) {
		return chunkHolder == null || chunkHolder.getTicketLevel() > i;
	}

	@Override
	public boolean hasChunk(int i, int j) {
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(new ChunkPos(i, j).toLong());
		int k = ChunkLevel.byStatus(ChunkStatus.FULL);
		return !this.chunkAbsent(chunkHolder, k);
	}

	@Nullable
	@Override
	public LightChunk getChunkForLighting(int i, int j) {
		long l = ChunkPos.asLong(i, j);
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
		if (chunkHolder == null) {
			return null;
		} else {
			int k = CHUNK_STATUSES.size() - 1;

			while (true) {
				ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(k);
				ChunkAccess chunkAccess = (ChunkAccess)((ChunkResult)chunkHolder.getFutureIfPresentUnchecked(chunkStatus).getNow(ChunkHolder.UNLOADED_CHUNK)).orElse(null);
				if (chunkAccess != null) {
					return chunkAccess;
				}

				if (chunkStatus == ChunkStatus.INITIALIZE_LIGHT.getParent()) {
					return null;
				}

				k--;
			}
		}
	}

	public Level getLevel() {
		return this.level;
	}

	public boolean pollTask() {
		return this.mainThreadProcessor.pollTask();
	}

	boolean runDistanceManagerUpdates() {
		boolean bl = this.distanceManager.runAllUpdates(this.chunkMap);
		boolean bl2 = this.chunkMap.promoteChunkMap();
		if (!bl && !bl2) {
			return false;
		} else {
			this.clearCache();
			return true;
		}
	}

	public boolean isPositionTicking(long l) {
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
		if (chunkHolder == null) {
			return false;
		} else {
			return !this.level.shouldTickBlocksAt(l) ? false : ((ChunkResult)chunkHolder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK)).isSuccess();
		}
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
		if (this.level.tickRateManager().runsNormally() || !bl) {
			this.distanceManager.purgeStaleTickets();
		}

		this.runDistanceManagerUpdates();
		this.level.getProfiler().popPush("chunks");
		if (bl) {
			this.tickChunks();
			this.chunkMap.tick();
		}

		this.level.getProfiler().popPush("unload");
		this.chunkMap.tick(booleanSupplier);
		this.level.getProfiler().pop();
		this.clearCache();
	}

	private void tickChunks() {
		long l = this.level.getGameTime();
		long m = l - this.lastInhabitedUpdate;
		this.lastInhabitedUpdate = l;
		if (!this.level.isDebug()) {
			ProfilerFiller profilerFiller = this.level.getProfiler();
			profilerFiller.push("pollingChunks");
			profilerFiller.push("filteringLoadedChunks");
			List<ServerChunkCache.ChunkAndHolder> list = Lists.<ServerChunkCache.ChunkAndHolder>newArrayListWithCapacity(this.chunkMap.size());

			for (ChunkHolder chunkHolder : this.chunkMap.getChunks()) {
				LevelChunk levelChunk = chunkHolder.getTickingChunk();
				if (levelChunk != null) {
					list.add(new ServerChunkCache.ChunkAndHolder(levelChunk, chunkHolder));
				}
			}

			if (this.level.tickRateManager().runsNormally()) {
				profilerFiller.popPush("naturalSpawnCount");
				int i = this.distanceManager.getNaturalSpawnChunkCount();
				NaturalSpawner.SpawnState spawnState = NaturalSpawner.createState(
					i, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap)
				);
				this.lastSpawnState = spawnState;
				profilerFiller.popPush("spawnAndTick");
				boolean bl = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
				Util.shuffle(list, this.level.random);
				int j = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
				boolean bl2 = this.level.getLevelData().getGameTime() % 400L == 0L;

				for (ServerChunkCache.ChunkAndHolder chunkAndHolder : list) {
					LevelChunk levelChunk2 = chunkAndHolder.chunk;
					ChunkPos chunkPos = levelChunk2.getPos();
					if (this.level.isNaturalSpawningAllowed(chunkPos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkPos)) {
						levelChunk2.incrementInhabitedTime(m);
						if (bl && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkPos)) {
							NaturalSpawner.spawnForChunk(this.level, levelChunk2, spawnState, this.spawnFriendlies, this.spawnEnemies, bl2);
						}

						if (this.level.shouldTickBlocksAt(chunkPos.toLong())) {
							this.level.tickChunk(levelChunk2, j);
						}
					}
				}

				profilerFiller.popPush("customSpawners");
				if (bl) {
					this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
				}
			}

			profilerFiller.popPush("broadcast");
			list.forEach(chunkAndHolderx -> chunkAndHolderx.holder.broadcastChanges(chunkAndHolderx.chunk));
			profilerFiller.pop();
			profilerFiller.pop();
		}
	}

	private void getFullChunk(long l, Consumer<LevelChunk> consumer) {
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
		if (chunkHolder != null) {
			((ChunkResult)chunkHolder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK)).ifSuccess(consumer);
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
		int i = SectionPos.blockToSectionCoord(blockPos.getX());
		int j = SectionPos.blockToSectionCoord(blockPos.getZ());
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));
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
		this.chunkMap.setServerViewDistance(i);
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

	static record ChunkAndHolder(LevelChunk chunk, ChunkHolder holder) {
	}

	final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
		MainThreadExecutor(final Level level) {
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
			} else {
				ServerChunkCache.this.lightEngine.tryScheduleUpdate();
				return super.pollTask();
			}
		}
	}
}
