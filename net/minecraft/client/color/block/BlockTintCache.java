/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

@Environment(value=EnvType.CLIENT)
public class BlockTintCache {
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(LatestCacheInfo::new);
    private final Long2ObjectLinkedOpenHashMap<int[]> cache = new Long2ObjectLinkedOpenHashMap(256, 0.25f);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public int getColor(BlockPos blockPos, IntSupplier intSupplier) {
        int o;
        int i = SectionPos.blockToSectionCoord(blockPos.getX());
        int j = SectionPos.blockToSectionCoord(blockPos.getZ());
        LatestCacheInfo latestCacheInfo = this.latestChunkOnThread.get();
        if (latestCacheInfo.x != i || latestCacheInfo.z != j) {
            latestCacheInfo.x = i;
            latestCacheInfo.z = j;
            latestCacheInfo.cache = this.findOrCreateChunkCache(i, j);
        }
        int k = blockPos.getX() & 0xF;
        int l = blockPos.getZ() & 0xF;
        int m = l << 4 | k;
        int n = latestCacheInfo.cache[m];
        if (n != -1) {
            return n;
        }
        latestCacheInfo.cache[m] = o = intSupplier.getAsInt();
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
    private int[] findOrCreateChunkCache(int i, int j) {
        int[] is;
        long l = ChunkPos.asLong(i, j);
        this.lock.readLock().lock();
        try {
            is = this.cache.get(l);
        } finally {
            this.lock.readLock().unlock();
        }
        if (is != null) {
            return is;
        }
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

    @Environment(value=EnvType.CLIENT)
    static class LatestCacheInfo {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        public int[] cache;

        private LatestCacheInfo() {
        }
    }
}

