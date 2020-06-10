package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChunkBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
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
	private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new AtomicReferenceArray(
		CHUNK_STATUSES.size()
	);
	private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
	private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
	private int oldTicketLevel;
	private int ticketLevel;
	private int queueLevel;
	private final ChunkPos pos;
	private final short[] changedBlocks = new short[64];
	private int changes;
	private int changedSectionFilter;
	private boolean forceSendLight;
	private int blockChangedLightSectionFilter;
	private int skyChangedLightSectionFilter;
	private final LevelLightEngine lightEngine;
	private final ChunkHolder.LevelChangeListener onLevelChange;
	private final ChunkHolder.PlayerProvider playerProvider;
	private boolean wasAccessibleSinceLastSave;

	public ChunkHolder(
		ChunkPos chunkPos, int i, LevelLightEngine levelLightEngine, ChunkHolder.LevelChangeListener levelChangeListener, ChunkHolder.PlayerProvider playerProvider
	) {
		this.pos = chunkPos;
		this.lightEngine = levelLightEngine;
		this.onLevelChange = levelChangeListener;
		this.playerProvider = playerProvider;
		this.oldTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;
		this.ticketLevel = this.oldTicketLevel;
		this.queueLevel = this.oldTicketLevel;
		this.setTicketLevel(i);
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
	@Environment(EnvType.CLIENT)
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

	public void blockChanged(int i, int j, int k) {
		LevelChunk levelChunk = this.getTickingChunk();
		if (levelChunk != null) {
			this.changedSectionFilter |= 1 << (j >> 4);
			if (this.changes < 64) {
				short s = (short)(i << 12 | k << 8 | j);

				for (int l = 0; l < this.changes; l++) {
					if (this.changedBlocks[l] == s) {
						return;
					}
				}

				this.changedBlocks[this.changes++] = s;
			}
		}
	}

	public void sectionLightChanged(LightLayer lightLayer, int i) {
		LevelChunk levelChunk = this.getTickingChunk();
		if (levelChunk != null) {
			levelChunk.setUnsaved(true);
			if (lightLayer == LightLayer.SKY) {
				this.skyChangedLightSectionFilter |= 1 << i - -1;
			} else {
				this.blockChangedLightSectionFilter |= 1 << i - -1;
			}
		}
	}

	public void broadcastChanges(LevelChunk levelChunk) {
		if (this.changes != 0 || this.skyChangedLightSectionFilter != 0 || this.blockChangedLightSectionFilter != 0) {
			Level level = levelChunk.getLevel();
			if (this.changes == 64) {
				this.forceSendLight = true;
			}

			boolean bl = !this.forceSendLight;
			this.forceSendLight = this.forceSendLight && this.lightEngine.hasLightWork();
			if (this.skyChangedLightSectionFilter != 0 || this.blockChangedLightSectionFilter != 0) {
				this.broadcast(
					new ClientboundLightUpdatePacket(levelChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter, bl), bl
				);
				this.skyChangedLightSectionFilter = 0;
				this.blockChangedLightSectionFilter = 0;
			}

			if (this.changes == 1) {
				int i = (this.changedBlocks[0] >> 12 & 15) + this.pos.x * 16;
				int j = this.changedBlocks[0] & 255;
				int k = (this.changedBlocks[0] >> 8 & 15) + this.pos.z * 16;
				BlockPos blockPos = new BlockPos(i, j, k);
				this.broadcast(new ClientboundBlockUpdatePacket(level, blockPos), false);
				if (level.getBlockState(blockPos).getBlock().isEntityBlock()) {
					this.broadcastBlockEntity(level, blockPos);
				}
			} else if (this.changes == 64) {
				this.broadcast(new ClientboundLevelChunkPacket(levelChunk, this.changedSectionFilter), false);
			} else if (this.changes != 0) {
				this.broadcast(new ClientboundChunkBlocksUpdatePacket(this.changes, this.changedBlocks, levelChunk), false);

				for (int i = 0; i < this.changes; i++) {
					int j = (this.changedBlocks[i] >> 12 & 15) + this.pos.x * 16;
					int k = this.changedBlocks[i] & 255;
					int l = (this.changedBlocks[i] >> 8 & 15) + this.pos.z * 16;
					BlockPos blockPos2 = new BlockPos(j, k, l);
					if (level.getBlockState(blockPos2).getBlock().isEntityBlock()) {
						this.broadcastBlockEntity(level, blockPos2);
					}
				}
			}

			this.changes = 0;
			this.changedSectionFilter = 0;
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
			if (either == null || either.left().isPresent()) {
				return completableFuture;
			}
		}

		if (getStatus(this.ticketLevel).isOrAfter(chunkStatus)) {
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture2 = chunkMap.schedule(this, chunkStatus);
			this.updateChunkToSave(completableFuture2);
			this.futures.set(i, completableFuture2);
			return completableFuture2;
		} else {
			return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
		}
	}

	private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture) {
		this.chunkToSave = this.chunkToSave
			.thenCombine(completableFuture, (chunkAccess, either) -> either.map(chunkAccessx -> chunkAccessx, chunkLoadingFailure -> chunkAccess));
	}

	@Environment(EnvType.CLIENT)
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

	protected void updateFutures(ChunkMap chunkMap) {
		ChunkStatus chunkStatus = getStatus(this.oldTicketLevel);
		ChunkStatus chunkStatus2 = getStatus(this.ticketLevel);
		boolean bl = this.oldTicketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
		boolean bl2 = this.ticketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
		ChunkHolder.FullChunkStatus fullChunkStatus = getFullChunkStatus(this.oldTicketLevel);
		ChunkHolder.FullChunkStatus fullChunkStatus2 = getFullChunkStatus(this.ticketLevel);
		if (bl) {
			Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = Either.right(new ChunkHolder.ChunkLoadingFailure() {
				public String toString() {
					return "Unloaded ticket level " + ChunkHolder.this.pos.toString();
				}
			});

			for (int i = bl2 ? chunkStatus2.getIndex() + 1 : 0; i <= chunkStatus.getIndex(); i++) {
				CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)this.futures
					.get(i);
				if (completableFuture != null) {
					completableFuture.complete(either);
				} else {
					this.futures.set(i, CompletableFuture.completedFuture(either));
				}
			}
		}

		boolean bl3 = fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
		boolean bl4 = fullChunkStatus2.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
		this.wasAccessibleSinceLastSave |= bl4;
		if (!bl3 && bl4) {
			this.fullChunkFuture = chunkMap.unpackTicks(this);
			this.updateChunkToSave(this.fullChunkFuture);
		}

		if (bl3 && !bl4) {
			CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.fullChunkFuture;
			this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
			this.updateChunkToSave(completableFuture.thenApply(either -> either.ifLeft(chunkMap::packTicks)));
		}

		boolean bl5 = fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
		boolean bl6 = fullChunkStatus2.isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
		if (!bl5 && bl6) {
			this.tickingChunkFuture = chunkMap.postProcess(this);
			this.updateChunkToSave(this.tickingChunkFuture);
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
		return i < 33 ? ChunkStatus.FULL : ChunkStatus.getStatus(i - 33);
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

		this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(imposterProtoChunk.getWrapped())));
	}

	public interface ChunkLoadingFailure {
		ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
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

		public boolean isOrAfter(ChunkHolder.FullChunkStatus fullChunkStatus) {
			return this.ordinal() >= fullChunkStatus.ordinal();
		}
	}

	public interface LevelChangeListener {
		void onLevelChange(ChunkPos chunkPos, IntSupplier intSupplier, int i, IntConsumer intConsumer);
	}

	public interface PlayerProvider {
		Stream<ServerPlayer> getPlayers(ChunkPos chunkPos, boolean bl);
	}
}
