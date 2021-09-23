package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DistanceManager {
	static final Logger LOGGER = LogManager.getLogger();
	private static final int ENTITY_TICKING_RANGE = 2;
	static final int PLAYER_TICKET_LEVEL = 33 + ChunkStatus.getDistance(ChunkStatus.FULL) - 2;
	private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
	private static final int ENTITY_TICKING_LEVEL_THRESHOLD = 32;
	private static final int BLOCK_TICKING_LEVEL_THRESHOLD = 33;
	final Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk = new Long2ObjectOpenHashMap<>();
	final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();
	private final DistanceManager.ChunkTicketTracker ticketTracker = new DistanceManager.ChunkTicketTracker();
	private final DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter = new DistanceManager.FixedPlayerDistanceChunkTracker(8);
	private final TickingTracker tickingTicketsTracker = new TickingTracker();
	private final DistanceManager.PlayerTicketTracker playerTicketManager = new DistanceManager.PlayerTicketTracker(33);
	final Set<ChunkHolder> chunksToUpdateFutures = Sets.<ChunkHolder>newHashSet();
	final ChunkTaskPriorityQueueSorter ticketThrottler;
	final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> ticketThrottlerInput;
	final ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
	final LongSet ticketsToRelease = new LongOpenHashSet();
	final Executor mainThreadExecutor;
	private long ticketTickCounter;
	private int simulationDistance = 10;

	protected DistanceManager(Executor executor, Executor executor2) {
		ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("player ticket throttler", executor2::execute);
		ChunkTaskPriorityQueueSorter chunkTaskPriorityQueueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorHandle), executor, 4);
		this.ticketThrottler = chunkTaskPriorityQueueSorter;
		this.ticketThrottlerInput = chunkTaskPriorityQueueSorter.getProcessor(processorHandle, true);
		this.ticketThrottlerReleaser = chunkTaskPriorityQueueSorter.getReleaseProcessor(processorHandle);
		this.mainThreadExecutor = executor2;
	}

	protected void purgeStaleTickets() {
		this.ticketTickCounter++;
		ObjectIterator<Entry<SortedArraySet<Ticket<?>>>> objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();

		while (objectIterator.hasNext()) {
			Entry<SortedArraySet<Ticket<?>>> entry = (Entry<SortedArraySet<Ticket<?>>>)objectIterator.next();
			Iterator<Ticket<?>> iterator = ((SortedArraySet)entry.getValue()).iterator();
			boolean bl = false;

			while (iterator.hasNext()) {
				Ticket<?> ticket = (Ticket<?>)iterator.next();
				if (ticket.timedOut(this.ticketTickCounter)) {
					iterator.remove();
					bl = true;
					this.tickingTicketsTracker.removeTicket(entry.getLongKey(), ticket);
				}
			}

			if (bl) {
				this.ticketTracker.update(entry.getLongKey(), getTicketLevelAt((SortedArraySet<Ticket<?>>)entry.getValue()), false);
			}

			if (((SortedArraySet)entry.getValue()).isEmpty()) {
				objectIterator.remove();
			}
		}
	}

	private static int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedArraySet) {
		return !sortedArraySet.isEmpty() ? sortedArraySet.first().getTicketLevel() : ChunkMap.MAX_CHUNK_DISTANCE + 1;
	}

	protected abstract boolean isChunkToRemove(long l);

	@Nullable
	protected abstract ChunkHolder getChunk(long l);

	@Nullable
	protected abstract ChunkHolder updateChunkScheduling(long l, int i, @Nullable ChunkHolder chunkHolder, int j);

	public boolean runAllUpdates(ChunkMap chunkMap) {
		this.naturalSpawnChunkCounter.runAllUpdates();
		this.tickingTicketsTracker.runAllUpdates();
		this.playerTicketManager.runAllUpdates();
		int i = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
		boolean bl = i != 0;
		if (bl) {
		}

		if (!this.chunksToUpdateFutures.isEmpty()) {
			this.chunksToUpdateFutures.forEach(chunkHolderx -> chunkHolderx.updateFutures(chunkMap, this.mainThreadExecutor));
			this.chunksToUpdateFutures.clear();
			return true;
		} else {
			if (!this.ticketsToRelease.isEmpty()) {
				LongIterator longIterator = this.ticketsToRelease.iterator();

				while (longIterator.hasNext()) {
					long l = longIterator.nextLong();
					if (this.getTickets(l).stream().anyMatch(ticket -> ticket.getType() == TicketType.PLAYER)) {
						ChunkHolder chunkHolder = chunkMap.getUpdatingChunkIfPresent(l);
						if (chunkHolder == null) {
							throw new IllegalStateException();
						}

						CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getEntityTickingChunkFuture();
						completableFuture.thenAccept(
							either -> this.mainThreadExecutor.execute(() -> this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
									}, l, false)))
						);
					}
				}

				this.ticketsToRelease.clear();
			}

			return bl;
		}
	}

	void addTicket(long l, Ticket<?> ticket) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
		int i = getTicketLevelAt(sortedArraySet);
		Ticket<?> ticket2 = sortedArraySet.addOrGet(ticket);
		ticket2.setCreatedTick(this.ticketTickCounter);
		if (ticket.getTicketLevel() < i) {
			this.ticketTracker.update(l, ticket.getTicketLevel(), true);
		}
	}

	void removeTicket(long l, Ticket<?> ticket) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
		if (sortedArraySet.remove(ticket)) {
		}

		if (sortedArraySet.isEmpty()) {
			this.tickets.remove(l);
		}

		this.ticketTracker.update(l, getTicketLevelAt(sortedArraySet), false);
	}

	public <T> void addTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
		this.addTicket(chunkPos.toLong(), new Ticket<>(ticketType, i, object));
	}

	public <T> void removeTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
		Ticket<T> ticket = new Ticket<>(ticketType, i, object);
		this.removeTicket(chunkPos.toLong(), ticket);
	}

	public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
		Ticket<T> ticket = new Ticket<>(ticketType, 33 - i, object);
		long l = chunkPos.toLong();
		this.addTicket(l, ticket);
		this.tickingTicketsTracker.addTicket(l, ticket);
	}

	public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
		Ticket<T> ticket = new Ticket<>(ticketType, 33 - i, object);
		long l = chunkPos.toLong();
		this.removeTicket(l, ticket);
		this.tickingTicketsTracker.removeTicket(l, ticket);
	}

	private SortedArraySet<Ticket<?>> getTickets(long l) {
		return this.tickets.computeIfAbsent(l, lx -> SortedArraySet.create(4));
	}

	protected void updateChunkForced(ChunkPos chunkPos, boolean bl) {
		Ticket<ChunkPos> ticket = new Ticket<>(TicketType.FORCED, 31, chunkPos);
		long l = chunkPos.toLong();
		if (bl) {
			this.addTicket(l, ticket);
			this.tickingTicketsTracker.addTicket(l, ticket);
		} else {
			this.removeTicket(l, ticket);
			this.tickingTicketsTracker.removeTicket(l, ticket);
		}
	}

	public void addPlayer(SectionPos sectionPos, ServerPlayer serverPlayer) {
		ChunkPos chunkPos = sectionPos.chunk();
		long l = chunkPos.toLong();
		this.playersPerChunk.computeIfAbsent(l, lx -> new ObjectOpenHashSet()).add(serverPlayer);
		this.naturalSpawnChunkCounter.update(l, 0, true);
		this.playerTicketManager.update(l, 0, true);
		this.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
	}

	public void removePlayer(SectionPos sectionPos, ServerPlayer serverPlayer) {
		ChunkPos chunkPos = sectionPos.chunk();
		long l = chunkPos.toLong();
		ObjectSet<ServerPlayer> objectSet = this.playersPerChunk.get(l);
		objectSet.remove(serverPlayer);
		if (objectSet.isEmpty()) {
			this.playersPerChunk.remove(l);
			this.naturalSpawnChunkCounter.update(l, Integer.MAX_VALUE, false);
			this.playerTicketManager.update(l, Integer.MAX_VALUE, false);
			this.tickingTicketsTracker.removeTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
		}
	}

	private int getPlayerTicketLevel() {
		return Math.max(0, 31 - this.simulationDistance);
	}

	public boolean inEntityTickingRange(long l) {
		return this.tickingTicketsTracker.getLevel(l) < 32;
	}

	public boolean inBlockTickingRange(long l) {
		return this.tickingTicketsTracker.getLevel(l) < 33;
	}

	protected String getTicketDebugString(long l) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.tickets.get(l);
		return sortedArraySet != null && !sortedArraySet.isEmpty() ? sortedArraySet.first().toString() : "no_ticket";
	}

	protected void updatePlayerTickets(int i) {
		this.playerTicketManager.updateViewDistance(i);
	}

	public void updateSimulationDistance(int i) {
		if (i != this.simulationDistance) {
			this.simulationDistance = i;
			this.tickingTicketsTracker.replacePlayerTicketsLevel(this.getPlayerTicketLevel());
		}
	}

	public int getNaturalSpawnChunkCount() {
		this.naturalSpawnChunkCounter.runAllUpdates();
		return this.naturalSpawnChunkCounter.chunks.size();
	}

	public boolean hasPlayersNearby(long l) {
		this.naturalSpawnChunkCounter.runAllUpdates();
		return this.naturalSpawnChunkCounter.chunks.containsKey(l);
	}

	public String getDebugStatus() {
		return this.ticketThrottler.getDebugStatus();
	}

	private void dumpTickets(String string) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(new File(string));

			try {
				for (Entry<SortedArraySet<Ticket<?>>> entry : this.tickets.long2ObjectEntrySet()) {
					ChunkPos chunkPos = new ChunkPos(entry.getLongKey());

					for (Ticket<?> ticket : (SortedArraySet)entry.getValue()) {
						fileOutputStream.write(
							(chunkPos.x + "\t" + chunkPos.z + "\t" + ticket.getType() + "\t" + ticket.getTicketLevel() + "\t\n").getBytes(StandardCharsets.UTF_8)
						);
					}
				}
			} catch (Throwable var9) {
				try {
					fileOutputStream.close();
				} catch (Throwable var8) {
					var9.addSuppressed(var8);
				}

				throw var9;
			}

			fileOutputStream.close();
		} catch (IOException var10) {
			LOGGER.error(var10);
		}
	}

	@VisibleForTesting
	TickingTracker tickingTracker() {
		return this.tickingTicketsTracker;
	}

	class ChunkTicketTracker extends ChunkTracker {
		public ChunkTicketTracker() {
			super(ChunkMap.MAX_CHUNK_DISTANCE + 2, 16, 256);
		}

		@Override
		protected int getLevelFromSource(long l) {
			SortedArraySet<Ticket<?>> sortedArraySet = DistanceManager.this.tickets.get(l);
			if (sortedArraySet == null) {
				return Integer.MAX_VALUE;
			} else {
				return sortedArraySet.isEmpty() ? Integer.MAX_VALUE : sortedArraySet.first().getTicketLevel();
			}
		}

		@Override
		protected int getLevel(long l) {
			if (!DistanceManager.this.isChunkToRemove(l)) {
				ChunkHolder chunkHolder = DistanceManager.this.getChunk(l);
				if (chunkHolder != null) {
					return chunkHolder.getTicketLevel();
				}
			}

			return ChunkMap.MAX_CHUNK_DISTANCE + 1;
		}

		@Override
		protected void setLevel(long l, int i) {
			ChunkHolder chunkHolder = DistanceManager.this.getChunk(l);
			int j = chunkHolder == null ? ChunkMap.MAX_CHUNK_DISTANCE + 1 : chunkHolder.getTicketLevel();
			if (j != i) {
				chunkHolder = DistanceManager.this.updateChunkScheduling(l, i, chunkHolder, j);
				if (chunkHolder != null) {
					DistanceManager.this.chunksToUpdateFutures.add(chunkHolder);
				}
			}
		}

		public int runDistanceUpdates(int i) {
			return this.runUpdates(i);
		}
	}

	class FixedPlayerDistanceChunkTracker extends ChunkTracker {
		protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
		protected final int maxDistance;

		protected FixedPlayerDistanceChunkTracker(int i) {
			super(i + 2, 16, 256);
			this.maxDistance = i;
			this.chunks.defaultReturnValue((byte)(i + 2));
		}

		@Override
		protected int getLevel(long l) {
			return this.chunks.get(l);
		}

		@Override
		protected void setLevel(long l, int i) {
			byte b;
			if (i > this.maxDistance) {
				b = this.chunks.remove(l);
			} else {
				b = this.chunks.put(l, (byte)i);
			}

			this.onLevelChange(l, b, i);
		}

		protected void onLevelChange(long l, int i, int j) {
		}

		@Override
		protected int getLevelFromSource(long l) {
			return this.havePlayer(l) ? 0 : Integer.MAX_VALUE;
		}

		private boolean havePlayer(long l) {
			ObjectSet<ServerPlayer> objectSet = DistanceManager.this.playersPerChunk.get(l);
			return objectSet != null && !objectSet.isEmpty();
		}

		public void runAllUpdates() {
			this.runUpdates(Integer.MAX_VALUE);
		}

		private void dumpChunks(String string) {
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(new File(string));

				try {
					for (it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
						ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
						String string2 = Byte.toString(entry.getByteValue());
						fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + string2 + "\n").getBytes(StandardCharsets.UTF_8));
					}
				} catch (Throwable var8) {
					try {
						fileOutputStream.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}

					throw var8;
				}

				fileOutputStream.close();
			} catch (IOException var9) {
				DistanceManager.LOGGER.error(var9);
			}
		}
	}

	class PlayerTicketTracker extends DistanceManager.FixedPlayerDistanceChunkTracker {
		private int viewDistance;
		private final Long2IntMap queueLevels = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
		private final LongSet toUpdate = new LongOpenHashSet();

		protected PlayerTicketTracker(int i) {
			super(i);
			this.viewDistance = 0;
			this.queueLevels.defaultReturnValue(i + 2);
		}

		@Override
		protected void onLevelChange(long l, int i, int j) {
			this.toUpdate.add(l);
		}

		public void updateViewDistance(int i) {
			for (it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
				byte b = entry.getByteValue();
				long l = entry.getLongKey();
				this.onLevelChange(l, b, this.haveTicketFor(b), b <= i - 2);
			}

			this.viewDistance = i;
		}

		private void onLevelChange(long l, int i, boolean bl, boolean bl2) {
			if (bl != bl2) {
				Ticket<?> ticket = new Ticket<>(TicketType.PLAYER, DistanceManager.PLAYER_TICKET_LEVEL, new ChunkPos(l));
				if (bl2) {
					DistanceManager.this.ticketThrottlerInput
						.tell(ChunkTaskPriorityQueueSorter.message((Runnable)(() -> DistanceManager.this.mainThreadExecutor.execute(() -> {
								if (this.haveTicketFor(this.getLevel(l))) {
									DistanceManager.this.addTicket(l, ticket);
									DistanceManager.this.ticketsToRelease.add(l);
								} else {
									DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
									}, l, false));
								}
							})), l, () -> i));
				} else {
					DistanceManager.this.ticketThrottlerReleaser
						.tell(
							ChunkTaskPriorityQueueSorter.release(() -> DistanceManager.this.mainThreadExecutor.execute(() -> DistanceManager.this.removeTicket(l, ticket)), l, true)
						);
				}
			}
		}

		@Override
		public void runAllUpdates() {
			super.runAllUpdates();
			if (!this.toUpdate.isEmpty()) {
				LongIterator longIterator = this.toUpdate.iterator();

				while (longIterator.hasNext()) {
					long l = longIterator.nextLong();
					int i = this.queueLevels.get(l);
					int j = this.getLevel(l);
					if (i != j) {
						DistanceManager.this.ticketThrottler.onLevelChange(new ChunkPos(l), () -> this.queueLevels.get(l), j, ix -> {
							if (ix >= this.queueLevels.defaultReturnValue()) {
								this.queueLevels.remove(l);
							} else {
								this.queueLevels.put(l, ix);
							}
						});
						this.onLevelChange(l, j, this.haveTicketFor(i), this.haveTicketFor(j));
					}
				}

				this.toUpdate.clear();
			}
		}

		private boolean haveTicketFor(int i) {
			return i <= this.viewDistance - 2;
		}
	}
}
