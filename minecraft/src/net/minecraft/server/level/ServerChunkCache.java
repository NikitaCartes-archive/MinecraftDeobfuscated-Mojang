package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ServerChunkCache extends ChunkSource {
	private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
	private final DistanceManager distanceManager;
	private final ChunkGenerator generator;
	private final ServerLevel level;
	private final Thread mainThread;
	private final ThreadedLevelLightEngine lightEngine;
	private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
	public final ChunkMap chunkMap;
	private final DimensionDataStorage dataStorage;
	private long lastInhabitedUpdate;
	private boolean spawnEnemies = true;
	private boolean spawnFriendlies = true;
	private final long[] lastChunkPos = new long[4];
	private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
	private final ChunkAccess[] lastChunk = new ChunkAccess[4];
	@Nullable
	private NaturalSpawner.SpawnState lastSpawnState;

	public ServerChunkCache(
		ServerLevel serverLevel,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		DataFixer dataFixer,
		StructureManager structureManager,
		Executor executor,
		ChunkGenerator chunkGenerator,
		int i,
		boolean bl,
		ChunkProgressListener chunkProgressListener,
		Supplier<DimensionDataStorage> supplier
	) {
		this.level = serverLevel;
		this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor(serverLevel);
		this.generator = chunkGenerator;
		this.mainThread = Thread.currentThread();
		File file = levelStorageAccess.getDimensionPath(serverLevel.dimensionType());
		File file2 = new File(file, "data");
		file2.mkdirs();
		this.dataStorage = new DimensionDataStorage(file2, dataFixer);
		this.chunkMap = new ChunkMap(
			serverLevel,
			levelStorageAccess,
			dataFixer,
			structureManager,
			executor,
			this.mainThreadProcessor,
			this,
			this.getGenerator(),
			chunkProgressListener,
			supplier,
			i,
			bl
		);
		this.lightEngine = this.chunkMap.getLightEngine();
		this.distanceManager = this.chunkMap.getDistanceManager();
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

	private void storeInCache(long l, ChunkAccess chunkAccess, ChunkStatus chunkStatus) {
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
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkFutureMainThread(i, j, chunkStatus, bl);
			this.mainThreadProcessor.managedBlock(completableFuture::isDone);
			ChunkAccess chunkAccess = ((Either)completableFuture.join()).map(chunkAccessx -> chunkAccessx, chunkLoadingFailure -> {
				if (bl) {
					throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + chunkLoadingFailure));
				} else {
					return null;
				}
			});
			this.storeInCache(l, chunkAccess, chunkStatus);
			return chunkAccess;
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
				Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = (Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)chunkHolder.getFutureIfPresent(
						ChunkStatus.FULL
					)
					.getNow(null);
				if (either == null) {
					return null;
				} else {
					ChunkAccess chunkAccess2 = (ChunkAccess)either.left().orElse(null);
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

	@Environment(EnvType.CLIENT)
	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		boolean bl2 = Thread.currentThread() == this.mainThread;
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture;
		if (bl2) {
			completableFuture = this.getChunkFutureMainThread(i, j, chunkStatus, bl);
			this.mainThreadProcessor.managedBlock(completableFuture::isDone);
		} else {
			completableFuture = CompletableFuture.supplyAsync(() -> this.getChunkFutureMainThread(i, j, chunkStatus, bl), this.mainThreadProcessor)
				.thenCompose(completableFuturex -> completableFuturex);
		}

		return completableFuture;
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
		int k = 33 + ChunkStatus.getDistance(ChunkStatus.FULL);
		return !this.chunkAbsent(chunkHolder, k);
	}

	@Override
	public BlockGetter getChunkForLighting(int i, int j) {
		long l = ChunkPos.asLong(i, j);
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
		if (chunkHolder == null) {
			return null;
		} else {
			int k = CHUNK_STATUSES.size() - 1;

			while (true) {
				ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(k);
				Optional<ChunkAccess> optional = ((Either)chunkHolder.getFutureIfPresentUnchecked(chunkStatus).getNow(ChunkHolder.UNLOADED_CHUNK)).left();
				if (optional.isPresent()) {
					return (BlockGetter)optional.get();
				}

				if (chunkStatus == ChunkStatus.LIGHT.getParent()) {
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

	private boolean runDistanceManagerUpdates() {
		boolean bl = this.distanceManager.runAllUpdates(this.chunkMap);
		boolean bl2 = this.chunkMap.promoteChunkMap();
		if (!bl && !bl2) {
			return false;
		} else {
			this.clearCache();
			return true;
		}
	}

	@Override
	public boolean isEntityTickingChunk(Entity entity) {
		long l = ChunkPos.asLong(Mth.floor(entity.getX()) >> 4, Mth.floor(entity.getZ()) >> 4);
		return this.checkChunkFuture(l, ChunkHolder::getEntityTickingChunkFuture);
	}

	@Override
	public boolean isEntityTickingChunk(ChunkPos chunkPos) {
		return this.checkChunkFuture(chunkPos.toLong(), ChunkHolder::getEntityTickingChunkFuture);
	}

	@Override
	public boolean isTickingChunk(BlockPos blockPos) {
		long l = ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
		return this.checkChunkFuture(l, ChunkHolder::getTickingChunkFuture);
	}

	private boolean checkChunkFuture(long l, Function<ChunkHolder, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>>> function) {
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
		if (chunkHolder == null) {
			return false;
		} else {
			Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = (Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)((CompletableFuture)function.apply(
					chunkHolder
				))
				.getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK);
			return either.left().isPresent();
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

	public void tick(BooleanSupplier booleanSupplier) {
		this.level.getProfiler().push("purge");
		this.distanceManager.purgeStaleTickets();
		this.runDistanceManagerUpdates();
		this.level.getProfiler().popPush("chunks");
		this.tickChunks();
		this.level.getProfiler().popPush("unload");
		this.chunkMap.tick(booleanSupplier);
		this.level.getProfiler().pop();
		this.clearCache();
	}

	private void tickChunks() {
		long l = this.level.getGameTime();
		long m = l - this.lastInhabitedUpdate;
		this.lastInhabitedUpdate = l;
		LevelData levelData = this.level.getLevelData();
		boolean bl = this.level.isDebug();
		boolean bl2 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
		if (!bl) {
			this.level.getProfiler().push("pollingChunks");
			int i = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
			boolean bl3 = levelData.getGameTime() % 400L == 0L;
			this.level.getProfiler().push("naturalSpawnCount");
			int j = this.distanceManager.getNaturalSpawnChunkCount();
			NaturalSpawner.SpawnState spawnState = NaturalSpawner.createState(j, this.level.getAllEntities(), this::getFullChunk);
			this.lastSpawnState = spawnState;
			this.level.getProfiler().pop();
			List<ChunkHolder> list = Lists.<ChunkHolder>newArrayList(this.chunkMap.getChunks());
			Collections.shuffle(list);
			list.forEach(chunkHolder -> {
				Optional<LevelChunk> optional = ((Either)chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK)).left();
				if (optional.isPresent()) {
					LevelChunk levelChunk = (LevelChunk)optional.get();
					this.level.getProfiler().push("broadcast");
					chunkHolder.broadcastChanges(levelChunk);
					this.level.getProfiler().pop();
					ChunkPos chunkPos = chunkHolder.getPos();
					if (!this.chunkMap.noPlayersCloseForSpawning(chunkPos)) {
						levelChunk.setInhabitedTime(levelChunk.getInhabitedTime() + m);
						if (bl2 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(levelChunk.getPos())) {
							NaturalSpawner.spawnForChunk(this.level, levelChunk, spawnState, this.spawnFriendlies, this.spawnEnemies, bl3);
						}

						this.level.tickChunk(levelChunk, i);
					}
				}
			});
			this.level.getProfiler().push("customSpawners");
			if (bl2) {
				this.generator.tickCustomSpawners(this.level, this.spawnEnemies, this.spawnFriendlies);
			}

			this.level.getProfiler().pop();
			this.level.getProfiler().pop();
		}

		this.chunkMap.tick();
	}

	private void getFullChunk(long l, Consumer<LevelChunk> consumer) {
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
		if (chunkHolder != null) {
			((Either)chunkHolder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK)).left().ifPresent(consumer);
		}
	}

	@Override
	public String gatherStats() {
		return "ServerChunkCache: " + this.getLoadedChunksCount();
	}

	@VisibleForTesting
	public int getPendingTasksCount() {
		return this.mainThreadProcessor.getPendingTasksCount();
	}

	public ChunkGenerator getGenerator() {
		return this.generator;
	}

	public int getLoadedChunksCount() {
		return this.chunkMap.size();
	}

	public void blockChanged(BlockPos blockPos) {
		int i = blockPos.getX() >> 4;
		int j = blockPos.getZ() >> 4;
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));
		if (chunkHolder != null) {
			chunkHolder.blockChanged(blockPos.getX() & 15, blockPos.getY(), blockPos.getZ() & 15);
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
		this.chunkMap.move(serverPlayer);
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

	@Override
	public void setSpawnSettings(boolean bl, boolean bl2) {
		this.spawnEnemies = bl;
		this.spawnFriendlies = bl2;
	}

	@Environment(EnvType.CLIENT)
	public String getChunkDebugData(ChunkPos chunkPos) {
		return this.chunkMap.getChunkDebugData(chunkPos);
	}

	public DimensionDataStorage getDataStorage() {
		return this.dataStorage;
	}

	public PoiManager getPoiManager() {
		return this.chunkMap.getPoiManager();
	}

	@Nullable
	public NaturalSpawner.SpawnState getLastSpawnState() {
		return this.lastSpawnState;
	}

	final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
		private MainThreadExecutor(Level level) {
			super("Chunk source main thread executor for " + Registry.DIMENSION_TYPE.getKey(level.dimensionType()));
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
