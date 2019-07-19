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

public class SpruceFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();

    public SpruceFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
        super(function, bl);
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        int p;
        int o;
        int n;
        int m;
        int i = random.nextInt(4) + 6;
        int j = 1 + random.nextInt(2);
        int k = i - j;
        int l = 2 + random.nextInt(2);
        boolean bl = true;
        if (blockPos.getY() < 1 || blockPos.getY() + i + 1 > 256) {
            return false;
        }
        for (m = blockPos.getY(); m <= blockPos.getY() + 1 + i && bl; ++m) {
            n = m - blockPos.getY() < j ? 0 : l;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (o = blockPos.getX() - n; o <= blockPos.getX() + n && bl; ++o) {
                for (p = blockPos.getZ() - n; p <= blockPos.getZ() + n && bl; ++p) {
                    if (m >= 0 && m < 256) {
                        mutableBlockPos.set(o, m, p);
                        if (SpruceFeature.isAirOrLeaves(levelSimulatedRW, mutableBlockPos)) continue;
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
        if (!SpruceFeature.isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below()) || blockPos.getY() >= 256 - i - 1) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos.below());
        m = random.nextInt(2);
        n = 1;
        int q = 0;
        for (o = 0; o <= k; ++o) {
            p = blockPos.getY() + i - o;
            for (int r = blockPos.getX() - m; r <= blockPos.getX() + m; ++r) {
                int s = r - blockPos.getX();
                for (int t = blockPos.getZ() - m; t <= blockPos.getZ() + m; ++t) {
                    BlockPos blockPos2;
                    int u = t - blockPos.getZ();
                    if (Math.abs(s) == m && Math.abs(u) == m && m > 0 || !SpruceFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(r, p, t)) && !SpruceFeature.isReplaceablePlant(levelSimulatedRW, blockPos2)) continue;
                    this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
                }
            }
            if (m >= n) {
                m = q;
                q = 1;
                if (++n <= l) continue;
                n = l;
                continue;
            }
            ++m;
        }
        o = random.nextInt(3);
        for (p = 0; p < i - o; ++p) {
            if (!SpruceFeature.isAirOrLeaves(levelSimulatedRW, blockPos.above(p))) continue;
            this.setBlock(set, levelSimulatedRW, blockPos.above(p), TRUNK, boundingBox);
        }
        return true;
    }
}

