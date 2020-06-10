/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FancyTrunkPlacer
extends TrunkPlacer {
    public static final Codec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> FancyTrunkPlacer.trunkPlacerParts(instance).apply(instance, FancyTrunkPlacer::new));

    public FancyTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        int o;
        int j = 5;
        int k = i + 2;
        int l = Mth.floor((double)k * 0.618);
        if (!treeConfiguration.fromSapling) {
            FancyTrunkPlacer.setDirtAt(levelSimulatedRW, blockPos.below());
        }
        double d = 1.0;
        int m = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * (double)k / 13.0, 2.0)));
        int n = blockPos.getY() + l;
        ArrayList<FoliageCoords> list = Lists.newArrayList();
        list.add(new FoliageCoords(blockPos.above(o), n));
        for (o = k - 5; o >= 0; --o) {
            float f = this.treeShape(k, o);
            if (f < 0.0f) continue;
            for (int p = 0; p < m; ++p) {
                BlockPos blockPos3;
                double r;
                double h;
                double e = 1.0;
                double g = 1.0 * (double)f * ((double)random.nextFloat() + 0.328);
                double q = g * Math.sin(h = (double)(random.nextFloat() * 2.0f) * Math.PI) + 0.5;
                BlockPos blockPos2 = blockPos.offset(q, (double)(o - 1), r = g * Math.cos(h) + 0.5);
                if (!this.makeLimb(levelSimulatedRW, random, blockPos2, blockPos3 = blockPos2.above(5), false, set, boundingBox, treeConfiguration)) continue;
                int s = blockPos.getX() - blockPos2.getX();
                int t = blockPos.getZ() - blockPos2.getZ();
                double u = (double)blockPos2.getY() - Math.sqrt(s * s + t * t) * 0.381;
                int v = u > (double)n ? n : (int)u;
                BlockPos blockPos4 = new BlockPos(blockPos.getX(), v, blockPos.getZ());
                if (!this.makeLimb(levelSimulatedRW, random, blockPos4, blockPos2, false, set, boundingBox, treeConfiguration)) continue;
                list.add(new FoliageCoords(blockPos2, blockPos4.getY()));
            }
        }
        this.makeLimb(levelSimulatedRW, random, blockPos, blockPos.above(l), true, set, boundingBox, treeConfiguration);
        this.makeBranches(levelSimulatedRW, random, k, blockPos, list, set, boundingBox, treeConfiguration);
        ArrayList<FoliagePlacer.FoliageAttachment> list2 = Lists.newArrayList();
        for (FoliageCoords foliageCoords : list) {
            if (!this.trimBranches(k, foliageCoords.getBranchBase() - blockPos.getY())) continue;
            list2.add(foliageCoords.attachment);
        }
        return list2;
    }

    private boolean makeLimb(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BlockPos blockPos2, boolean bl, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        if (!bl && Objects.equals(blockPos, blockPos2)) {
            return true;
        }
        BlockPos blockPos3 = blockPos2.offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
        int i = this.getSteps(blockPos3);
        float f = (float)blockPos3.getX() / (float)i;
        float g = (float)blockPos3.getY() / (float)i;
        float h = (float)blockPos3.getZ() / (float)i;
        for (int j = 0; j <= i; ++j) {
            BlockPos blockPos4 = blockPos.offset(0.5f + (float)j * f, 0.5f + (float)j * g, 0.5f + (float)j * h);
            if (bl) {
                FancyTrunkPlacer.setBlock(levelSimulatedRW, blockPos4, (BlockState)treeConfiguration.trunkProvider.getState(random, blockPos4).setValue(RotatedPillarBlock.AXIS, this.getLogAxis(blockPos, blockPos4)), boundingBox);
                set.add(blockPos4.immutable());
                continue;
            }
            if (TreeFeature.isFree(levelSimulatedRW, blockPos4)) continue;
            return false;
        }
        return true;
    }

    private int getSteps(BlockPos blockPos) {
        int i = Mth.abs(blockPos.getX());
        int j = Mth.abs(blockPos.getY());
        int k = Mth.abs(blockPos.getZ());
        return Math.max(i, Math.max(j, k));
    }

    private Direction.Axis getLogAxis(BlockPos blockPos, BlockPos blockPos2) {
        int j;
        Direction.Axis axis = Direction.Axis.Y;
        int i = Math.abs(blockPos2.getX() - blockPos.getX());
        int k = Math.max(i, j = Math.abs(blockPos2.getZ() - blockPos.getZ()));
        if (k > 0) {
            axis = i == k ? Direction.Axis.X : Direction.Axis.Z;
        }
        return axis;
    }

    private boolean trimBranches(int i, int j) {
        return (double)j >= (double)i * 0.2;
    }

    private void makeBranches(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, List<FoliageCoords> list, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        for (FoliageCoords foliageCoords : list) {
            int j = foliageCoords.getBranchBase();
            BlockPos blockPos2 = new BlockPos(blockPos.getX(), j, blockPos.getZ());
            if (blockPos2.equals(foliageCoords.attachment.foliagePos()) || !this.trimBranches(i, j - blockPos.getY())) continue;
            this.makeLimb(levelSimulatedRW, random, blockPos2, foliageCoords.attachment.foliagePos(), true, set, boundingBox, treeConfiguration);
        }
    }

    private float treeShape(int i, int j) {
        if ((float)j < (float)i * 0.3f) {
            return -1.0f;
        }
        float f = (float)i / 2.0f;
        float g = f - (float)j;
        float h = Mth.sqrt(f * f - g * g);
        if (g == 0.0f) {
            h = f;
        } else if (Math.abs(g) >= f) {
            return 0.0f;
        }
        return h * 0.5f;
    }

    static class FoliageCoords {
        private final FoliagePlacer.FoliageAttachment attachment;
        private final int branchBase;

        public FoliageCoords(BlockPos blockPos, int i) {
            this.attachment = new FoliagePlacer.FoliageAttachment(blockPos, 0, false);
            this.branchBase = i;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}

