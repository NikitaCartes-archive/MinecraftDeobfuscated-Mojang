/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.area;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public final class LazyArea
implements Area {
    private final PixelTransformer transformer;
    private final Long2IntLinkedOpenHashMap cache;
    private final int maxCache;

    public LazyArea(Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap, int i, PixelTransformer pixelTransformer) {
        this.cache = long2IntLinkedOpenHashMap;
        this.maxCache = i;
        this.transformer = pixelTransformer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int get(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = this.cache;
        synchronized (long2IntLinkedOpenHashMap) {
            int k = this.cache.get(l);
            if (k != Integer.MIN_VALUE) {
                return k;
            }
            int m = this.transformer.apply(i, j);
            this.cache.put(l, m);
            if (this.cache.size() > this.maxCache) {
                for (int n = 0; n < this.maxCache / 16; ++n) {
                    this.cache.removeFirstInt();
                }
            }
            return m;
        }
    }

    public int getMaxCache() {
        return this.maxCache;
    }
}

