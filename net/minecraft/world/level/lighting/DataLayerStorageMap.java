/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.chunk.DataLayer;
import org.jetbrains.annotations.Nullable;

public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
    private static final int CACHE_SIZE = 2;
    private final long[] lastSectionKeys = new long[2];
    private final DataLayer[] lastSections = new DataLayer[2];
    private boolean cacheEnabled;
    protected final Long2ObjectOpenHashMap<DataLayer> map;

    protected DataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap) {
        this.map = long2ObjectOpenHashMap;
        this.clearCache();
        this.cacheEnabled = true;
    }

    public abstract M copy();

    public void copyDataLayer(long l) {
        this.map.put(l, this.map.get(l).copy());
        this.clearCache();
    }

    public boolean hasLayer(long l) {
        return this.map.containsKey(l);
    }

    @Nullable
    public DataLayer getLayer(long l) {
        DataLayer dataLayer;
        if (this.cacheEnabled) {
            for (int i = 0; i < 2; ++i) {
                if (l != this.lastSectionKeys[i]) continue;
                return this.lastSections[i];
            }
        }
        if ((dataLayer = this.map.get(l)) != null) {
            if (this.cacheEnabled) {
                for (int j = 1; j > 0; --j) {
                    this.lastSectionKeys[j] = this.lastSectionKeys[j - 1];
                    this.lastSections[j] = this.lastSections[j - 1];
                }
                this.lastSectionKeys[0] = l;
                this.lastSections[0] = dataLayer;
            }
            return dataLayer;
        }
        return null;
    }

    @Nullable
    public DataLayer removeLayer(long l) {
        return this.map.remove(l);
    }

    public void setLayer(long l, DataLayer dataLayer) {
        this.map.put(l, dataLayer);
    }

    public void clearCache() {
        for (int i = 0; i < 2; ++i) {
            this.lastSectionKeys[i] = Long.MAX_VALUE;
            this.lastSections[i] = null;
        }
    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}

