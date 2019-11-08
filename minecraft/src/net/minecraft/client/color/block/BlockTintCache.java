package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

@Environment(EnvType.CLIENT)
public class BlockTintCache {
	private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(() -> new BlockTintCache.LatestCacheInfo());
	private final Long2ObjectLinkedOpenHashMap<int[]> cache = new Long2ObjectLinkedOpenHashMap<>(256, 0.25F);
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public int getColor(BlockPos blockPos, IntSupplier intSupplier) {
		int i = blockPos.getX() >> 4;
		int j = blockPos.getZ() >> 4;
		BlockTintCache.LatestCacheInfo latestCacheInfo = (BlockTintCache.LatestCacheInfo)this.latestChunkOnThread.get();
		if (latestCacheInfo.x != i || latestCacheInfo.z != j) {
			latestCacheInfo.x = i;
			latestCacheInfo.z = j;
			latestCacheInfo.cache = this.findOrCreateChunkCache(i, j);
		}

		int k = blockPos.getX() & 15;
		int l = blockPos.getZ() & 15;
		int m = l << 4 | k;
		int n = latestCacheInfo.cache[m];
		if (n != -1) {
			return n;
		} else {
			int o = intSupplier.getAsInt();
			latestCacheInfo.cache[m] = o;
			return o;
		}
	}

	public void invalidateForChunk(int i, int j) {
		try {
			this.lock.writeLock().lock();

			for (int k = -1; k <= 1; k++) {
				for (int l = -1; l <= 1; l++) {
					long m = ChunkPos.asLong(i + k, j + l);
					this.cache.remove(m);
				}
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public void invalidateAll() {
		try {
			this.lock.writeLock().lock();
			this.cache.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	private int[] findOrCreateChunkCache(int i, int j) {
		long l = ChunkPos.asLong(i, j);
		this.lock.readLock().lock();

		int[] is;
		try {
			is = this.cache.get(l);
		} finally {
			this.lock.readLock().unlock();
		}

		if (is != null) {
			return is;
		} else {
			int[] js = new int[256];
			Arrays.fill(js, -1);

			try {
				this.lock.writeLock().lock();
				if (this.cache.size() >= 256) {
					this.cache.removeFirst();
				}

				this.cache.put(l, js);
			} finally {
				this.lock.writeLock().unlock();
			}

			return js;
		}
	}

	@Environment(EnvType.CLIENT)
	static class LatestCacheInfo {
		public int x = Integer.MIN_VALUE;
		public int z = Integer.MIN_VALUE;
		public int[] cache;

		private LatestCacheInfo() {
		}
	}
}
