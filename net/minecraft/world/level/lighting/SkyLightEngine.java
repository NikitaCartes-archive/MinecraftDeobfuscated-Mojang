/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SkyLightEngine
extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public SkyLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.SKY, new SkyLightSectionStorage(lightChunkGetter));
    }

    @Override
    protected int computeLevelFromNeighbor(long l, long m, int i) {
        boolean bl2;
        VoxelShape voxelShape;
        if (m == Long.MAX_VALUE) {
            return 15;
        }
        if (l == Long.MAX_VALUE) {
            if (((SkyLightSectionStorage)this.storage).hasLightSource(m)) {
                i = 0;
            } else {
                return 15;
            }
        }
        if (i >= 15) {
            return i;
        }
        AtomicInteger atomicInteger = new AtomicInteger();
        BlockState blockState = this.getStateAndOpacity(m, atomicInteger);
        if (atomicInteger.get() >= 15) {
            return 15;
        }
        int j = BlockPos.getX(l);
        int k = BlockPos.getY(l);
        int n = BlockPos.getZ(l);
        int o = BlockPos.getX(m);
        int p = BlockPos.getY(m);
        int q = BlockPos.getZ(m);
        boolean bl = j == o && n == q;
        int r = Integer.signum(o - j);
        int s = Integer.signum(p - k);
        int t = Integer.signum(q - n);
        Direction direction = l == Long.MAX_VALUE ? Direction.DOWN : Direction.fromNormal(r, s, t);
        BlockState blockState2 = this.getStateAndOpacity(l, null);
        if (direction != null) {
            VoxelShape voxelShape2;
            voxelShape = this.getShape(blockState2, l, direction);
            if (Shapes.faceShapeOccludes(voxelShape, voxelShape2 = this.getShape(blockState, m, direction.getOpposite()))) {
                return 15;
            }
        } else {
            voxelShape = this.getShape(blockState2, l, Direction.DOWN);
            if (Shapes.faceShapeOccludes(voxelShape, Shapes.empty())) {
                return 15;
            }
            int u = bl ? -1 : 0;
            Direction direction2 = Direction.fromNormal(r, u, t);
            if (direction2 == null) {
                return 15;
            }
            VoxelShape voxelShape3 = this.getShape(blockState, m, direction2.getOpposite());
            if (Shapes.faceShapeOccludes(Shapes.empty(), voxelShape3)) {
                return 15;
            }
        }
        boolean bl3 = bl2 = l == Long.MAX_VALUE || bl && k > p;
        if (bl2 && i == 0 && atomicInteger.get() == 0) {
            return 0;
        }
        return i + Math.max(1, atomicInteger.get());
    }

    @Override
    protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
        long s;
        long t;
        int o;
        long m = SectionPos.blockToSection(l);
        int j = BlockPos.getY(l);
        int k = SectionPos.sectionRelative(j);
        int n = SectionPos.blockToSectionCoord(j);
        if (k != 0) {
            o = 0;
        } else {
            int p = 0;
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.offset(m, 0, -p - 1, 0)) && ((SkyLightSectionStorage)this.storage).hasSectionsBelow(n - p - 1)) {
                ++p;
            }
            o = p;
        }
        long q = BlockPos.offset(l, 0, -1 - o * 16, 0);
        long r = SectionPos.blockToSection(q);
        if (m == r || ((SkyLightSectionStorage)this.storage).storingLightForSection(r)) {
            this.checkNeighbor(l, q, i, bl);
        }
        if (m == (t = SectionPos.blockToSection(s = BlockPos.offset(l, Direction.UP))) || ((SkyLightSectionStorage)this.storage).storingLightForSection(t)) {
            this.checkNeighbor(l, s, i, bl);
        }
        block1: for (Direction direction : HORIZONTALS) {
            int u = 0;
            do {
                long v;
                long w;
                if (m == (w = SectionPos.blockToSection(v = BlockPos.offset(l, direction.getStepX(), -u, direction.getStepZ())))) {
                    this.checkNeighbor(l, v, i, bl);
                    continue block1;
                }
                if (!((SkyLightSectionStorage)this.storage).storingLightForSection(w)) continue;
                this.checkNeighbor(l, v, i, bl);
            } while (++u <= o * 16);
        }
    }

    @Override
    protected int getComputedLevel(long l, long m, int i) {
        int j = i;
        if (Long.MAX_VALUE != m) {
            int k = this.computeLevelFromNeighbor(Long.MAX_VALUE, l, 0);
            if (j > k) {
                j = k;
            }
            if (j == 0) {
                return j;
            }
        }
        long n = SectionPos.blockToSection(l);
        DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayer(n, true);
        for (Direction direction : DIRECTIONS) {
            int r;
            long o = BlockPos.offset(l, direction);
            long p = SectionPos.blockToSection(o);
            DataLayer dataLayer2 = n == p ? dataLayer : ((SkyLightSectionStorage)this.storage).getDataLayer(p, true);
            if (dataLayer2 != null) {
                if (o == m) continue;
                int q = this.computeLevelFromNeighbor(o, l, this.getLevel(dataLayer2, o));
                if (j > q) {
                    j = q;
                }
                if (j != 0) continue;
                return j;
            }
            if (direction == Direction.DOWN) continue;
            o = BlockPos.getFlatIndex(o);
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(p) && !((SkyLightSectionStorage)this.storage).isAboveData(p)) {
                p = SectionPos.offset(p, Direction.UP);
                o = BlockPos.offset(o, 0, 16, 0);
            }
            DataLayer dataLayer3 = ((SkyLightSectionStorage)this.storage).getDataLayer(p, true);
            if (o == m) continue;
            if (dataLayer3 != null) {
                r = this.computeLevelFromNeighbor(o, l, this.getLevel(dataLayer3, o));
            } else {
                int n2 = r = ((SkyLightSectionStorage)this.storage).lightOnInSection(p) ? 0 : 15;
            }
            if (j > r) {
                j = r;
            }
            if (j != 0) continue;
            return j;
        }
        return j;
    }

    @Override
    protected void checkNode(long l) {
        ((SkyLightSectionStorage)this.storage).runAllUpdates();
        long m = SectionPos.blockToSection(l);
        if (((SkyLightSectionStorage)this.storage).storingLightForSection(m)) {
            super.checkNode(l);
        } else {
            l = BlockPos.getFlatIndex(l);
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(m) && !((SkyLightSectionStorage)this.storage).isAboveData(m)) {
                m = SectionPos.offset(m, Direction.UP);
                l = BlockPos.offset(l, 0, 16, 0);
            }
            if (((SkyLightSectionStorage)this.storage).storingLightForSection(m)) {
                super.checkNode(l);
            }
        }
    }
}

