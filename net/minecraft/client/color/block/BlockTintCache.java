/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockTintCache {
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(LatestCacheInfo::new);
    private final Long2ObjectLinkedOpenHashMap<CacheData> cache = new Long2ObjectLinkedOpenHashMap(256, 0.25f);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ToIntFunction<BlockPos> source;

    public BlockTintCache(ToIntFunction<BlockPos> toIntFunction) {
        this.source = toIntFunction;
    }

    public int getColor(BlockPos blockPos) {
        int o;
        int i = SectionPos.blockToSectionCoord(blockPos.getX());
        int j = SectionPos.blockToSectionCoord(blockPos.getZ());
        LatestCacheInfo latestCacheInfo = this.latestChunkOnThread.get();
        if (latestCacheInfo.x != i || latestCacheInfo.z != j || latestCacheInfo.cache == null) {
            latestCacheInfo.x = i;
            latestCacheInfo.z = j;
            latestCacheInfo.cache = this.findOrCreateChunkCache(i, j);
        }
        int[] is = latestCacheInfo.cache.getLayer(blockPos.getY());
        int k = blockPos.getX() & 0xF;
        int l = blockPos.getZ() & 0xF;
        int m = l << 4 | k;
        int n = is[m];
        if (n != -1) {
            return n;
        }
        is[m] = o = this.source.applyAsInt(blockPos);
        return o;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void invalidateForChunk(int i, int j) {
        try {
            this.lock.writeLock().lock();
            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CacheData findOrCreateChunkCache(int i, int j) {
        CacheData cacheData;
        long l = ChunkPos.asLong(i, j);
        this.lock.readLock().lock();
        try {
            cacheData = this.cache.get(l);
            if (cacheData != null) {
                CacheData cacheData2 = cacheData;
                return cacheData2;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        this.lock.writeLock().lock();
        try {
            cacheData = this.cache.get(l);
            if (cacheData != null) {
                CacheData cacheData3 = cacheData;
                return cacheData3;
            }
            CacheData cacheData2 = new CacheData();
            if (this.cache.size() >= 256) {
                this.cache.removeFirst();
            }
            this.cache.put(l, cacheData2);
            CacheData cacheData4 = cacheData2;
            return cacheData4;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LatestCacheInfo {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        @Nullable
        CacheData cache;

        private LatestCacheInfo() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CacheData {
        private final Int2ObjectArrayMap<int[]> cache = new Int2ObjectArrayMap(16);
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private static final int BLOCKS_PER_LAYER = Mth.square(16);

        CacheData() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public int[] getLayer(int i2) {
            this.lock.readLock().lock();
            try {
                int[] is = this.cache.get(i2);
                if (is != null) {
                    int[] nArray = is;
                    return nArray;
                }
            } finally {
                this.lock.readLock().unlock();
            }
            this.lock.writeLock().lock();
            try {
                int[] nArray = this.cache.computeIfAbsent(i2, i -> this.allocateLayer());
                return nArray;
            } finally {
                this.lock.writeLock().unlock();
            }
        }

        private int[] allocateLayer() {
            int[] is = new int[BLOCKS_PER_LAYER];
            Arrays.fill(is, -1);
            return is;
        }
    }
}

