package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

@Environment(EnvType.CLIENT)
public class BlockTintCache {
	private static final int MAX_CACHE_ENTRIES = 256;
	private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(BlockTintCache.LatestCacheInfo::new);
	private final Long2ObjectLinkedOpenHashMap<BlockTintCache.CacheData> cache = new Long2ObjectLinkedOpenHashMap<>(256, 0.25F);
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ToIntFunction<BlockPos> source;

	public BlockTintCache(ToIntFunction<BlockPos> toIntFunction) {
		this.source = toIntFunction;
	}

	public int getColor(BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX());
		int j = SectionPos.blockToSectionCoord(blockPos.getZ());
		BlockTintCache.LatestCacheInfo latestCacheInfo = (BlockTintCache.LatestCacheInfo)this.latestChunkOnThread.get();
		if (latestCacheInfo.x != i || latestCacheInfo.z != j || latestCacheInfo.cache == null || latestCacheInfo.cache.isInvalidated()) {
			latestCacheInfo.x = i;
			latestCacheInfo.z = j;
			latestCacheInfo.cache = this.findOrCreateChunkCache(i, j);
		}

		int[] is = latestCacheInfo.cache.getLayer(blockPos.getY());
		int k = blockPos.getX() & 15;
		int l = blockPos.getZ() & 15;
		int m = l << 4 | k;
		int n = is[m];
		if (n != -1) {
			return n;
		} else {
			int o = this.source.applyAsInt(blockPos);
			is[m] = o;
			return o;
		}
	}

	public void invalidateForChunk(int i, int j) {
		try {
			this.lock.writeLock().lock();

			for (int k = -1; k <= 1; k++) {
				for (int l = -1; l <= 1; l++) {
					long m = ChunkPos.asLong(i + k, j + l);
					BlockTintCache.CacheData cacheData = this.cache.remove(m);
					if (cacheData != null) {
						cacheData.invalidate();
					}
				}
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public void invalidateAll() {
		try {
			this.lock.writeLock().lock();
			this.cache.values().forEach(BlockTintCache.CacheData::invalidate);
			this.cache.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	private BlockTintCache.CacheData findOrCreateChunkCache(int i, int j) {
		long l = ChunkPos.asLong(i, j);
		this.lock.readLock().lock();

		try {
			BlockTintCache.CacheData cacheData = this.cache.get(l);
			if (cacheData != null) {
				return cacheData;
			}
		} finally {
			this.lock.readLock().unlock();
		}

		this.lock.writeLock().lock();

		BlockTintCache.CacheData cacheData2;
		try {
			BlockTintCache.CacheData cacheData = this.cache.get(l);
			if (cacheData == null) {
				cacheData2 = new BlockTintCache.CacheData();
				if (this.cache.size() >= 256) {
					this.cache.removeFirst();
				}

				this.cache.put(l, cacheData2);
				return cacheData2;
			}

			cacheData2 = cacheData;
		} finally {
			this.lock.writeLock().unlock();
		}

		return cacheData2;
	}

	@Environment(EnvType.CLIENT)
	static class CacheData {
		private final Int2ObjectArrayMap<int[]> cache = new Int2ObjectArrayMap<>(16);
		private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		private static final int BLOCKS_PER_LAYER = Mth.square(16);
		private volatile boolean invalidated;

		public int[] getLayer(int i) {
			this.lock.readLock().lock();

			try {
				int[] is = this.cache.get(i);
				if (is != null) {
					return is;
				}
			} finally {
				this.lock.readLock().unlock();
			}

			this.lock.writeLock().lock();

			int[] var12;
			try {
				var12 = this.cache.computeIfAbsent(i, ix -> this.allocateLayer());
			} finally {
				this.lock.writeLock().unlock();
			}

			return var12;
		}

		private int[] allocateLayer() {
			int[] is = new int[BLOCKS_PER_LAYER];
			Arrays.fill(is, -1);
			return is;
		}

		public boolean isInvalidated() {
			return this.invalidated;
		}

		public void invalidate() {
			this.invalidated = true;
		}
	}

	@Environment(EnvType.CLIENT)
	static class LatestCacheInfo {
		public int x = Integer.MIN_VALUE;
		public int z = Integer.MIN_VALUE;
		@Nullable
		BlockTintCache.CacheData cache;

		private LatestCacheInfo() {
		}
	}
}
