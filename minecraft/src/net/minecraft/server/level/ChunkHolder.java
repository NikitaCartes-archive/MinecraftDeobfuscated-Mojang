package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.util.DebugBuffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ChunkHolder {
	public static final ChunkResult<ChunkAccess> UNLOADED_CHUNK = ChunkResult.error("Unloaded chunk");
	public static final CompletableFuture<ChunkResult<ChunkAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
	public static final ChunkResult<LevelChunk> UNLOADED_LEVEL_CHUNK = ChunkResult.error("Unloaded level chunk");
	public static final ChunkResult<ChunkAccess> NOT_DONE_YET = ChunkResult.error("Not done yet");
	private static final CompletableFuture<ChunkResult<LevelChunk>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_LEVEL_CHUNK);
	private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
	private final AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new AtomicReferenceArray(CHUNK_STATUSES.size());
	private final LevelHeightAccessor levelHeightAccessor;
	private volatile CompletableFuture<ChunkResult<LevelChunk>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private volatile CompletableFuture<ChunkResult<LevelChunk>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private volatile CompletableFuture<ChunkResult<LevelChunk>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
	@Nullable
	private final DebugBuffer<ChunkHolder.ChunkSaveDebug> chunkToSaveHistory = null;
	private int oldTicketLevel;
	private int ticketLevel;
	private int queueLevel;
	private final ChunkPos pos;
	private boolean hasChangedSections;
	private final ShortSet[] changedBlocksPerSection;
	private final BitSet blockChangedLightSectionFilter = new BitSet();
	private final BitSet skyChangedLightSectionFilter = new BitSet();
	private final LevelLightEngine lightEngine;
	private final ChunkHolder.LevelChangeListener onLevelChange;
	private final ChunkHolder.PlayerProvider playerProvider;
	private boolean wasAccessibleSinceLastSave;
	private CompletableFuture<Void> pendingFullStateConfirmation = CompletableFuture.completedFuture(null);
	private CompletableFuture<?> sendSync = CompletableFuture.completedFuture(null);

	public ChunkHolder(
		ChunkPos chunkPos,
		int i,
		LevelHeightAccessor levelHeightAccessor,
		LevelLightEngine levelLightEngine,
		ChunkHolder.LevelChangeListener levelChangeListener,
		ChunkHolder.PlayerProvider playerProvider
	) {
		this.pos = chunkPos;
		this.levelHeightAccessor = levelHeightAccessor;
		this.lightEngine = levelLightEngine;
		this.onLevelChange = levelChangeListener;
		this.playerProvider = playerProvider;
		this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
		this.ticketLevel = this.oldTicketLevel;
		this.queueLevel = this.oldTicketLevel;
		this.setTicketLevel(i);
		this.changedBlocksPerSection = new ShortSet[levelHeightAccessor.getSectionsCount()];
	}

	public CompletableFuture<ChunkResult<ChunkAccess>> getFutureIfPresentUnchecked(ChunkStatus chunkStatus) {
		CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(chunkStatus.getIndex());
		return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
	}

	public CompletableFuture<ChunkResult<ChunkAccess>> getFutureIfPresent(ChunkStatus chunkStatus) {
		return ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(chunkStatus) ? this.getFutureIfPresentUnchecked(chunkStatus) : UNLOADED_CHUNK_FUTURE;
	}

	public CompletableFuture<ChunkResult<LevelChunk>> getTickingChunkFuture() {
		return this.tickingChunkFuture;
	}

	public CompletableFuture<ChunkResult<LevelChunk>> getEntityTickingChunkFuture() {
		return this.entityTickingChunkFuture;
	}

	public CompletableFuture<ChunkResult<LevelChunk>> getFullChunkFuture() {
		return this.fullChunkFuture;
	}

	@Nullable
	public LevelChunk getTickingChunk() {
		return (LevelChunk)((ChunkResult)this.getTickingChunkFuture().getNow(UNLOADED_LEVEL_CHUNK)).orElse(null);
	}

	public CompletableFuture<?> getChunkSendSyncFuture() {
		return this.sendSync;
	}

	@Nullable
	public LevelChunk getChunkToSend() {
		return !this.sendSync.isDone() ? null : this.getTickingChunk();
	}

	@Nullable
	public ChunkStatus getLastAvailableStatus() {
		for (int i = CHUNK_STATUSES.size() - 1; i >= 0; i--) {
			ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.getFutureIfPresentUnchecked(chunkStatus);
			if (((ChunkResult)completableFuture.getNow(UNLOADED_CHUNK)).isSuccess()) {
				return chunkStatus;
			}
		}

		return null;
	}

	@Nullable
	public ChunkAccess getLastAvailable() {
		for (int i = CHUNK_STATUSES.size() - 1; i >= 0; i--) {
			ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = this.getFutureIfPresentUnchecked(chunkStatus);
			if (!completableFuture.isCompletedExceptionally()) {
				ChunkAccess chunkAccess = (ChunkAccess)((ChunkResult)completableFuture.getNow(UNLOADED_CHUNK)).orElse(null);
				if (chunkAccess != null) {
					return chunkAccess;
				}
			}
		}

		return null;
	}

	public CompletableFuture<ChunkAccess> getChunkToSave() {
		return this.chunkToSave;
	}

	public void blockChanged(BlockPos blockPos) {
		LevelChunk levelChunk = this.getTickingChunk();
		if (levelChunk != null) {
			int i = this.levelHeightAccessor.getSectionIndex(blockPos.getY());
			if (this.changedBlocksPerSection[i] == null) {
				this.hasChangedSections = true;
				this.changedBlocksPerSection[i] = new ShortOpenHashSet();
			}

			this.changedBlocksPerSection[i].add(SectionPos.sectionRelativePos(blockPos));
		}
	}

	public void sectionLightChanged(LightLayer lightLayer, int i) {
		ChunkAccess chunkAccess = (ChunkAccess)((ChunkResult)this.getFutureIfPresent(ChunkStatus.INITIALIZE_LIGHT).getNow(UNLOADED_CHUNK)).orElse(null);
		if (chunkAccess != null) {
			chunkAccess.setUnsaved(true);
			LevelChunk levelChunk = this.getTickingChunk();
			if (levelChunk != null) {
				int j = this.lightEngine.getMinLightSection();
				int k = this.lightEngine.getMaxLightSection();
				if (i >= j && i <= k) {
					int l = i - j;
					if (lightLayer == LightLayer.SKY) {
						this.skyChangedLightSectionFilter.set(l);
					} else {
						this.blockChangedLightSectionFilter.set(l);
					}
				}
			}
		}
	}

	public void broadcastChanges(LevelChunk levelChunk) {
		if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
			Level level = levelChunk.getLevel();
			if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
				List<ServerPlayer> list = this.playerProvider.getPlayers(this.pos, true);
				if (!list.isEmpty()) {
					ClientboundLightUpdatePacket clientboundLightUpdatePacket = new ClientboundLightUpdatePacket(
						levelChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter
					);
					this.broadcast(list, clientboundLightUpdatePacket);
				}

				this.skyChangedLightSectionFilter.clear();
				this.blockChangedLightSectionFilter.clear();
			}

			if (this.hasChangedSections) {
				List<ServerPlayer> list = this.playerProvider.getPlayers(this.pos, false);

				for (int i = 0; i < this.changedBlocksPerSection.length; i++) {
					ShortSet shortSet = this.changedBlocksPerSection[i];
					if (shortSet != null) {
						this.changedBlocksPerSection[i] = null;
						if (!list.isEmpty()) {
							int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
							SectionPos sectionPos = SectionPos.of(levelChunk.getPos(), j);
							if (shortSet.size() == 1) {
								BlockPos blockPos = sectionPos.relativeToBlockPos(shortSet.iterator().nextShort());
								BlockState blockState = level.getBlockState(blockPos);
								this.broadcast(list, new ClientboundBlockUpdatePacket(blockPos, blockState));
								this.broadcastBlockEntityIfNeeded(list, level, blockPos, blockState);
							} else {
								LevelChunkSection levelChunkSection = levelChunk.getSection(i);
								ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket = new ClientboundSectionBlocksUpdatePacket(
									sectionPos, shortSet, levelChunkSection
								);
								this.broadcast(list, clientboundSectionBlocksUpdatePacket);
								clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) -> this.broadcastBlockEntityIfNeeded(list, level, blockPos, blockState));
							}
						}
					}
				}

				this.hasChangedSections = false;
			}
		}
	}

	private void broadcastBlockEntityIfNeeded(List<ServerPlayer> list, Level level, BlockPos blockPos, BlockState blockState) {
		if (blockState.hasBlockEntity()) {
			this.broadcastBlockEntity(list, level, blockPos);
		}
	}

	private void broadcastBlockEntity(List<ServerPlayer> list, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity != null) {
			Packet<?> packet = blockEntity.getUpdatePacket();
			if (packet != null) {
				this.broadcast(list, packet);
			}
		}
	}

	private void broadcast(List<ServerPlayer> list, Packet<?> packet) {
		list.forEach(serverPlayer -> serverPlayer.connection.send(packet));
	}

	public CompletableFuture<ChunkResult<ChunkAccess>> getOrScheduleFuture(ChunkStatus chunkStatus, ChunkMap chunkMap) {
		int i = chunkStatus.getIndex();
		CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(i);
		if (completableFuture != null) {
			ChunkResult<ChunkAccess> chunkResult = (ChunkResult<ChunkAccess>)completableFuture.getNow(NOT_DONE_YET);
			if (chunkResult == null) {
				String string = "value in future for status: " + chunkStatus + " was incorrectly set to null at chunk: " + this.pos;
				throw chunkMap.debugFuturesAndCreateReportedException(new IllegalStateException("null value previously set for chunk status"), string);
			}

			if (chunkResult == NOT_DONE_YET || chunkResult.isSuccess()) {
				return completableFuture;
			}
		}

		if (ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(chunkStatus)) {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture2 = chunkMap.schedule(this, chunkStatus);
			this.updateChunkToSave(completableFuture2, "schedule " + chunkStatus);
			this.futures.set(i, completableFuture2);
			return completableFuture2;
		} else {
			return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
		}
	}

	protected void addSaveDependency(String string, CompletableFuture<?> completableFuture) {
		if (this.chunkToSaveHistory != null) {
			this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), completableFuture, string));
		}

		this.chunkToSave = this.chunkToSave.thenCombine(completableFuture, (chunkAccess, object) -> chunkAccess);
	}

	private void updateChunkToSave(CompletableFuture<? extends ChunkResult<? extends ChunkAccess>> completableFuture, String string) {
		if (this.chunkToSaveHistory != null) {
			this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), completableFuture, string));
		}

		this.chunkToSave = this.chunkToSave.thenCombine(completableFuture, (chunkAccess, chunkResult) -> ChunkResult.orElse(chunkResult, chunkAccess));
	}

	public void addSendDependency(CompletableFuture<?> completableFuture) {
		if (this.sendSync.isDone()) {
			this.sendSync = completableFuture;
		} else {
			this.sendSync = this.sendSync.thenCombine(completableFuture, (object, object2) -> null);
		}
	}

	public FullChunkStatus getFullStatus() {
		return ChunkLevel.fullStatus(this.ticketLevel);
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

	private void scheduleFullChunkPromotion(
		ChunkMap chunkMap, CompletableFuture<ChunkResult<LevelChunk>> completableFuture, Executor executor, FullChunkStatus fullChunkStatus
	) {
		this.pendingFullStateConfirmation.cancel(false);
		CompletableFuture<Void> completableFuture2 = new CompletableFuture();
		completableFuture2.thenRunAsync(() -> chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus), executor);
		this.pendingFullStateConfirmation = completableFuture2;
		completableFuture.thenAccept(chunkResult -> chunkResult.ifSuccess(levelChunk -> completableFuture2.complete(null)));
	}

	private void demoteFullChunk(ChunkMap chunkMap, FullChunkStatus fullChunkStatus) {
		this.pendingFullStateConfirmation.cancel(false);
		chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus);
	}

	protected void updateFutures(ChunkMap chunkMap, Executor executor) {
		ChunkStatus chunkStatus = ChunkLevel.generationStatus(this.oldTicketLevel);
		ChunkStatus chunkStatus2 = ChunkLevel.generationStatus(this.ticketLevel);
		boolean bl = ChunkLevel.isLoaded(this.oldTicketLevel);
		boolean bl2 = ChunkLevel.isLoaded(this.ticketLevel);
		FullChunkStatus fullChunkStatus = ChunkLevel.fullStatus(this.oldTicketLevel);
		FullChunkStatus fullChunkStatus2 = ChunkLevel.fullStatus(this.ticketLevel);
		if (bl) {
			ChunkResult<ChunkAccess> chunkResult = ChunkResult.error((Supplier<String>)(() -> "Unloaded ticket level " + this.pos));

			for (int i = bl2 ? chunkStatus2.getIndex() + 1 : 0; i <= chunkStatus.getIndex(); i++) {
				CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(i);
				if (completableFuture == null) {
					this.futures.set(i, CompletableFuture.completedFuture(chunkResult));
				}
			}
		}

		boolean bl3 = fullChunkStatus.isOrAfter(FullChunkStatus.FULL);
		boolean bl4 = fullChunkStatus2.isOrAfter(FullChunkStatus.FULL);
		this.wasAccessibleSinceLastSave |= bl4;
		if (!bl3 && bl4) {
			this.fullChunkFuture = chunkMap.prepareAccessibleChunk(this);
			this.scheduleFullChunkPromotion(chunkMap, this.fullChunkFuture, executor, FullChunkStatus.FULL);
			this.updateChunkToSave(this.fullChunkFuture, "full");
		}

		if (bl3 && !bl4) {
			this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
			this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
		}

		boolean bl5 = fullChunkStatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
		boolean bl6 = fullChunkStatus2.isOrAfter(FullChunkStatus.BLOCK_TICKING);
		if (!bl5 && bl6) {
			this.tickingChunkFuture = chunkMap.prepareTickingChunk(this);
			this.scheduleFullChunkPromotion(chunkMap, this.tickingChunkFuture, executor, FullChunkStatus.BLOCK_TICKING);
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
				throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
			}

			this.entityTickingChunkFuture = chunkMap.prepareEntityTickingChunk(this);
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

	public boolean wasAccessibleSinceLastSave() {
		return this.wasAccessibleSinceLastSave;
	}

	public void refreshAccessibility() {
		this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
	}

	public void replaceProtoChunk(ImposterProtoChunk imposterProtoChunk) {
		for (int i = 0; i < this.futures.length(); i++) {
			CompletableFuture<ChunkResult<ChunkAccess>> completableFuture = (CompletableFuture<ChunkResult<ChunkAccess>>)this.futures.get(i);
			if (completableFuture != null) {
				ChunkAccess chunkAccess = (ChunkAccess)((ChunkResult)completableFuture.getNow(UNLOADED_CHUNK)).orElse(null);
				if (chunkAccess instanceof ProtoChunk) {
					this.futures.set(i, CompletableFuture.completedFuture(ChunkResult.of(imposterProtoChunk)));
				}
			}
		}

		this.updateChunkToSave(CompletableFuture.completedFuture(ChunkResult.of(imposterProtoChunk.getWrapped())), "replaceProto");
	}

	public List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> getAllFutures() {
		List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> list = new ArrayList();

		for (int i = 0; i < CHUNK_STATUSES.size(); i++) {
			list.add(Pair.of((ChunkStatus)CHUNK_STATUSES.get(i), (CompletableFuture)this.futures.get(i)));
		}

		return list;
	}

	static record ChunkSaveDebug(Thread thread, CompletableFuture<?> future, String source) {
	}

	@FunctionalInterface
	public interface LevelChangeListener {
		void onLevelChange(ChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer);
	}

	public interface PlayerProvider {
		List<ServerPlayer> getPlayers(ChunkPos chunkPos, boolean bl);
	}
}
