package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public class PlayerChunkSender {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final float MIN_CHUNKS_PER_TICK = 0.01F;
	public static final float MAX_CHUNKS_PER_TICK = 64.0F;
	private static final float START_CHUNKS_PER_TICK = 9.0F;
	private static final int MAX_UNACKNOWLEDGED_BATCHES = 10;
	private final LongSet pendingChunks = new LongOpenHashSet();
	private final boolean memoryConnection;
	private float desiredChunksPerTick = 9.0F;
	private float batchQuota;
	private int unacknowledgedBatches;
	private int maxUnacknowledgedBatches = 1;

	public PlayerChunkSender(boolean bl) {
		this.memoryConnection = bl;
	}

	public void markChunkPendingToSend(LevelChunk levelChunk) {
		this.pendingChunks.add(levelChunk.getPos().toLong());
	}

	public void dropChunk(ServerPlayer serverPlayer, ChunkPos chunkPos) {
		if (!this.pendingChunks.remove(chunkPos.toLong()) && serverPlayer.isAlive()) {
			serverPlayer.connection.send(new ClientboundForgetLevelChunkPacket(chunkPos));
		}
	}

	public void sendNextChunks(ServerPlayer serverPlayer) {
		if (this.unacknowledgedBatches < this.maxUnacknowledgedBatches) {
			float f = Math.max(1.0F, this.desiredChunksPerTick);
			this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, f);
			if (!(this.batchQuota < 1.0F)) {
				if (!this.pendingChunks.isEmpty()) {
					ServerLevel serverLevel = serverPlayer.serverLevel();
					ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
					List<LevelChunk> list = this.collectChunksToSend(chunkMap, serverPlayer.chunkPosition());
					if (!list.isEmpty()) {
						ServerGamePacketListenerImpl serverGamePacketListenerImpl = serverPlayer.connection;
						this.unacknowledgedBatches++;
						serverGamePacketListenerImpl.send(ClientboundChunkBatchStartPacket.INSTANCE);

						for (LevelChunk levelChunk : list) {
							sendChunk(serverGamePacketListenerImpl, serverLevel, levelChunk);
						}

						serverGamePacketListenerImpl.send(new ClientboundChunkBatchFinishedPacket(list.size()));
						this.batchQuota = this.batchQuota - (float)list.size();
					}
				}
			}
		}
	}

	private static void sendChunk(ServerGamePacketListenerImpl serverGamePacketListenerImpl, ServerLevel serverLevel, LevelChunk levelChunk) {
		serverGamePacketListenerImpl.send(new ClientboundLevelChunkWithLightPacket(levelChunk, serverLevel.getLightEngine(), null, null));
		ChunkPos chunkPos = levelChunk.getPos();
		DebugPackets.sendPoiPacketsForChunk(serverLevel, chunkPos);
	}

	private List<LevelChunk> collectChunksToSend(ChunkMap chunkMap, ChunkPos chunkPos) {
		int i = Mth.floor(this.batchQuota);
		List<LevelChunk> list;
		if (!this.memoryConnection && this.pendingChunks.size() > i) {
			list = ((List)this.pendingChunks.stream().collect(Comparators.least(i, Comparator.comparingInt(chunkPos::distanceSquared))))
				.stream()
				.mapToLong(Long::longValue)
				.mapToObj(chunkMap::getChunkToSend)
				.filter(Objects::nonNull)
				.toList();
		} else {
			list = this.pendingChunks
				.longStream()
				.mapToObj(chunkMap::getChunkToSend)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparingInt(levelChunkx -> chunkPos.distanceSquared(levelChunkx.getPos())))
				.toList();
		}

		for (LevelChunk levelChunk : list) {
			this.pendingChunks.remove(levelChunk.getPos().toLong());
		}

		return list;
	}

	public void onChunkBatchReceivedByClient(float f) {
		this.unacknowledgedBatches--;
		this.desiredChunksPerTick = Double.isNaN((double)f) ? 0.01F : Mth.clamp(f, 0.01F, 64.0F);
		if (this.unacknowledgedBatches == 0) {
			this.batchQuota = 1.0F;
		}

		this.maxUnacknowledgedBatches = 10;
	}

	public boolean isPending(long l) {
		return this.pendingChunks.contains(l);
	}
}
