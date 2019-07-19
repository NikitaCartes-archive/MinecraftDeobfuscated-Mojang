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

public class PineFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();

    public PineFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function, false);
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        int p;
        int o;
        int n;
        int m;
        int i = random.nextInt(5) + 7;
        int j = i - random.nextInt(2) - 3;
        int k = i - j;
        int l = 1 + random.nextInt(k + 1);
        if (blockPos.getY() < 1 || blockPos.getY() + i + 1 > 256) {
            return false;
        }
        boolean bl = true;
        for (m = blockPos.getY(); m <= blockPos.getY() + 1 + i && bl; ++m) {
            n = 1;
            n = m - blockPos.getY() < j ? 0 : l;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (o = blockPos.getX() - n; o <= blockPos.getX() + n && bl; ++o) {
                for (p = blockPos.getZ() - n; p <= blockPos.getZ() + n && bl; ++p) {
                    if (m >= 0 && m < 256) {
                        if (PineFeature.isFree(levelSimulatedRW, mutableBlockPos.set(o, m, p))) continue;
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
        if (!PineFeature.isGrassOrDirt(levelSimulatedRW, blockPos.below()) || blockPos.getY() >= 256 - i - 1) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos.below());
        m = 0;
        for (n = blockPos.getY() + i; n >= blockPos.getY() + j; --n) {
            for (int q = blockPos.getX() - m; q <= blockPos.getX() + m; ++q) {
                o = q - blockPos.getX();
                for (p = blockPos.getZ() - m; p <= blockPos.getZ() + m; ++p) {
                    BlockPos blockPos2;
                    int r = p - blockPos.getZ();
                    if (Math.abs(o) == m && Math.abs(r) == m && m > 0 || !PineFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(q, n, p))) continue;
                    this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
                }
            }
            if (m >= 1 && n == blockPos.getY() + j + 1) {
                --m;
                continue;
            }
            if (m >= l) continue;
            ++m;
        }
        for (n = 0; n < i - 1; ++n) {
            if (!PineFeature.isAirOrLeaves(levelSimulatedRW, blockPos.above(n))) continue;
            this.setBlock(set, levelSimulatedRW, blockPos.above(n), TRUNK, boundingBox);
        }
        return true;
    }
}

