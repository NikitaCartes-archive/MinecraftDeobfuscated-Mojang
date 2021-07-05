package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider {
	private static final byte CHUNK_TYPE_REPLACEABLE = -1;
	private static final byte CHUNK_TYPE_UNKNOWN = 0;
	private static final byte CHUNK_TYPE_FULL = 1;
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int CHUNK_SAVED_PER_TICK = 200;
	private static final int MIN_VIEW_DISTANCE = 3;
	public static final int MAX_VIEW_DISTANCE = 33;
	public static final int MAX_CHUNK_DISTANCE = 33 + ChunkStatus.maxDistance();
	public static final int FORCED_TICKET_LEVEL = 31;
	private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
	private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
	private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
	private final LongSet entitiesInLevel = new LongOpenHashSet();
	final ServerLevel level;
	private final ThreadedLevelLightEngine lightEngine;
	private final BlockableEventLoop<Runnable> mainThreadExecutor;
	private final ChunkGenerator generator;
	private final Supplier<DimensionDataStorage> overworldDataStorage;
	private final PoiManager poiManager;
	final LongSet toDrop = new LongOpenHashSet();
	private boolean modified;
	private final ChunkTaskPriorityQueueSorter queueSorter;
	private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
	private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
	private final ChunkProgressListener progressListener;
	private final ChunkStatusUpdateListener chunkStatusListener;
	private final ChunkMap.DistanceManager distanceManager;
	private final AtomicInteger tickingGenerated = new AtomicInteger();
	private final StructureManager structureManager;
	private final String storageName;
	private final PlayerMap playerMap = new PlayerMap();
	private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
	private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
	private final Queue<Runnable> unloadQueue = Queues.<Runnable>newConcurrentLinkedQueue();
	int viewDistance;

	public ChunkMap(
		ServerLevel serverLevel,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		DataFixer dataFixer,
		StructureManager structureManager,
		Executor executor,
		BlockableEventLoop<Runnable> blockableEventLoop,
		LightChunkGetter lightChunkGetter,
		ChunkGenerator chunkGenerator,
		ChunkProgressListener chunkProgressListener,
		ChunkStatusUpdateListener chunkStatusUpdateListener,
		Supplier<DimensionDataStorage> supplier,
		int i,
		boolean bl
	) {
		super(new File(levelStorageAccess.getDimensionPath(serverLevel.dimension()), "region"), dataFixer, bl);
		this.structureManager = structureManager;
		File file = levelStorageAccess.getDimensionPath(serverLevel.dimension());
		this.storageName = file.getName();
		this.level = serverLevel;
		this.generator = chunkGenerator;
		this.mainThreadExecutor = blockableEventLoop;
		ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(executor, "worldgen");
		ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("main", blockableEventLoop::tell);
		this.progressListener = chunkProgressListener;
		this.chunkStatusListener = chunkStatusUpdateListener;
		ProcessorMailbox<Runnable> processorMailbox2 = ProcessorMailbox.create(executor, "light");
		this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorMailbox, processorHandle, processorMailbox2), executor, Integer.MAX_VALUE);
		this.worldgenMailbox = this.queueSorter.getProcessor(processorMailbox, false);
		this.mainThreadMailbox = this.queueSorter.getProcessor(processorHandle, false);
		this.lightEngine = new ThreadedLevelLightEngine(
			lightChunkGetter, this, this.level.dimensionType().hasSkyLight(), processorMailbox2, this.queueSorter.getProcessor(processorMailbox2, false)
		);
		this.distanceManager = new ChunkMap.DistanceManager(executor, blockableEventLoop);
		this.overworldDataStorage = supplier;
		this.poiManager = new PoiManager(new File(file, "poi"), dataFixer, bl, serverLevel);
		this.setViewDistance(i);
	}

	private static double euclideanDistanceSquared(ChunkPos chunkPos, Entity entity) {
		double d = (double)SectionPos.sectionToBlockCoord(chunkPos.x, 8);
		double e = (double)SectionPos.sectionToBlockCoord(chunkPos.z, 8);
		double f = d - entity.getX();
		double g = e - entity.getZ();
		return f * f + g * g;
	}

	private static int checkerboardDistance(ChunkPos chunkPos, ServerPlayer serverPlayer, boolean bl) {
		int i;
		int j;
		if (bl) {
			SectionPos sectionPos = serverPlayer.getLastSectionPos();
			i = sectionPos.x();
			j = sectionPos.z();
		} else {
			i = SectionPos.blockToSectionCoord(serverPlayer.getBlockX());
			j = SectionPos.blockToSectionCoord(serverPlayer.getBlockZ());
		}

		return checkerboardDistance(chunkPos, i, j);
	}

	private static int checkerboardDistance(ChunkPos chunkPos, Entity entity) {
		return checkerboardDistance(chunkPos, SectionPos.blockToSectionCoord(entity.getBlockX()), SectionPos.blockToSectionCoord(entity.getBlockZ()));
	}

	private static int checkerboardDistance(ChunkPos chunkPos, int i, int j) {
		int k = chunkPos.x - i;
		int l = chunkPos.z - j;
		return Math.max(Math.abs(k), Math.abs(l));
	}

	protected ThreadedLevelLightEngine getLightEngine() {
		return this.lightEngine;
	}

	@Nullable
	protected ChunkHolder getUpdatingChunkIfPresent(long l) {
		return this.updatingChunkMap.get(l);
	}

	@Nullable
	protected ChunkHolder getVisibleChunkIfPresent(long l) {
		return this.visibleChunkMap.get(l);
	}

	protected IntSupplier getChunkQueueLevel(long l) {
		return () -> {
			ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
			return chunkHolder == null
				? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1
				: Math.min(chunkHolder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
		};
	}

	public String getChunkDebugData(ChunkPos chunkPos) {
		ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(chunkPos.toLong());
		if (chunkHolder == null) {
			return "null";
		} else {
			String string = chunkHolder.getTicketLevel() + "\n";
			ChunkStatus chunkStatus = chunkHolder.getLastAvailableStatus();
			ChunkAccess chunkAccess = chunkHolder.getLastAvailable();
			if (chunkStatus != null) {
				string = string + "St: §" + chunkStatus.getIndex() + chunkStatus + "§r\n";
			}

			if (chunkAccess != null) {
				string = string + "Ch: §" + chunkAccess.getStatus().getIndex() + chunkAccess.getStatus() + "§r\n";
			}

			ChunkHolder.FullChunkStatus fullChunkStatus = chunkHolder.getFullStatus();
			string = string + "§" + fullChunkStatus.ordinal() + fullChunkStatus;
			return string + "§r";
		}
	}

	private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(
		ChunkPos chunkPos, int i, IntFunction<ChunkStatus> intFunction
	) {
		List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> list = Lists.<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>newArrayList();
		int j = chunkPos.x;
		int k = chunkPos.z;

		for (int l = -i; l <= i; l++) {
			for (int m = -i; m <= i; m++) {
				int n = Math.max(Math.abs(m), Math.abs(l));
				final ChunkPos chunkPos2 = new ChunkPos(j + m, k + l);
				long o = chunkPos2.toLong();
				ChunkHolder chunkHolder = this.getUpdatingChunkIfPresent(o);
				if (chunkHolder == null) {
					return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
						public String toString() {
							return "Unloaded " + chunkPos2;
						}
					}));
				}

				ChunkStatus chunkStatus = (ChunkStatus)intFunction.apply(n);
				CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getOrScheduleFuture(chunkStatus, this);
				list.add(completableFuture);
			}
		}

		CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completableFuture2 = Util.sequence(list);
		return completableFuture2.thenApply(listx -> {
			List<ChunkAccess> list2 = Lists.<ChunkAccess>newArrayList();
			int l = 0;

			for (final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either : listx) {
				Optional<ChunkAccess> optional = either.left();
				if (!optional.isPresent()) {
					final int mx = l;
					return Either.right(new ChunkHolder.ChunkLoadingFailure() {
						public String toString() {
							return "Unloaded " + new ChunkPos(j + mx % (i * 2 + 1), k + mx / (i * 2 + 1)) + " " + either.right().get();
						}
					});
				}

				list2.add((ChunkAccess)optional.get());
				l++;
			}

			return Either.left(list2);
		});
	}

	public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareEntityTickingChunk(ChunkPos chunkPos) {
		return this.getChunkRangeFuture(chunkPos, 2, i -> ChunkStatus.FULL)
			.thenApplyAsync(either -> either.mapLeft(list -> (LevelChunk)list.get(list.size() / 2)), this.mainThreadExecutor);
	}

	@Nullable
	ChunkHolder updateChunkScheduling(long l, int i, @Nullable ChunkHolder chunkHolder, int j) {
		if (j > MAX_CHUNK_DISTANCE && i > MAX_CHUNK_DISTANCE) {
			return chunkHolder;
		} else {
			if (chunkHolder != null) {
				chunkHolder.setTicketLevel(i);
			}

			if (chunkHolder != null) {
				if (i > MAX_CHUNK_DISTANCE) {
					this.toDrop.add(l);
				} else {
					this.toDrop.remove(l);
				}
			}

			if (i <= MAX_CHUNK_DISTANCE && chunkHolder == null) {
				chunkHolder = this.pendingUnloads.remove(l);
				if (chunkHolder != null) {
					chunkHolder.setTicketLevel(i);
				} else {
					chunkHolder = new ChunkHolder(new ChunkPos(l), i, this.level, this.lightEngine, this.queueSorter, this);
				}

				this.updatingChunkMap.put(l, chunkHolder);
				this.modified = true;
			}

			return chunkHolder;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.queueSorter.close();
			this.poiManager.close();
		} finally {
			super.close();
		}
	}

	protected void saveAllChunks(boolean bl) {
		if (bl) {
			List<ChunkHolder> list = (List<ChunkHolder>)this.visibleChunkMap
				.values()
				.stream()
				.filter(ChunkHolder::wasAccessibleSinceLastSave)
				.peek(ChunkHolder::refreshAccessibility)
				.collect(Collectors.toList());
			MutableBoolean mutableBoolean = new MutableBoolean();

			do {
				mutableBoolean.setFalse();
				list.stream()
					.map(chunkHolder -> {
						CompletableFuture<ChunkAccess> completableFuture;
						do {
							completableFuture = chunkHolder.getChunkToSave();
							this.mainThreadExecutor.managedBlock(completableFuture::isDone);
						} while (completableFuture != chunkHolder.getChunkToSave());

						return (ChunkAccess)completableFuture.join();
					})
					.filter(chunkAccess -> chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk)
					.filter(this::save)
					.forEach(chunkAccess -> mutableBoolean.setTrue());
			} while (mutableBoolean.isTrue());

			this.processUnloads(() -> true);
			this.flushWorker();
		} else {
			this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).forEach(chunkHolder -> {
				ChunkAccess chunkAccess = (ChunkAccess)chunkHolder.getChunkToSave().getNow(null);
				if (chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk) {
					this.save(chunkAccess);
					chunkHolder.refreshAccessibility();
				}
			});
		}
	}

	protected void tick(BooleanSupplier booleanSupplier) {
		ProfilerFiller profilerFiller = this.level.getProfiler();
		profilerFiller.push("poi");
		this.poiManager.tick(booleanSupplier);
		profilerFiller.popPush("chunk_unload");
		if (!this.level.noSave()) {
			this.processUnloads(booleanSupplier);
		}

		profilerFiller.pop();
	}

	private void processUnloads(BooleanSupplier booleanSupplier) {
		LongIterator longIterator = this.toDrop.iterator();

		for (int i = 0; longIterator.hasNext() && (booleanSupplier.getAsBoolean() || i < 200 || this.toDrop.size() > 2000); longIterator.remove()) {
			long l = longIterator.nextLong();
			ChunkHolder chunkHolder = this.updatingChunkMap.remove(l);
			if (chunkHolder != null) {
				this.pendingUnloads.put(l, chunkHolder);
				this.modified = true;
				i++;
				this.scheduleUnload(l, chunkHolder);
			}
		}

		Runnable runnable;
		while ((booleanSupplier.getAsBoolean() || this.unloadQueue.size() > 2000) && (runnable = (Runnable)this.unloadQueue.poll()) != null) {
			runnable.run();
		}
	}

	private void scheduleUnload(long l, ChunkHolder chunkHolder) {
		CompletableFuture<ChunkAccess> completableFuture = chunkHolder.getChunkToSave();
		completableFuture.thenAcceptAsync(chunkAccess -> {
			CompletableFuture<ChunkAccess> completableFuture2 = chunkHolder.getChunkToSave();
			if (completableFuture2 != completableFuture) {
				this.scheduleUnload(l, chunkHolder);
			} else {
				if (this.pendingUnloads.remove(l, chunkHolder) && chunkAccess != null) {
					if (chunkAccess instanceof LevelChunk) {
						((LevelChunk)chunkAccess).setLoaded(false);
					}

					this.save(chunkAccess);
					if (this.entitiesInLevel.remove(l) && chunkAccess instanceof LevelChunk levelChunk) {
						this.level.unload(levelChunk);
					}

					this.lightEngine.updateChunkStatus(chunkAccess.getPos());
					this.lightEngine.tryScheduleUpdate();
					this.progressListener.onStatusChange(chunkAccess.getPos(), null);
				}
			}
		}, this.unloadQueue::add).whenComplete((void_, throwable) -> {
			if (throwable != null) {
				LOGGER.error("Failed to save chunk {}", chunkHolder.getPos(), throwable);
			}
		});
	}

	protected boolean promoteChunkMap() {
		if (!this.modified) {
			return false;
		} else {
			this.visibleChunkMap = this.updatingChunkMap.clone();
			this.modified = false;
			return true;
		}
	}

	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder chunkHolder, ChunkStatus chunkStatus) {
		ChunkPos chunkPos = chunkHolder.getPos();
		if (chunkStatus == ChunkStatus.EMPTY) {
			return this.scheduleChunkLoad(chunkPos);
		} else {
			if (chunkStatus == ChunkStatus.LIGHT) {
				this.distanceManager.addTicket(TicketType.LIGHT, chunkPos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkPos);
			}

			Optional<ChunkAccess> optional = ((Either)chunkHolder.getOrScheduleFuture(chunkStatus.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK)).left();
			if (optional.isPresent() && ((ChunkAccess)optional.get()).getStatus().isOrAfter(chunkStatus)) {
				CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkStatus.load(
					this.level, this.structureManager, this.lightEngine, chunkAccess -> this.protoChunkToFullChunk(chunkHolder), (ChunkAccess)optional.get()
				);
				this.progressListener.onStatusChange(chunkPos, chunkStatus);
				return completableFuture;
			} else {
				return this.scheduleChunkGeneration(chunkHolder, chunkStatus);
			}
		}
	}

	private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos chunkPos) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.level.getProfiler().incrementCounter("chunkLoad");
				CompoundTag compoundTag = this.readChunk(chunkPos);
				if (compoundTag != null) {
					boolean bl = compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8);
					if (bl) {
						ChunkAccess chunkAccess = ChunkSerializer.read(this.level, this.structureManager, this.poiManager, chunkPos, compoundTag);
						this.markPosition(chunkPos, chunkAccess.getStatus().getChunkType());
						return Either.left(chunkAccess);
					}

					LOGGER.error("Chunk file at {} is missing level data, skipping", chunkPos);
				}
			} catch (ReportedException var5) {
				Throwable throwable = var5.getCause();
				if (!(throwable instanceof IOException)) {
					this.markPositionReplaceable(chunkPos);
					throw var5;
				}

				LOGGER.error("Couldn't load chunk {}", chunkPos, throwable);
			} catch (Exception var6) {
				LOGGER.error("Couldn't load chunk {}", chunkPos, var6);
			}

			this.markPositionReplaceable(chunkPos);
			return Either.left(new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.level));
		}, this.mainThreadExecutor);
	}

	private void markPositionReplaceable(ChunkPos chunkPos) {
		this.chunkTypeCache.put(chunkPos.toLong(), (byte)-1);
	}

	private byte markPosition(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType) {
		return this.chunkTypeCache.put(chunkPos.toLong(), (byte)(chunkType == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
	}

	private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder chunkHolder, ChunkStatus chunkStatus) {
		ChunkPos chunkPos = chunkHolder.getPos();
		CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkRangeFuture(
			chunkPos, chunkStatus.getRange(), i -> this.getDependencyStatus(chunkStatus, i)
		);
		this.level.getProfiler().incrementCounter((Supplier<String>)(() -> "chunkGenerate " + chunkStatus.getName()));
		Executor executor = runnable -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable));
		return completableFuture.thenComposeAsync(
			either -> either.map(
					list -> {
						try {
							CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuturex = chunkStatus.generate(
								executor, this.level, this.generator, this.structureManager, this.lightEngine, chunkAccess -> this.protoChunkToFullChunk(chunkHolder), list
							);
							this.progressListener.onStatusChange(chunkPos, chunkStatus);
							return completableFuturex;
						} catch (Exception var9) {
							var9.getStackTrace();
							CrashReport crashReport = CrashReport.forThrowable(var9, "Exception generating new chunk");
							CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk to be generated");
							crashReportCategory.setDetail("Location", String.format("%d,%d", chunkPos.x, chunkPos.z));
							crashReportCategory.setDetail("Position hash", ChunkPos.asLong(chunkPos.x, chunkPos.z));
							crashReportCategory.setDetail("Generator", this.generator);
							throw new ReportedException(crashReport);
						}
					},
					chunkLoadingFailure -> {
						this.releaseLightTicket(chunkPos);
						return CompletableFuture.completedFuture(Either.right(chunkLoadingFailure));
					}
				),
			executor
		);
	}

	protected void releaseLightTicket(ChunkPos chunkPos) {
		this.mainThreadExecutor
			.tell(
				Util.name(
					() -> this.distanceManager.removeTicket(TicketType.LIGHT, chunkPos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkPos),
					() -> "release light ticket " + chunkPos
				)
			);
	}

	private ChunkStatus getDependencyStatus(ChunkStatus chunkStatus, int i) {
		ChunkStatus chunkStatus2;
		if (i == 0) {
			chunkStatus2 = chunkStatus.getParent();
		} else {
			chunkStatus2 = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(chunkStatus) + i);
		}

		return chunkStatus2;
	}

	private static void postLoadProtoChunk(ServerLevel serverLevel, List<CompoundTag> list) {
		if (!list.isEmpty()) {
			serverLevel.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(list, serverLevel));
		}
	}

	private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder chunkHolder) {
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getFutureIfPresentUnchecked(
			ChunkStatus.FULL.getParent()
		);
		return completableFuture.thenApplyAsync(either -> {
			ChunkStatus chunkStatus = ChunkHolder.getStatus(chunkHolder.getTicketLevel());
			return !chunkStatus.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft(chunkAccess -> {
				ChunkPos chunkPos = chunkHolder.getPos();
				ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
				LevelChunk levelChunk;
				if (protoChunk instanceof ImposterProtoChunk) {
					levelChunk = ((ImposterProtoChunk)protoChunk).getWrapped();
				} else {
					levelChunk = new LevelChunk(this.level, protoChunk, levelChunkx -> postLoadProtoChunk(this.level, protoChunk.getEntities()));
					chunkHolder.replaceProtoChunk(new ImposterProtoChunk(levelChunk));
				}

				levelChunk.setFullStatus(() -> ChunkHolder.getFullChunkStatus(chunkHolder.getTicketLevel()));
				levelChunk.runPostLoad();
				if (this.entitiesInLevel.add(chunkPos.toLong())) {
					levelChunk.setLoaded(true);
					levelChunk.registerAllBlockEntitiesAfterLevelLoad();
				}

				return levelChunk;
			});
		}, runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(runnable, chunkHolder.getPos().toLong(), chunkHolder::getTicketLevel)));
	}

	public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder chunkHolder) {
		ChunkPos chunkPos = chunkHolder.getPos();
		CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkRangeFuture(chunkPos, 1, i -> ChunkStatus.FULL);
		CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture2 = completableFuture.thenApplyAsync(
			either -> either.flatMap(list -> {
					LevelChunk levelChunk = (LevelChunk)list.get(list.size() / 2);
					levelChunk.postProcessGeneration();
					return Either.left(levelChunk);
				}), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable))
		);
		completableFuture2.thenAcceptAsync(either -> either.ifLeft(levelChunk -> {
				this.tickingGenerated.getAndIncrement();
				Packet<?>[] packets = new Packet[2];
				this.getPlayers(chunkPos, false).forEach(serverPlayer -> this.playerLoadedChunk(serverPlayer, packets, levelChunk));
			}), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable)));
		return completableFuture2;
	}

	public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareAccessibleChunk(ChunkHolder chunkHolder) {
		return this.getChunkRangeFuture(chunkHolder.getPos(), 1, ChunkStatus::getStatusAroundFullChunk).thenApplyAsync(either -> either.mapLeft(list -> {
				LevelChunk levelChunk = (LevelChunk)list.get(list.size() / 2);
				levelChunk.unpackTicks();
				return levelChunk;
			}), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable)));
	}

	public int getTickingGenerated() {
		return this.tickingGenerated.get();
	}

	private boolean save(ChunkAccess chunkAccess) {
		this.poiManager.flush(chunkAccess.getPos());
		if (!chunkAccess.isUnsaved()) {
			return false;
		} else {
			chunkAccess.setUnsaved(false);
			ChunkPos chunkPos = chunkAccess.getPos();

			try {
				ChunkStatus chunkStatus = chunkAccess.getStatus();
				if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
					if (this.isExistingChunkFull(chunkPos)) {
						return false;
					}

					if (chunkStatus == ChunkStatus.EMPTY && chunkAccess.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
						return false;
					}
				}

				this.level.getProfiler().incrementCounter("chunkSave");
				CompoundTag compoundTag = ChunkSerializer.write(this.level, chunkAccess);
				this.write(chunkPos, compoundTag);
				this.markPosition(chunkPos, chunkStatus.getChunkType());
				return true;
			} catch (Exception var5) {
				LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, var5);
				return false;
			}
		}
	}

	private boolean isExistingChunkFull(ChunkPos chunkPos) {
		byte b = this.chunkTypeCache.get(chunkPos.toLong());
		if (b != 0) {
			return b == 1;
		} else {
			CompoundTag compoundTag;
			try {
				compoundTag = this.readChunk(chunkPos);
				if (compoundTag == null) {
					this.markPositionReplaceable(chunkPos);
					return false;
				}
			} catch (Exception var5) {
				LOGGER.error("Failed to read chunk {}", chunkPos, var5);
				this.markPositionReplaceable(chunkPos);
				return false;
			}

			ChunkStatus.ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
			return this.markPosition(chunkPos, chunkType) == 1;
		}
	}

	protected void setViewDistance(int i) {
		int j = Mth.clamp(i + 1, 3, 33);
		if (j != this.viewDistance) {
			int k = this.viewDistance;
			this.viewDistance = j;
			this.distanceManager.updatePlayerTickets(this.viewDistance);

			for (ChunkHolder chunkHolder : this.updatingChunkMap.values()) {
				ChunkPos chunkPos = chunkHolder.getPos();
				Packet<?>[] packets = new Packet[2];
				this.getPlayers(chunkPos, false).forEach(serverPlayer -> {
					int jx = checkerboardDistance(chunkPos, serverPlayer, true);
					boolean bl = jx <= k;
					boolean bl2 = jx <= this.viewDistance;
					this.updateChunkTracking(serverPlayer, chunkPos, packets, bl, bl2);
				});
			}
		}
	}

	protected void updateChunkTracking(ServerPlayer serverPlayer, ChunkPos chunkPos, Packet<?>[] packets, boolean bl, boolean bl2) {
		if (serverPlayer.level == this.level) {
			if (bl2 && !bl) {
				ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(chunkPos.toLong());
				if (chunkHolder != null) {
					LevelChunk levelChunk = chunkHolder.getTickingChunk();
					if (levelChunk != null) {
						this.playerLoadedChunk(serverPlayer, packets, levelChunk);
					}

					DebugPackets.sendPoiPacketsForChunk(this.level, chunkPos);
				}
			}

			if (!bl2 && bl) {
				serverPlayer.untrackChunk(chunkPos);
			}
		}
	}

	public int size() {
		return this.visibleChunkMap.size();
	}

	protected net.minecraft.server.level.DistanceManager getDistanceManager() {
		return this.distanceManager;
	}

	protected Iterable<ChunkHolder> getChunks() {
		return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
	}

	void dumpChunks(Writer writer) throws IOException {
		CsvOutput csvOutput = CsvOutput.builder()
			.addColumn("x")
			.addColumn("z")
			.addColumn("level")
			.addColumn("in_memory")
			.addColumn("status")
			.addColumn("full_status")
			.addColumn("accessible_ready")
			.addColumn("ticking_ready")
			.addColumn("entity_ticking_ready")
			.addColumn("ticket")
			.addColumn("spawning")
			.addColumn("block_entity_count")
			.build(writer);

		for (Entry<ChunkHolder> entry : this.visibleChunkMap.long2ObjectEntrySet()) {
			ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
			ChunkHolder chunkHolder = (ChunkHolder)entry.getValue();
			Optional<ChunkAccess> optional = Optional.ofNullable(chunkHolder.getLastAvailable());
			Optional<LevelChunk> optional2 = optional.flatMap(chunkAccess -> chunkAccess instanceof LevelChunk ? Optional.of((LevelChunk)chunkAccess) : Optional.empty());
			csvOutput.writeRow(
				chunkPos.x,
				chunkPos.z,
				chunkHolder.getTicketLevel(),
				optional.isPresent(),
				optional.map(ChunkAccess::getStatus).orElse(null),
				optional2.map(LevelChunk::getFullStatus).orElse(null),
				printFuture(chunkHolder.getFullChunkFuture()),
				printFuture(chunkHolder.getTickingChunkFuture()),
				printFuture(chunkHolder.getEntityTickingChunkFuture()),
				this.distanceManager.getTicketDebugString(entry.getLongKey()),
				!this.noPlayersCloseForSpawning(chunkPos),
				optional2.map(levelChunk -> levelChunk.getBlockEntities().size()).orElse(0)
			);
		}
	}

	private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture) {
		try {
			Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = (Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)completableFuture.getNow(null);
			return either != null ? either.map(levelChunk -> "done", chunkLoadingFailure -> "unloaded") : "not completed";
		} catch (CompletionException var2) {
			return "failed " + var2.getCause().getMessage();
		} catch (CancellationException var3) {
			return "cancelled";
		}
	}

	@Nullable
	private CompoundTag readChunk(ChunkPos chunkPos) throws IOException {
		CompoundTag compoundTag = this.read(chunkPos);
		return compoundTag == null ? null : this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, compoundTag);
	}

	boolean noPlayersCloseForSpawning(ChunkPos chunkPos) {
		long l = chunkPos.toLong();
		return !this.distanceManager.hasPlayersNearby(l)
			? true
			: this.playerMap.getPlayers(l).noneMatch(serverPlayer -> !serverPlayer.isSpectator() && euclideanDistanceSquared(chunkPos, serverPlayer) < 16384.0);
	}

	private boolean skipPlayer(ServerPlayer serverPlayer) {
		return serverPlayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
	}

	void updatePlayerStatus(ServerPlayer serverPlayer, boolean bl) {
		boolean bl2 = this.skipPlayer(serverPlayer);
		boolean bl3 = this.playerMap.ignoredOrUnknown(serverPlayer);
		int i = SectionPos.blockToSectionCoord(serverPlayer.getBlockX());
		int j = SectionPos.blockToSectionCoord(serverPlayer.getBlockZ());
		if (bl) {
			this.playerMap.addPlayer(ChunkPos.asLong(i, j), serverPlayer, bl2);
			this.updatePlayerPos(serverPlayer);
			if (!bl2) {
				this.distanceManager.addPlayer(SectionPos.of(serverPlayer), serverPlayer);
			}
		} else {
			SectionPos sectionPos = serverPlayer.getLastSectionPos();
			this.playerMap.removePlayer(sectionPos.chunk().toLong(), serverPlayer);
			if (!bl3) {
				this.distanceManager.removePlayer(sectionPos, serverPlayer);
			}
		}

		for (int k = i - this.viewDistance; k <= i + this.viewDistance; k++) {
			for (int l = j - this.viewDistance; l <= j + this.viewDistance; l++) {
				ChunkPos chunkPos = new ChunkPos(k, l);
				this.updateChunkTracking(serverPlayer, chunkPos, new Packet[2], !bl, bl);
			}
		}
	}

	private SectionPos updatePlayerPos(ServerPlayer serverPlayer) {
		SectionPos sectionPos = SectionPos.of(serverPlayer);
		serverPlayer.setLastSectionPos(sectionPos);
		serverPlayer.connection.send(new ClientboundSetChunkCacheCenterPacket(sectionPos.x(), sectionPos.z()));
		return sectionPos;
	}

	public void move(ServerPlayer serverPlayer) {
		for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
			if (trackedEntity.entity == serverPlayer) {
				trackedEntity.updatePlayers(this.level.players());
			} else {
				trackedEntity.updatePlayer(serverPlayer);
			}
		}

		int i = SectionPos.blockToSectionCoord(serverPlayer.getBlockX());
		int j = SectionPos.blockToSectionCoord(serverPlayer.getBlockZ());
		SectionPos sectionPos = serverPlayer.getLastSectionPos();
		SectionPos sectionPos2 = SectionPos.of(serverPlayer);
		long l = sectionPos.chunk().toLong();
		long m = sectionPos2.chunk().toLong();
		boolean bl = this.playerMap.ignored(serverPlayer);
		boolean bl2 = this.skipPlayer(serverPlayer);
		boolean bl3 = sectionPos.asLong() != sectionPos2.asLong();
		if (bl3 || bl != bl2) {
			this.updatePlayerPos(serverPlayer);
			if (!bl) {
				this.distanceManager.removePlayer(sectionPos, serverPlayer);
			}

			if (!bl2) {
				this.distanceManager.addPlayer(sectionPos2, serverPlayer);
			}

			if (!bl && bl2) {
				this.playerMap.ignorePlayer(serverPlayer);
			}

			if (bl && !bl2) {
				this.playerMap.unIgnorePlayer(serverPlayer);
			}

			if (l != m) {
				this.playerMap.updatePlayer(l, m, serverPlayer);
			}
		}

		int k = sectionPos.x();
		int n = sectionPos.z();
		if (Math.abs(k - i) <= this.viewDistance * 2 && Math.abs(n - j) <= this.viewDistance * 2) {
			int o = Math.min(i, k) - this.viewDistance;
			int p = Math.min(j, n) - this.viewDistance;
			int q = Math.max(i, k) + this.viewDistance;
			int r = Math.max(j, n) + this.viewDistance;

			for (int s = o; s <= q; s++) {
				for (int t = p; t <= r; t++) {
					ChunkPos chunkPos = new ChunkPos(s, t);
					boolean bl4 = checkerboardDistance(chunkPos, k, n) <= this.viewDistance;
					boolean bl5 = checkerboardDistance(chunkPos, i, j) <= this.viewDistance;
					this.updateChunkTracking(serverPlayer, chunkPos, new Packet[2], bl4, bl5);
				}
			}
		} else {
			for (int o = k - this.viewDistance; o <= k + this.viewDistance; o++) {
				for (int p = n - this.viewDistance; p <= n + this.viewDistance; p++) {
					ChunkPos chunkPos2 = new ChunkPos(o, p);
					boolean bl6 = true;
					boolean bl7 = false;
					this.updateChunkTracking(serverPlayer, chunkPos2, new Packet[2], true, false);
				}
			}

			for (int o = i - this.viewDistance; o <= i + this.viewDistance; o++) {
				for (int p = j - this.viewDistance; p <= j + this.viewDistance; p++) {
					ChunkPos chunkPos2 = new ChunkPos(o, p);
					boolean bl6 = false;
					boolean bl7 = true;
					this.updateChunkTracking(serverPlayer, chunkPos2, new Packet[2], false, true);
				}
			}
		}
	}

	@Override
	public Stream<ServerPlayer> getPlayers(ChunkPos chunkPos, boolean bl) {
		return this.playerMap.getPlayers(chunkPos.toLong()).filter(serverPlayer -> {
			int i = checkerboardDistance(chunkPos, serverPlayer, true);
			return i > this.viewDistance ? false : !bl || i == this.viewDistance;
		});
	}

	protected void addEntity(Entity entity) {
		if (!(entity instanceof EnderDragonPart)) {
			EntityType<?> entityType = entity.getType();
			int i = entityType.clientTrackingRange() * 16;
			if (i != 0) {
				int j = entityType.updateInterval();
				if (this.entityMap.containsKey(entity.getId())) {
					throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
				} else {
					ChunkMap.TrackedEntity trackedEntity = new ChunkMap.TrackedEntity(entity, i, j, entityType.trackDeltas());
					this.entityMap.put(entity.getId(), trackedEntity);
					trackedEntity.updatePlayers(this.level.players());
					if (entity instanceof ServerPlayer serverPlayer) {
						this.updatePlayerStatus(serverPlayer, true);

						for (ChunkMap.TrackedEntity trackedEntity2 : this.entityMap.values()) {
							if (trackedEntity2.entity != serverPlayer) {
								trackedEntity2.updatePlayer(serverPlayer);
							}
						}
					}
				}
			}
		}
	}

	protected void removeEntity(Entity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			this.updatePlayerStatus(serverPlayer, false);

			for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
				trackedEntity.removePlayer(serverPlayer);
			}
		}

		ChunkMap.TrackedEntity trackedEntity2 = this.entityMap.remove(entity.getId());
		if (trackedEntity2 != null) {
			trackedEntity2.broadcastRemoved();
		}
	}

	protected void tick() {
		List<ServerPlayer> list = Lists.<ServerPlayer>newArrayList();
		List<ServerPlayer> list2 = this.level.players();

		for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
			SectionPos sectionPos = trackedEntity.lastSectionPos;
			SectionPos sectionPos2 = SectionPos.of(trackedEntity.entity);
			if (!Objects.equals(sectionPos, sectionPos2)) {
				trackedEntity.updatePlayers(list2);
				Entity entity = trackedEntity.entity;
				if (entity instanceof ServerPlayer) {
					list.add((ServerPlayer)entity);
				}

				trackedEntity.lastSectionPos = sectionPos2;
			}

			trackedEntity.serverEntity.sendChanges();
		}

		if (!list.isEmpty()) {
			for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
				trackedEntity.updatePlayers(list);
			}
		}
	}

	public void broadcast(Entity entity, Packet<?> packet) {
		ChunkMap.TrackedEntity trackedEntity = this.entityMap.get(entity.getId());
		if (trackedEntity != null) {
			trackedEntity.broadcast(packet);
		}
	}

	protected void broadcastAndSend(Entity entity, Packet<?> packet) {
		ChunkMap.TrackedEntity trackedEntity = this.entityMap.get(entity.getId());
		if (trackedEntity != null) {
			trackedEntity.broadcastAndSend(packet);
		}
	}

	private void playerLoadedChunk(ServerPlayer serverPlayer, Packet<?>[] packets, LevelChunk levelChunk) {
		if (packets[0] == null) {
			packets[0] = new ClientboundLevelChunkPacket(levelChunk);
			packets[1] = new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, null, null, true);
		}

		serverPlayer.trackChunk(levelChunk.getPos(), packets[0], packets[1]);
		DebugPackets.sendPoiPacketsForChunk(this.level, levelChunk.getPos());
		List<Entity> list = Lists.<Entity>newArrayList();
		List<Entity> list2 = Lists.<Entity>newArrayList();

		for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
			Entity entity = trackedEntity.entity;
			if (entity != serverPlayer && entity.chunkPosition().equals(levelChunk.getPos())) {
				trackedEntity.updatePlayer(serverPlayer);
				if (entity instanceof Mob && ((Mob)entity).getLeashHolder() != null) {
					list.add(entity);
				}

				if (!entity.getPassengers().isEmpty()) {
					list2.add(entity);
				}
			}
		}

		if (!list.isEmpty()) {
			for (Entity entity2 : list) {
				serverPlayer.connection.send(new ClientboundSetEntityLinkPacket(entity2, ((Mob)entity2).getLeashHolder()));
			}
		}

		if (!list2.isEmpty()) {
			for (Entity entity2 : list2) {
				serverPlayer.connection.send(new ClientboundSetPassengersPacket(entity2));
			}
		}
	}

	protected PoiManager getPoiManager() {
		return this.poiManager;
	}

	public String getStorageName() {
		return this.storageName;
	}

	public CompletableFuture<Void> packTicks(LevelChunk levelChunk) {
		return this.mainThreadExecutor.submit((Runnable)(() -> levelChunk.packTicks(this.level)));
	}

	void onFullChunkStatusChange(ChunkPos chunkPos, ChunkHolder.FullChunkStatus fullChunkStatus) {
		this.chunkStatusListener.onChunkStatusChange(chunkPos, fullChunkStatus);
	}

	class DistanceManager extends net.minecraft.server.level.DistanceManager {
		protected DistanceManager(Executor executor, Executor executor2) {
			super(executor, executor2);
		}

		@Override
		protected boolean isChunkToRemove(long l) {
			return ChunkMap.this.toDrop.contains(l);
		}

		@Nullable
		@Override
		protected ChunkHolder getChunk(long l) {
			return ChunkMap.this.getUpdatingChunkIfPresent(l);
		}

		@Nullable
		@Override
		protected ChunkHolder updateChunkScheduling(long l, int i, @Nullable ChunkHolder chunkHolder, int j) {
			return ChunkMap.this.updateChunkScheduling(l, i, chunkHolder, j);
		}
	}

	class TrackedEntity {
		final ServerEntity serverEntity;
		final Entity entity;
		private final int range;
		SectionPos lastSectionPos;
		private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

		public TrackedEntity(Entity entity, int i, int j, boolean bl) {
			this.serverEntity = new ServerEntity(ChunkMap.this.level, entity, j, bl, this::broadcast);
			this.entity = entity;
			this.range = i;
			this.lastSectionPos = SectionPos.of(entity);
		}

		public boolean equals(Object object) {
			return object instanceof ChunkMap.TrackedEntity ? ((ChunkMap.TrackedEntity)object).entity.getId() == this.entity.getId() : false;
		}

		public int hashCode() {
			return this.entity.getId();
		}

		public void broadcast(Packet<?> packet) {
			for (ServerPlayerConnection serverPlayerConnection : this.seenBy) {
				serverPlayerConnection.send(packet);
			}
		}

		public void broadcastAndSend(Packet<?> packet) {
			this.broadcast(packet);
			if (this.entity instanceof ServerPlayer) {
				((ServerPlayer)this.entity).connection.send(packet);
			}
		}

		public void broadcastRemoved() {
			for (ServerPlayerConnection serverPlayerConnection : this.seenBy) {
				this.serverEntity.removePairing(serverPlayerConnection.getPlayer());
			}
		}

		public void removePlayer(ServerPlayer serverPlayer) {
			if (this.seenBy.remove(serverPlayer.connection)) {
				this.serverEntity.removePairing(serverPlayer);
			}
		}

		public void updatePlayer(ServerPlayer serverPlayer) {
			if (serverPlayer != this.entity) {
				Vec3 vec3 = serverPlayer.position().subtract(this.serverEntity.sentPos());
				int i = Math.min(this.getEffectiveRange(), (ChunkMap.this.viewDistance - 1) * 16);
				boolean bl = vec3.x >= (double)(-i) && vec3.x <= (double)i && vec3.z >= (double)(-i) && vec3.z <= (double)i && this.entity.broadcastToPlayer(serverPlayer);
				if (bl) {
					if (this.seenBy.add(serverPlayer.connection)) {
						this.serverEntity.addPairing(serverPlayer);
					}
				} else if (this.seenBy.remove(serverPlayer.connection)) {
					this.serverEntity.removePairing(serverPlayer);
				}
			}
		}

		private int scaledRange(int i) {
			return ChunkMap.this.level.getServer().getScaledTrackingDistance(i);
		}

		private int getEffectiveRange() {
			int i = this.range;

			for (Entity entity : this.entity.getIndirectPassengers()) {
				int j = entity.getType().clientTrackingRange() * 16;
				if (j > i) {
					i = j;
				}
			}

			return this.scaledRange(i);
		}

		public void updatePlayers(List<ServerPlayer> list) {
			for (ServerPlayer serverPlayer : list) {
				this.updatePlayer(serverPlayer);
			}
		}
	}
}
