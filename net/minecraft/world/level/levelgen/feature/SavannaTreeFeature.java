/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SavannaTreeFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.ACACIA_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.ACACIA_LEAVES.defaultBlockState();

    public SavannaTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
        super(function, bl);
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        int q;
        int m;
        int l;
        int k;
        int i = random.nextInt(3) + random.nextInt(3) + 5;
        boolean bl = true;
        if (blockPos.getY() < 1 || blockPos.getY() + i + 1 > 256) {
            return false;
        }
        for (int j = blockPos.getY(); j <= blockPos.getY() + 1 + i; ++j) {
            k = 1;
            if (j == blockPos.getY()) {
                k = 0;
            }
            if (j >= blockPos.getY() + 1 + i - 2) {
                k = 2;
            }
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (l = blockPos.getX() - k; l <= blockPos.getX() + k && bl; ++l) {
                for (m = blockPos.getZ() - k; m <= blockPos.getZ() + k && bl; ++m) {
                    if (j >= 0 && j < 256) {
                        if (SavannaTreeFeature.isFree(levelSimulatedRW, mutableBlockPos.set(l, j, m))) continue;
                        bl = false;
                        continue;
                    }
                    bl = false;
                }
            }
        }
        if (!bl) {
            return false;
        }
        if (!SavannaTreeFeature.isGrassOrDirt(levelSimulatedRW, blockPos.below()) || blockPos.getY() >= 256 - i - 1) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos.below());
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        k = i - random.nextInt(4) - 1;
        int n = 3 - random.nextInt(3);
        l = blockPos.getX();
        m = blockPos.getZ();
        int o = 0;
        for (int p = 0; p < i; ++p) {
            BlockPos blockPos2;
            q = blockPos.getY() + p;
            if (p >= k && n > 0) {
                l += direction.getStepX();
                m += direction.getStepZ();
                --n;
            }
            if (!SavannaTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(l, q, m))) continue;
            this.placeLogAt(set, levelSimulatedRW, blockPos2, boundingBox);
            o = q;
        }
        BlockPos blockPos3 = new BlockPos(l, o, m);
        for (q = -3; q <= 3; ++q) {
            for (int r = -3; r <= 3; ++r) {
                if (Math.abs(q) == 3 && Math.abs(r) == 3) continue;
                this.placeLeafAt(set, levelSimulatedRW, blockPos3.offset(q, 0, r), boundingBox);
            }
        }
        blockPos3 = blockPos3.above();
        for (q = -1; q <= 1; ++q) {
            for (int r = -1; r <= 1; ++r) {
                this.placeLeafAt(set, levelSimulatedRW, blockPos3.offset(q, 0, r), boundingBox);
            }
        }
        this.placeLeafAt(set, levelSimulatedRW, blockPos3.east(2), boundingBox);
        this.placeLeafAt(set, levelSimulatedRW, blockPos3.west(2), boundingBox);
        this.placeLeafAt(set, levelSimulatedRW, blockPos3.south(2), boundingBox);
        this.placeLeafAt(set, levelSimulatedRW, blockPos3.north(2), boundingBox);
        l = blockPos.getX();
        m = blockPos.getZ();
        Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (direction2 != direction) {
            int t;
            q = k - random.nextInt(2) - 1;
            int r = 1 + random.nextInt(3);
            o = 0;
            for (int s = q; s < i && r > 0; ++s, --r) {
                if (s < 1) continue;
                t = blockPos.getY() + s;
                BlockPos blockPos4 = new BlockPos(l += direction2.getStepX(), t, m += direction2.getStepZ());
                if (!SavannaTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos4)) continue;
                this.placeLogAt(set, levelSimulatedRW, blockPos4, boundingBox);
                o = t;
            }
            if (o > 0) {
                BlockPos blockPos5 = new BlockPos(l, o, m);
                for (t = -2; t <= 2; ++t) {
                    for (int u = -2; u <= 2; ++u) {
                        if (Math.abs(t) == 2 && Math.abs(u) == 2) continue;
                        this.placeLeafAt(set, levelSimulatedRW, blockPos5.offset(t, 0, u), boundingBox);
                    }
                }
                blockPos5 = blockPos5.above();
                for (t = -1; t <= 1; ++t) {
                    for (int u = -1; u <= 1; ++u) {
                        this.placeLeafAt(set, levelSimulatedRW, blockPos5.offset(t, 0, u), boundingBox);
                    }
                }
            }
        }
        return true;
    }

    private void placeLogAt(Set<BlockPos> set, LevelWriter levelWriter, BlockPos blockPos, BoundingBox boundingBox) {
        this.setBlock(set, levelWriter, blockPos, TRUNK, boundingBox);
    }

    private void placeLeafAt(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BoundingBox boundingBox) {
        if (SavannaTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos)) {
            this.setBlock(set, levelSimulatedRW, blockPos, LEAF, boundingBox);
        }
    }
}

