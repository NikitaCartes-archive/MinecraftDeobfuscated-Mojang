/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BirchFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState LOG = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.BIRCH_LEAVES.defaultBlockState();
    private final boolean superBirch;

    public BirchFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, boolean bl2) {
        super(function, bl);
        this.superBirch = bl2;
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        int m;
        int l;
        int k;
        int j;
        int i = random.nextInt(3) + 5;
        if (this.superBirch) {
            i += random.nextInt(7);
        }
        boolean bl = true;
        if (blockPos.getY() < 1 || blockPos.getY() + i + 1 > 256) {
            return false;
        }
        for (j = blockPos.getY(); j <= blockPos.getY() + 1 + i; ++j) {
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
                        if (BirchFeature.isFree(levelSimulatedRW, mutableBlockPos.set(l, j, m))) continue;
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
        if (!BirchFeature.isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below()) || blockPos.getY() >= 256 - i - 1) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos.below());
        for (j = blockPos.getY() - 3 + i; j <= blockPos.getY() + i; ++j) {
            k = j - (blockPos.getY() + i);
            int n = 1 - k / 2;
            for (l = blockPos.getX() - n; l <= blockPos.getX() + n; ++l) {
                m = l - blockPos.getX();
                for (int o = blockPos.getZ() - n; o <= blockPos.getZ() + n; ++o) {
                    BlockPos blockPos2;
                    int p = o - blockPos.getZ();
                    if (Math.abs(m) == n && Math.abs(p) == n && (random.nextInt(2) == 0 || k == 0) || !BirchFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(l, j, o))) continue;
                    this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
                }
            }
        }
        for (j = 0; j < i; ++j) {
            if (!BirchFeature.isAirOrLeaves(levelSimulatedRW, blockPos.above(j))) continue;
            this.setBlock(set, levelSimulatedRW, blockPos.above(j), LOG, boundingBox);
        }
        return true;
    }
}

