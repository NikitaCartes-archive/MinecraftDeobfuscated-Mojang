package net.minecraft.world.level;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.HashMap;
import java.util.List;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.entity.EntityAccess;

public class LocalMobCapTracker {
	final Long2ObjectMap<List<EntityAccess>> playersNearChunk = new Long2ObjectOpenHashMap<>();
	final HashMap<EntityAccess, LocalMobCapTracker.MobCounts> playerMobCounts = Maps.newHashMap();
	private final ChunkMap chunkMap;
	private final ServerLevel level;

	public LocalMobCapTracker(ChunkMap chunkMap, ServerLevel serverLevel) {
		this.chunkMap = chunkMap;
		this.level = serverLevel;
	}

	private List<EntityAccess> getPlayersNear(long l) {
		return this.playersNearChunk.computeIfAbsent(l, lx -> this.chunkMap.getPlayersInMobSpawningRadius(new ChunkPos(lx)));
	}

	public void addMob(long l, MobCategory mobCategory) {
		List<EntityAccess> list = this.getPlayersNear(l);
		float f = 1.0F / (float)mobCategory.getMaxInstancesPerChunk();

		for (EntityAccess entityAccess : list) {
			LocalMobCapTracker.MobCounts mobCounts = (LocalMobCapTracker.MobCounts)this.playerMobCounts
				.computeIfAbsent(entityAccess, entityAccessx -> new LocalMobCapTracker.MobCounts());
			mobCounts.add(mobCategory, f);
		}
	}

	public boolean canSpawn(MobCategory mobCategory, ChunkPos chunkPos) {
		return this.getPlayersNear(chunkPos.toLong()).stream().anyMatch(entityAccess -> this.canSpawn(entityAccess, mobCategory));
	}

	private boolean canSpawn(EntityAccess entityAccess, MobCategory mobCategory) {
		LocalMobCapTracker.MobCounts mobCounts = (LocalMobCapTracker.MobCounts)this.playerMobCounts.get(entityAccess);
		return mobCounts == null || mobCounts.canSpawn(mobCategory);
	}

	class MobCounts {
		private final Object2FloatMap<MobCategory> counts = new Object2FloatOpenHashMap<>(MobCategory.values().length);

		public void add(MobCategory mobCategory, float f) {
			float g = this.counts.getOrDefault(mobCategory, 0.0F);
			this.counts.put(mobCategory, g + f);
		}

		public boolean canSpawn(MobCategory mobCategory) {
			return this.counts.getOrDefault(mobCategory, 0.0F) < 1.0F;
		}
	}
}
