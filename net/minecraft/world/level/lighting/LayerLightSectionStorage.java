/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightEngine;
import org.jetbrains.annotations.Nullable;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>>
extends SectionTracker {
    protected static final int LIGHT_AND_DATA = 0;
    protected static final int LIGHT_ONLY = 1;
    protected static final int EMPTY = 2;
    protected static final DataLayer EMPTY_DATA = new DataLayer();
    private static final Direction[] DIRECTIONS = Direction.values();
    private final LightLayer layer;
    private final LightChunkGetter chunkSource;
    protected final LongSet dataSectionSet = new LongOpenHashSet();
    protected final LongSet toMarkNoData = new LongOpenHashSet();
    protected final LongSet toMarkData = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
    private final LongSet untrustedSections = new LongOpenHashSet();
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasToRemove;

    protected LayerLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, M dataLayerStorageMap) {
        super(3, 16, 256);
        this.layer = lightLayer;
        this.chunkSource = lightChunkGetter;
        this.updatingSectionData = dataLayerStorageMap;
        this.visibleSectionData = ((DataLayerStorageMap)dataLayerStorageMap).copy();
        ((DataLayerStorageMap)this.visibleSectionData).disableCache();
    }

    protected boolean storingLightForSection(long l) {
        return this.getDataLayer(l, true) != null;
    }

    @Nullable
    protected DataLayer getDataLayer(long l, boolean bl) {
        return this.getDataLayer(bl ? this.updatingSectionData : this.visibleSectionData, l);
    }

    @Nullable
    protected DataLayer getDataLayer(M dataLayerStorageMap, long l) {
        return ((DataLayerStorageMap)dataLayerStorageMap).getLayer(l);
    }

    @Nullable
    public DataLayer getDataLayerData(long l) {
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        return this.getDataLayer(l, false);
    }

    protected abstract int getLightValue(long var1);

    protected int getStoredLevel(long l) {
        long m = SectionPos.blockToSection(l);
        DataLayer dataLayer = this.getDataLayer(m, true);
        return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    protected void setStoredLevel(long l, int i) {
        long m = SectionPos.blockToSection(l);
        if (this.changedSections.add(m)) {
            ((DataLayerStorageMap)this.updatingSectionData).copyDataLayer(m);
        }
        DataLayer dataLayer = this.getDataLayer(m, true);
        dataLayer.set(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)), i);
        SectionPos.aroundAndAtBlockPos(l, this.sectionsAffectedByLightUpdates::add);
    }

    @Override
    protected int getLevel(long l) {
        if (l == Long.MAX_VALUE) {
            return 2;
        }
        if (this.dataSectionSet.contains(l)) {
            return 0;
        }
        if (!this.toRemove.contains(l) && ((DataLayerStorageMap)this.updatingSectionData).hasLayer(l)) {
            return 1;
        }
        return 2;
    }

    @Override
    protected int getLevelFromSource(long l) {
        if (this.toMarkNoData.contains(l)) {
            return 2;
        }
        if (this.dataSectionSet.contains(l) || this.toMarkData.contains(l)) {
            return 0;
        }
        return 2;
    }

    @Override
    protected void setLevel(long l, int i) {
        int j = this.getLevel(l);
        if (j != 0 && i == 0) {
            this.dataSectionSet.add(l);
            this.toMarkData.remove(l);
        }
        if (j == 0 && i != 0) {
            this.dataSectionSet.remove(l);
            this.toMarkNoData.remove(l);
        }
        if (j >= 2 && i != 2) {
            if (this.toRemove.contains(l)) {
                this.toRemove.remove(l);
            } else {
                ((DataLayerStorageMap)this.updatingSectionData).setLayer(l, this.createDataLayer(l));
                this.changedSections.add(l);
                this.onNodeAdded(l);
                int k = SectionPos.x(l);
                int m = SectionPos.y(l);
                int n = SectionPos.z(l);
                for (int o = -1; o <= 1; ++o) {
                    for (int p = -1; p <= 1; ++p) {
                        for (int q = -1; q <= 1; ++q) {
                            this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(k + p, m + q, n + o));
                        }
                    }
                }
            }
        }
        if (j != 2 && i >= 2) {
            this.toRemove.add(l);
        }
        this.hasToRemove = !this.toRemove.isEmpty();
    }

    protected DataLayer createDataLayer(long l) {
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        return new DataLayer();
    }

    protected void clearQueuedSectionBlocks(LayerLightEngine<?, ?> layerLightEngine, long l) {
        if (layerLightEngine.getQueueSize() == 0) {
            return;
        }
        if (layerLightEngine.getQueueSize() < 8192) {
            layerLightEngine.removeIf(m -> SectionPos.blockToSection(m) == l);
            return;
        }
        int i = SectionPos.sectionToBlockCoord(SectionPos.x(l));
        int j = SectionPos.sectionToBlockCoord(SectionPos.y(l));
        int k = SectionPos.sectionToBlockCoord(SectionPos.z(l));
        for (int m2 = 0; m2 < 16; ++m2) {
            for (int n = 0; n < 16; ++n) {
                for (int o = 0; o < 16; ++o) {
                    long p = BlockPos.asLong(i + m2, j + n, k + o);
                    layerLightEngine.removeFromQueue(p);
                }
            }
        }
    }

    protected boolean hasInconsistencies() {
        return this.hasToRemove;
    }

    protected void markNewInconsistencies(LayerLightEngine<M, ?> layerLightEngine, boolean bl, boolean bl2) {
        long m;
        DataLayer dataLayer2;
        long l;
        if (!this.hasInconsistencies() && this.queuedSections.isEmpty()) {
            return;
        }
        Iterator<Long> iterator = this.toRemove.iterator();
        while (iterator.hasNext()) {
            l = (Long)iterator.next();
            this.clearQueuedSectionBlocks(layerLightEngine, l);
            DataLayer dataLayer = (DataLayer)this.queuedSections.remove(l);
            dataLayer2 = ((DataLayerStorageMap)this.updatingSectionData).removeLayer(l);
            if (!this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(l))) continue;
            if (dataLayer != null) {
                this.queuedSections.put(l, dataLayer);
                continue;
            }
            if (dataLayer2 == null) continue;
            this.queuedSections.put(l, dataLayer2);
        }
        ((DataLayerStorageMap)this.updatingSectionData).clearCache();
        iterator = this.toRemove.iterator();
        while (iterator.hasNext()) {
            l = (Long)iterator.next();
            this.onNodeRemoved(l);
        }
        this.toRemove.clear();
        this.hasToRemove = false;
        for (Long2ObjectMap.Entry entry : this.queuedSections.long2ObjectEntrySet()) {
            m = entry.getLongKey();
            if (!this.storingLightForSection(m)) continue;
            dataLayer2 = (DataLayer)entry.getValue();
            if (((DataLayerStorageMap)this.updatingSectionData).getLayer(m) == dataLayer2) continue;
            this.clearQueuedSectionBlocks(layerLightEngine, m);
            ((DataLayerStorageMap)this.updatingSectionData).setLayer(m, dataLayer2);
            this.changedSections.add(m);
        }
        ((DataLayerStorageMap)this.updatingSectionData).clearCache();
        if (!bl2) {
            for (long l2 : this.queuedSections.keySet()) {
                this.checkEdgesForSection(layerLightEngine, l2);
            }
        } else {
            for (long l3 : this.untrustedSections) {
                this.checkEdgesForSection(layerLightEngine, l3);
            }
        }
        this.untrustedSections.clear();
        Iterator objectIterator = this.queuedSections.long2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            m = entry.getLongKey();
            if (!this.storingLightForSection(m)) continue;
            objectIterator.remove();
        }
    }

    private void checkEdgesForSection(LayerLightEngine<M, ?> layerLightEngine, long l) {
        if (!this.storingLightForSection(l)) {
            return;
        }
        int i = SectionPos.sectionToBlockCoord(SectionPos.x(l));
        int j = SectionPos.sectionToBlockCoord(SectionPos.y(l));
        int k = SectionPos.sectionToBlockCoord(SectionPos.z(l));
        for (Direction direction : DIRECTIONS) {
            long m = SectionPos.offset(l, direction);
            if (this.queuedSections.containsKey(m) || !this.storingLightForSection(m)) continue;
            for (int n = 0; n < 16; ++n) {
                for (int o = 0; o < 16; ++o) {
                    long p;
                    long q = switch (direction) {
                        case Direction.DOWN -> {
                            p = BlockPos.asLong(i + o, j, k + n);
                            yield BlockPos.asLong(i + o, j - 1, k + n);
                        }
                        case Direction.UP -> {
                            p = BlockPos.asLong(i + o, j + 16 - 1, k + n);
                            yield BlockPos.asLong(i + o, j + 16, k + n);
                        }
                        case Direction.NORTH -> {
                            p = BlockPos.asLong(i + n, j + o, k);
                            yield BlockPos.asLong(i + n, j + o, k - 1);
                        }
                        case Direction.SOUTH -> {
                            p = BlockPos.asLong(i + n, j + o, k + 16 - 1);
                            yield BlockPos.asLong(i + n, j + o, k + 16);
                        }
                        case Direction.WEST -> {
                            p = BlockPos.asLong(i, j + n, k + o);
                            yield BlockPos.asLong(i - 1, j + n, k + o);
                        }
                        default -> {
                            p = BlockPos.asLong(i + 16 - 1, j + n, k + o);
                            yield BlockPos.asLong(i + 16, j + n, k + o);
                        }
                    };
                    layerLightEngine.checkEdge(p, q, layerLightEngine.computeLevelFromNeighbor(p, q, layerLightEngine.getLevel(p)), false);
                    layerLightEngine.checkEdge(q, p, layerLightEngine.computeLevelFromNeighbor(q, p, layerLightEngine.getLevel(q)), false);
                }
            }
        }
    }

    protected void onNodeAdded(long l) {
    }

    protected void onNodeRemoved(long l) {
    }

    protected void enableLightSources(long l, boolean bl) {
    }

    public void retainData(long l, boolean bl) {
        if (bl) {
            this.columnsToRetainQueuedDataFor.add(l);
        } else {
            this.columnsToRetainQueuedDataFor.remove(l);
        }
    }

    protected void queueSectionData(long l, @Nullable DataLayer dataLayer, boolean bl) {
        if (dataLayer != null) {
            this.queuedSections.put(l, dataLayer);
            if (!bl) {
                this.untrustedSections.add(l);
            }
        } else {
            this.queuedSections.remove(l);
        }
    }

    protected void updateSectionStatus(long l, boolean bl) {
        boolean bl2 = this.dataSectionSet.contains(l);
        if (!bl2 && !bl) {
            this.toMarkData.add(l);
            this.checkEdge(Long.MAX_VALUE, l, 0, true);
        }
        if (bl2 && bl) {
            this.toMarkNoData.add(l);
            this.checkEdge(Long.MAX_VALUE, l, 2, false);
        }
    }

    protected void runAllUpdates() {
        if (this.hasWork()) {
            this.runUpdates(Integer.MAX_VALUE);
        }
    }

    protected void swapSectionMap() {
        if (!this.changedSections.isEmpty()) {
            Object dataLayerStorageMap = ((DataLayerStorageMap)this.updatingSectionData).copy();
            ((DataLayerStorageMap)dataLayerStorageMap).disableCache();
            this.visibleSectionData = dataLayerStorageMap;
            this.changedSections.clear();
        }
        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            LongIterator longIterator = this.sectionsAffectedByLightUpdates.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(l));
            }
            this.sectionsAffectedByLightUpdates.clear();
        }
    }
}

