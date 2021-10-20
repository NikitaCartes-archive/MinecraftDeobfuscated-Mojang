package net.minecraft.world.level;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;

public class LocalMobCapCalculator {
	private final Long2ObjectMap<List<ServerPlayer>> playersNearChunk = new Long2ObjectOpenHashMap<>();
	private final Map<ServerPlayer, LocalMobCapCalculator.MobCounts> playerMobCounts = Maps.<ServerPlayer, LocalMobCapCalculator.MobCounts>newHashMap();
	private final ChunkMap chunkMap;

	public LocalMobCapCalculator(ChunkMap chunkMap) {
		this.chunkMap = chunkMap;
	}

	private List<ServerPlayer> getPlayersNear(ChunkPos chunkPos) {
		return this.playersNearChunk
			.computeIfAbsent(chunkPos.toLong(), (Long2ObjectFunction<? extends List<ServerPlayer>>)(l -> this.chunkMap.getPlayersCloseForSpawning(chunkPos)));
	}

	public void addMob(ChunkPos chunkPos, MobCategory mobCategory) {
		for (ServerPlayer serverPlayer : this.getPlayersNear(chunkPos)) {
			((LocalMobCapCalculator.MobCounts)this.playerMobCounts.computeIfAbsent(serverPlayer, serverPlayerx -> new LocalMobCapCalculator.MobCounts()))
				.add(mobCategory);
		}
	}

	public boolean canSpawn(MobCategory mobCategory, ChunkPos chunkPos) {
		for (ServerPlayer serverPlayer : this.getPlayersNear(chunkPos)) {
			LocalMobCapCalculator.MobCounts mobCounts = (LocalMobCapCalculator.MobCounts)this.playerMobCounts.get(serverPlayer);
			if (mobCounts == null || mobCounts.canSpawn(mobCategory)) {
				return true;
			}
		}

		return false;
	}

	static class MobCounts {
		private final Object2IntMap<MobCategory> counts = new Object2IntOpenHashMap<>(MobCategory.values().length);

		public void add(MobCategory mobCategory) {
			this.counts.computeInt(mobCategory, (mobCategoryx, integer) -> integer == null ? 1 : integer + 1);
		}

		public boolean canSpawn(MobCategory mobCategory) {
			return this.counts.getOrDefault(mobCategory, 0) < mobCategory.getMaxInstancesPerChunk();
		}
	}
}
