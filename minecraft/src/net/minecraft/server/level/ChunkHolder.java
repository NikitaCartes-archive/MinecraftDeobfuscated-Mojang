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
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
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

public class ChunkHolder {
	public static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
	public static final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(
		UNLOADED_CHUNK
	);
	public static final Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
	private static final CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(
		UNLOADED_LEVEL_CHUNK
	);
	private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
	private static final ChunkHolder.FullChunkStatus[] FULL_CHUNK_STATUSES = ChunkHolder.FullChunkStatus.values();
	private static final int BLOCKS_BEFORE_RESEND_FUDGE = 64;
	private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new AtomicReferenceArray(
		CHUNK_STATUSES.size()
	);
	private final LevelHeightAccessor levelHeightAccessor;
	private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
	@Nullable
	private final DebugBuffer<ChunkHolder.ChunkSaveDebug> chunkToSaveHistory = null;
	private int oldTicketLevel;
	private int ticketLevel;
	private int queueLevel;
	final ChunkPos pos;
	private boolean hasChangedSections;
	private final ShortSet[] changedBlocksPerSection;
	private final BitSet blockChangedLightSectionFilter = new BitSet();
	private final BitSet skyChangedLightSectionFilter = new BitSet();
	private final LevelLightEngine lightEngine;
	private final ChunkHolder.LevelChangeListener onLevelChange;
	private final ChunkHolder.PlayerProvider playerProvider;
	private boolean wasAccessibleSinceLastSave;
	private boolean resendLight;
	private CompletableFuture<Void> pendingFullStateConfirmation = CompletableFuture.completedFuture(null);

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
		this.oldTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;
		this.ticketLevel = this.oldTicketLevel;
		this.queueLevel = this.oldTicketLevel;
		this.setTicketLevel(i);
		this.changedBlocksPerSection = new ShortSet[levelHeightAccessor.getSectionsCount()];
	}

	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus chunkStatus) {
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)this.futures
			.get(chunkStatus.getIndex());
		return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
	}

	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus chunkStatus) {
		return getStatus(this.ticketLevel).isOrAfter(chunkStatus) ? this.getFutureIfPresentUnchecked(chunkStatus) : UNLOADED_CHUNK_FUTURE;
	}

	public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture() {
		return this.tickingChunkFuture;
	}

	public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getEntityTickingChunkFuture() {
		return this.entityTickingChunkFuture;
	}

	public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getFullChunkFuture() {
		return this.fullChunkFuture;
	}

	@Nullable
	public LevelChunk getTickingChunk() {
		CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getTickingChunkFuture();
		Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = (Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)completableFuture.getNow(null);
		return either == null ? null : (LevelChunk)either.left().orElse(null);
	}

	@Nullable
	public ChunkStatus getLastAvailableStatus() {
		for (int i = CHUNK_STATUSES.size() - 1; i >= 0; i--) {
			ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getFutureIfPresentUnchecked(chunkStatus);
			if (((Either)completableFuture.getNow(UNLOADED_CHUNK)).left().isPresent()) {
				return chunkStatus;
			}
		}

		return null;
	}

	@Nullable
	public ChunkAccess getLastAvailable() {
		for (int i = CHUNK_STATUSES.size() - 1; i >= 0; i--) {
			ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getFutureIfPresentUnchecked(chunkStatus);
			if (!completableFuture.isCompletedExceptionally()) {
				Optional<ChunkAccess> optional = ((Either)completableFuture.getNow(UNLOADED_CHUNK)).left();
				if (optional.isPresent()) {
					return (ChunkAccess)optional.get();
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
		LevelChunk levelChunk = this.getTickingChunk();
		if (levelChunk != null) {
			levelChunk.setUnsaved(true);
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

	public void broadcastChanges(LevelChunk levelChunk) {
		if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
			Level level = levelChunk.getLevel();
			int i = 0;

			for (int j = 0; j < this.changedBlocksPerSection.length; j++) {
				i += this.changedBlocksPerSection[j] != null ? this.changedBlocksPerSection[j].size() : 0;
			}

			this.resendLight |= i >= 64;
			if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
				this.broadcast(
					new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter, true),
					!this.resendLight
				);
				this.skyChangedLightSectionFilter.clear();
				this.blockChangedLightSectionFilter.clear();
			}

			for (int j = 0; j < this.changedBlocksPerSection.length; j++) {
				ShortSet shortSet = this.changedBlocksPerSection[j];
				if (shortSet != null) {
					int k = this.levelHeightAccessor.getSectionYFromSectionIndex(j);
					SectionPos sectionPos = SectionPos.of(levelChunk.getPos(), k);
					if (shortSet.size() == 1) {
						BlockPos blockPos = sectionPos.relativeToBlockPos(shortSet.iterator().nextShort());
						BlockState blockState = level.getBlockState(blockPos);
						this.broadcast(new ClientboundBlockUpdatePacket(blockPos, blockState), false);
						this.broadcastBlockEntityIfNeeded(level, blockPos, blockState);
					} else {
						LevelChunkSection levelChunkSection = levelChunk.getSections()[j];
						ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket = new ClientboundSectionBlocksUpdatePacket(
							sectionPos, shortSet, levelChunkSection, this.resendLight
						);
						this.broadcast(clientboundSectionBlocksUpdatePacket, false);
						clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) -> this.broadcastBlockEntityIfNeeded(level, blockPos, blockState));
					}

					this.changedBlocksPerSection[j] = null;
				}
			}

			this.hasChangedSections = false;
		}
	}

	private void broadcastBlockEntityIfNeeded(Level level, BlockPos blockPos, BlockState blockState) {
		if (blockState.hasBlockEntity()) {
			this.broadcastBlockEntity(level, blockPos);
		}
	}

	private void broadcastBlockEntity(Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity != null) {
			ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket = blockEntity.getUpdatePacket();
			if (clientboundBlockEntityDataPacket != null) {
				this.broadcast(clientboundBlockEntityDataPacket, false);
			}
		}
	}

	private void broadcast(Packet<?> packet, boolean bl) {
		this.playerProvider.getPlayers(this.pos, bl).forEach(serverPlayer -> serverPlayer.connection.send(packet));
	}

	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus chunkStatus, ChunkMap chunkMap) {
		int i = chunkStatus.getIndex();
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)this.futures
			.get(i);
		if (completableFuture != null) {
			Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = (Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)completableFuture.getNow(null);
			boolean bl = either != null && either.right().isPresent();
			if (!bl) {
				return completableFuture;
			}
		}

		if (getStatus(this.ticketLevel).isOrAfter(chunkStatus)) {
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture2 = chunkMap.schedule(this, chunkStatus);
			this.updateChunkToSave(completableFuture2, "schedule " + chunkStatus);
			this.futures.set(i, completableFuture2);
			return completableFuture2;
		} else {
			return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
		}
	}

	private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture, String string) {
		if (this.chunkToSaveHistory != null) {
			this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), completableFuture, string));
		}

		this.chunkToSave = this.chunkToSave
			.thenCombine(completableFuture, (chunkAccess, either) -> either.map(chunkAccessx -> chunkAccessx, chunkLoadingFailure -> chunkAccess));
	}

	public ChunkHolder.FullChunkStatus getFullStatus() {
		return getFullChunkStatus(this.ticketLevel);
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
		ChunkMap chunkMap,
		CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture,
		Executor executor,
		ChunkHolder.FullChunkStatus fullChunkStatus
	) {
		this.pendingFullStateConfirmation.cancel(false);
		CompletableFuture<Void> completableFuture2 = new CompletableFuture();
		completableFuture2.thenRunAsync(() -> chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus), executor);
		this.pendingFullStateConfirmation = completableFuture2;
		completableFuture.thenAccept(either -> either.ifLeft(levelChunk -> completableFuture2.complete(null)));
	}

	private void demoteFullChunk(ChunkMap chunkMap, ChunkHolder.FullChunkStatus fullChunkStatus) {
		this.pendingFullStateConfirmation.cancel(false);
		chunkMap.onFullChunkStatusChange(this.pos, fullChunkStatus);
	}

	protected void updateFutures(ChunkMap chunkMap, Executor executor) {
		ChunkStatus chunkStatus = getStatus(this.oldTicketLevel);
		ChunkStatus chunkStatus2 = getStatus(this.ticketLevel);
		boolean bl = this.oldTicketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
		boolean bl2 = this.ticketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
		ChunkHolder.FullChunkStatus fullChunkStatus = getFullChunkStatus(this.oldTicketLevel);
		ChunkHolder.FullChunkStatus fullChunkStatus2 = getFullChunkStatus(this.ticketLevel);
		if (bl) {
			Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = Either.right(new ChunkHolder.ChunkLoadingFailure() {
				public String toString() {
					return "Unloaded ticket level " + ChunkHolder.this.pos;
				}
			});

			for (int i = bl2 ? chunkStatus2.getIndex() + 1 : 0; i <= chunkStatus.getIndex(); i++) {
				CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)this.futures
					.get(i);
				if (completableFuture == null) {
					this.futures.set(i, CompletableFuture.completedFuture(either));
				}
			}
		}

		boolean bl3 = fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
		boolean bl4 = fullChunkStatus2.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
		this.wasAccessibleSinceLastSave |= bl4;
		if (!bl3 && bl4) {
			this.fullChunkFuture = chunkMap.prepareAccessibleChunk(this);
			this.scheduleFullChunkPromotion(chunkMap, this.fullChunkFuture, executor, ChunkHolder.FullChunkStatus.BORDER);
			this.updateChunkToSave(this.fullChunkFuture, "full");
		}

		if (bl3 && !bl4) {
			CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.fullChunkFuture;
			this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
			this.updateChunkToSave(completableFuture.thenApply(either -> either.ifLeft(chunkMap::packTicks)), "unfull");
		}

		boolean bl5 = fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
		boolean bl6 = fullChunkStatus2.isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
		if (!bl5 && bl6) {
			this.tickingChunkFuture = chunkMap.prepareTickingChunk(this);
			this.scheduleFullChunkPromotion(chunkMap, this.tickingChunkFuture, executor, ChunkHolder.FullChunkStatus.TICKING);
			this.updateChunkToSave(this.tickingChunkFuture, "ticking");
		}

		if (bl5 && !bl6) {
			this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
			this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
		}

		boolean bl7 = fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
		boolean bl8 = fullChunkStatus2.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
		if (!bl7 && bl8) {
			if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
				throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
			}

			this.entityTickingChunkFuture = chunkMap.prepareEntityTickingChunk(this.pos);
			this.scheduleFullChunkPromotion(chunkMap, this.entityTickingChunkFuture, executor, ChunkHolder.FullChunkStatus.ENTITY_TICKING);
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
		return i < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(i - 33);
	}

	public static ChunkHolder.FullChunkStatus getFullChunkStatus(int i) {
		return FULL_CHUNK_STATUSES[Mth.clamp(33 - i + 1, 0, FULL_CHUNK_STATUSES.length - 1)];
	}

	public boolean wasAccessibleSinceLastSave() {
		return this.wasAccessibleSinceLastSave;
	}

	public void refreshAccessibility() {
		this.wasAccessibleSinceLastSave = getFullChunkStatus(this.ticketLevel).isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
	}

	public void replaceProtoChunk(ImposterProtoChunk imposterProtoChunk) {
		for (int i = 0; i < this.futures.length(); i++) {
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)this.futures
				.get(i);
			if (completableFuture != null) {
				Optional<ChunkAccess> optional = ((Either)completableFuture.getNow(UNLOADED_CHUNK)).left();
				if (optional.isPresent() && optional.get() instanceof ProtoChunk) {
					this.futures.set(i, CompletableFuture.completedFuture(Either.left(imposterProtoChunk)));
				}
			}
		}

		this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(imposterProtoChunk.getWrapped())), "replaceProto");
	}

	public interface ChunkLoadingFailure {
		ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
			public String toString() {
				return "UNLOADED";
			}
		};
	}

	static final class ChunkSaveDebug {
		private final Thread thread;
		private final CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future;
		private final String source;

		ChunkSaveDebug(Thread thread, CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture, String string) {
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

		public boolean isOrAfter(ChunkHolder.FullChunkStatus fullChunkStatus) {
			return this.ordinal() >= fullChunkStatus.ordinal();
		}
	}

	@FunctionalInterface
	public interface LevelChangeListener {
		void onLevelChange(ChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer);
	}

	public interface PlayerProvider {
		Stream<ServerPlayer> getPlayers(ChunkPos chunkPos, boolean bl);
	}
}
