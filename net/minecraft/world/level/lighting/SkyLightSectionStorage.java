/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.FlatDataLayer;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;

public class SkyLightSectionStorage
extends LayerLightSectionStorage<SkyDataLayerStorageMap> {
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private final LongSet sectionsWithSources = new LongOpenHashSet();
    private final LongSet sectionsToAddSourcesTo = new LongOpenHashSet();
    private final LongSet sectionsToRemoveSourcesFrom = new LongOpenHashSet();
    private final LongSet columnsWithSkySources = new LongOpenHashSet();
    private volatile boolean hasSourceInconsistencies;

    protected SkyLightSectionStorage(LightChunkGetter lightChunkGetter) {
        super(LightLayer.SKY, lightChunkGetter, new SkyDataLayerStorageMap(new Long2ObjectOpenHashMap<DataLayer>(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
    }

    @Override
    protected int getLightValue(long l) {
        return this.getLightValue(l, false);
    }

    protected int getLightValue(long l, boolean bl) {
        long m = SectionPos.blockToSection(l);
        int i = SectionPos.y(m);
        SkyDataLayerStorageMap skyDataLayerStorageMap = bl ? (SkyDataLayerStorageMap)this.updatingSectionData : (SkyDataLayerStorageMap)this.visibleSectionData;
        int j = skyDataLayerStorageMap.topSections.get(SectionPos.getZeroNode(m));
        if (j == skyDataLayerStorageMap.currentLowestY || i >= j) {
            if (bl && !this.lightOnInSection(m)) {
                return 0;
            }
            return 15;
        }
        DataLayer dataLayer = this.getDataLayer(skyDataLayerStorageMap, m);
        if (dataLayer == null) {
            l = BlockPos.getFlatIndex(l);
            while (dataLayer == null) {
                if (++i >= j) {
                    return 15;
                }
                l = BlockPos.offset(l, 0, 16, 0);
                m = SectionPos.offset(m, Direction.UP);
                dataLayer = this.getDataLayer(skyDataLayerStorageMap, m);
            }
        }
        return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    @Override
    protected void onNodeAdded(long l) {
        long m;
        int j;
        int i = SectionPos.y(l);
        if (((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY > i) {
            ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY = i;
            ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.defaultReturnValue(((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY);
        }
        if ((j = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(m = SectionPos.getZeroNode(l))) < i + 1) {
            ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.put(m, i + 1);
            if (this.columnsWithSkySources.contains(m)) {
                this.queueAddSource(l);
                if (j > ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY) {
                    long n = SectionPos.asLong(SectionPos.x(l), j - 1, SectionPos.z(l));
                    this.queueRemoveSource(n);
                }
                this.recheckInconsistencyFlag();
            }
        }
    }

    private void queueRemoveSource(long l) {
        this.sectionsToRemoveSourcesFrom.add(l);
        this.sectionsToAddSourcesTo.remove(l);
    }

    private void queueAddSource(long l) {
        this.sectionsToAddSourcesTo.add(l);
        this.sectionsToRemoveSourcesFrom.remove(l);
    }

    private void recheckInconsistencyFlag() {
        this.hasSourceInconsistencies = !this.sectionsToAddSourcesTo.isEmpty() || !this.sectionsToRemoveSourcesFrom.isEmpty();
    }

    @Override
    protected void onNodeRemoved(long l) {
        long m = SectionPos.getZeroNode(l);
        boolean bl = this.columnsWithSkySources.contains(m);
        if (bl) {
            this.queueRemoveSource(l);
        }
        int i = SectionPos.y(l);
        if (((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(m) == i + 1) {
            long n = l;
            while (!this.storingLightForSection(n) && this.hasSectionsBelow(i)) {
                --i;
                n = SectionPos.offset(n, Direction.DOWN);
            }
            if (this.storingLightForSection(n)) {
                ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.put(m, i + 1);
                if (bl) {
                    this.queueAddSource(n);
                }
            } else {
                ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.remove(m);
            }
        }
        if (bl) {
            this.recheckInconsistencyFlag();
        }
    }

    @Override
    protected void enableLightSources(long l, boolean bl) {
        this.runAllUpdates();
        if (bl && this.columnsWithSkySources.add(l)) {
            int i = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(l);
            if (i != ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY) {
                long m = SectionPos.asLong(SectionPos.x(l), i - 1, SectionPos.z(l));
                this.queueAddSource(m);
                this.recheckInconsistencyFlag();
            }
        } else if (!bl) {
            this.columnsWithSkySources.remove(l);
        }
    }

    @Override
    protected boolean hasInconsistencies() {
        return super.hasInconsistencies() || this.hasSourceInconsistencies;
    }

    @Override
    protected DataLayer createDataLayer(long l) {
        DataLayer dataLayer2;
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        long m = SectionPos.offset(l, Direction.UP);
        int i = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(SectionPos.getZeroNode(l));
        if (i == ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY || SectionPos.y(m) >= i) {
            return new DataLayer();
        }
        while ((dataLayer2 = this.getDataLayer(m, true)) == null) {
            m = SectionPos.offset(m, Direction.UP);
        }
        return new DataLayer(new FlatDataLayer(dataLayer2, 0).getData());
    }

    @Override
    protected void markNewInconsistencies(LayerLightEngine<SkyDataLayerStorageMap, ?> layerLightEngine, boolean bl, boolean bl2) {
        int j;
        int i;
        long l;
        LongIterator longIterator;
        super.markNewInconsistencies(layerLightEngine, bl, bl2);
        if (!bl) {
            return;
        }
        if (!this.sectionsToAddSourcesTo.isEmpty()) {
            longIterator = this.sectionsToAddSourcesTo.iterator();
            while (longIterator.hasNext()) {
                int k;
                l = (Long)longIterator.next();
                i = this.getLevel(l);
                if (i == 2 || this.sectionsToRemoveSourcesFrom.contains(l) || !this.sectionsWithSources.add(l)) continue;
                if (i == 1) {
                    long n;
                    this.clearQueuedSectionBlocks(layerLightEngine, l);
                    if (this.changedSections.add(l)) {
                        ((SkyDataLayerStorageMap)this.updatingSectionData).copyDataLayer(l);
                    }
                    Arrays.fill(this.getDataLayer(l, true).getData(), (byte)-1);
                    j = SectionPos.sectionToBlockCoord(SectionPos.x(l));
                    k = SectionPos.sectionToBlockCoord(SectionPos.y(l));
                    int m = SectionPos.sectionToBlockCoord(SectionPos.z(l));
                    for (Direction direction : HORIZONTALS) {
                        n = SectionPos.offset(l, direction);
                        if (!this.sectionsToRemoveSourcesFrom.contains(n) && (this.sectionsWithSources.contains(n) || this.sectionsToAddSourcesTo.contains(n)) || !this.storingLightForSection(n)) continue;
                        for (int o = 0; o < 16; ++o) {
                            for (int p = 0; p < 16; ++p) {
                                long q;
                                long r = switch (direction) {
                                    case Direction.NORTH -> {
                                        q = BlockPos.asLong(j + o, k + p, m);
                                        yield BlockPos.asLong(j + o, k + p, m - 1);
                                    }
                                    case Direction.SOUTH -> {
                                        q = BlockPos.asLong(j + o, k + p, m + 16 - 1);
                                        yield BlockPos.asLong(j + o, k + p, m + 16);
                                    }
                                    case Direction.WEST -> {
                                        q = BlockPos.asLong(j, k + o, m + p);
                                        yield BlockPos.asLong(j - 1, k + o, m + p);
                                    }
                                    default -> {
                                        q = BlockPos.asLong(j + 16 - 1, k + o, m + p);
                                        yield BlockPos.asLong(j + 16, k + o, m + p);
                                    }
                                };
                                layerLightEngine.checkEdge(q, r, layerLightEngine.computeLevelFromNeighbor(q, r, 0), true);
                            }
                        }
                    }
                    for (int s = 0; s < 16; ++s) {
                        for (int t = 0; t < 16; ++t) {
                            long u = BlockPos.asLong(SectionPos.sectionToBlockCoord(SectionPos.x(l), s), SectionPos.sectionToBlockCoord(SectionPos.y(l)), SectionPos.sectionToBlockCoord(SectionPos.z(l), t));
                            n = BlockPos.asLong(SectionPos.sectionToBlockCoord(SectionPos.x(l), s), SectionPos.sectionToBlockCoord(SectionPos.y(l)) - 1, SectionPos.sectionToBlockCoord(SectionPos.z(l), t));
                            layerLightEngine.checkEdge(u, n, layerLightEngine.computeLevelFromNeighbor(u, n, 0), true);
                        }
                    }
                    continue;
                }
                for (j = 0; j < 16; ++j) {
                    for (k = 0; k < 16; ++k) {
                        long v = BlockPos.asLong(SectionPos.sectionToBlockCoord(SectionPos.x(l), j), SectionPos.sectionToBlockCoord(SectionPos.y(l), 15), SectionPos.sectionToBlockCoord(SectionPos.z(l), k));
                        layerLightEngine.checkEdge(Long.MAX_VALUE, v, 0, true);
                    }
                }
            }
        }
        this.sectionsToAddSourcesTo.clear();
        if (!this.sectionsToRemoveSourcesFrom.isEmpty()) {
            longIterator = this.sectionsToRemoveSourcesFrom.iterator();
            while (longIterator.hasNext()) {
                l = (Long)longIterator.next();
                if (!this.sectionsWithSources.remove(l) || !this.storingLightForSection(l)) continue;
                for (i = 0; i < 16; ++i) {
                    for (j = 0; j < 16; ++j) {
                        long w = BlockPos.asLong(SectionPos.sectionToBlockCoord(SectionPos.x(l), i), SectionPos.sectionToBlockCoord(SectionPos.y(l), 15), SectionPos.sectionToBlockCoord(SectionPos.z(l), j));
                        layerLightEngine.checkEdge(Long.MAX_VALUE, w, 15, false);
                    }
                }
            }
        }
        this.sectionsToRemoveSourcesFrom.clear();
        this.hasSourceInconsistencies = false;
    }

    protected boolean hasSectionsBelow(int i) {
        return i >= ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY;
    }

    protected boolean isAboveData(long l) {
        long m = SectionPos.getZeroNode(l);
        int i = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(m);
        return i == ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY || SectionPos.y(l) >= i;
    }

    protected boolean lightOnInSection(long l) {
        long m = SectionPos.getZeroNode(l);
        return this.columnsWithSkySources.contains(m);
    }

    protected static final class SkyDataLayerStorageMap
    extends DataLayerStorageMap<SkyDataLayerStorageMap> {
        int currentLowestY;
        final Long2IntOpenHashMap topSections;

        public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap, Long2IntOpenHashMap long2IntOpenHashMap, int i) {
            super(long2ObjectOpenHashMap);
            this.topSections = long2IntOpenHashMap;
            long2IntOpenHashMap.defaultReturnValue(i);
            this.currentLowestY = i;
        }

        @Override
        public SkyDataLayerStorageMap copy() {
            return new SkyDataLayerStorageMap((Long2ObjectOpenHashMap<DataLayer>)this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }

        @Override
        public /* synthetic */ DataLayerStorageMap copy() {
            return this.copy();
        }
    }
}

