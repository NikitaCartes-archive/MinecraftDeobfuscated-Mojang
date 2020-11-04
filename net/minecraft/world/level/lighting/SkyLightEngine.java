/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.apache.commons.lang3.mutable.MutableInt;

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
        VoxelShape voxelShape2;
        int t;
        int s;
        if (m == Long.MAX_VALUE || l == Long.MAX_VALUE) {
            return 15;
        }
        if (i >= 15) {
            return i;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState blockState = this.getStateAndOpacity(m, mutableInt);
        if (mutableInt.getValue() >= 15) {
            return 15;
        }
        int j = BlockPos.getX(l);
        int k = BlockPos.getY(l);
        int n = BlockPos.getZ(l);
        int o = BlockPos.getX(m);
        int p = BlockPos.getY(m);
        int q = BlockPos.getZ(m);
        int r = Integer.signum(o - j);
        Direction direction = Direction.fromNormal(r, s = Integer.signum(p - k), t = Integer.signum(q - n));
        if (direction == null) {
            throw new IllegalStateException(String.format("Light was spread in illegal direction %d, %d, %d", r, s, t));
        }
        BlockState blockState2 = this.getStateAndOpacity(l, null);
        VoxelShape voxelShape = this.getShape(blockState2, l, direction);
        if (Shapes.faceShapeOccludes(voxelShape, voxelShape2 = this.getShape(blockState, m, direction.getOpposite()))) {
            return 15;
        }
        boolean bl = j == o && n == q;
        boolean bl3 = bl2 = bl && k > p;
        if (bl2 && i == 0 && mutableInt.getValue() == 0) {
            return 0;
        }
        return i + Math.max(1, mutableInt.getValue());
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
                long x = BlockPos.offset(l, 0, -u, 0);
                this.checkNeighbor(x, v, i, bl);
            } while (++u <= o * 16);
        }
    }

    @Override
    protected int getComputedLevel(long l, long m, int i) {
        int j = i;
        long n = SectionPos.blockToSection(l);
        DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayer(n, true);
        for (Direction direction : DIRECTIONS) {
            int k;
            long o = BlockPos.offset(l, direction);
            if (o == m) continue;
            long p = SectionPos.blockToSection(o);
            DataLayer dataLayer2 = n == p ? dataLayer : ((SkyLightSectionStorage)this.storage).getDataLayer(p, true);
            if (dataLayer2 != null) {
                k = this.getLevel(dataLayer2, o);
            } else {
                if (direction == Direction.DOWN) continue;
                k = 15 - ((SkyLightSectionStorage)this.storage).getLightValue(o, true);
            }
            int q = this.computeLevelFromNeighbor(o, l, k);
            if (j > q) {
                j = q;
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

    @Override
    @Environment(value=EnvType.CLIENT)
    public String getDebugData(long l) {
        return super.getDebugData(l) + (((SkyLightSectionStorage)this.storage).isAboveData(l) ? "*" : "");
    }
}

