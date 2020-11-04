/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public final class BlockLightEngine
extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public BlockLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.BLOCK, new BlockLightSectionStorage(lightChunkGetter));
    }

    private int getLightEmission(long l) {
        int i = BlockPos.getX(l);
        int j = BlockPos.getY(l);
        int k = BlockPos.getZ(l);
        BlockGetter blockGetter = this.chunkSource.getChunkForLighting(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(k));
        if (blockGetter != null) {
            return blockGetter.getLightEmission(this.pos.set(i, j, k));
        }
        return 0;
    }

    @Override
    protected int computeLevelFromNeighbor(long l, long m, int i) {
        VoxelShape voxelShape2;
        int n;
        int k;
        if (m == Long.MAX_VALUE) {
            return 15;
        }
        if (l == Long.MAX_VALUE) {
            return i + 15 - this.getLightEmission(m);
        }
        if (i >= 15) {
            return i;
        }
        int j = Integer.signum(BlockPos.getX(m) - BlockPos.getX(l));
        Direction direction = Direction.fromNormal(j, k = Integer.signum(BlockPos.getY(m) - BlockPos.getY(l)), n = Integer.signum(BlockPos.getZ(m) - BlockPos.getZ(l)));
        if (direction == null) {
            return 15;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState blockState = this.getStateAndOpacity(m, mutableInt);
        if (mutableInt.getValue() >= 15) {
            return 15;
        }
        BlockState blockState2 = this.getStateAndOpacity(l, null);
        VoxelShape voxelShape = this.getShape(blockState2, l, direction);
        if (Shapes.faceShapeOccludes(voxelShape, voxelShape2 = this.getShape(blockState, m, direction.getOpposite()))) {
            return 15;
        }
        return i + Math.max(1, mutableInt.getValue());
    }

    @Override
    protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
        long m = SectionPos.blockToSection(l);
        for (Direction direction : DIRECTIONS) {
            long n = BlockPos.offset(l, direction);
            long o = SectionPos.blockToSection(n);
            if (m != o && !((BlockLightSectionStorage)this.storage).storingLightForSection(o)) continue;
            this.checkNeighbor(l, n, i, bl);
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
        DataLayer dataLayer = ((BlockLightSectionStorage)this.storage).getDataLayer(n, true);
        for (Direction direction : DIRECTIONS) {
            long p;
            DataLayer dataLayer2;
            long o = BlockPos.offset(l, direction);
            if (o == m || (dataLayer2 = n == (p = SectionPos.blockToSection(o)) ? dataLayer : ((BlockLightSectionStorage)this.storage).getDataLayer(p, true)) == null) continue;
            int q = this.computeLevelFromNeighbor(o, l, this.getLevel(dataLayer2, o));
            if (j > q) {
                j = q;
            }
            if (j != 0) continue;
            return j;
        }
        return j;
    }

    @Override
    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
        ((BlockLightSectionStorage)this.storage).runAllUpdates();
        this.checkEdge(Long.MAX_VALUE, blockPos.asLong(), 15 - i, true);
    }
}

