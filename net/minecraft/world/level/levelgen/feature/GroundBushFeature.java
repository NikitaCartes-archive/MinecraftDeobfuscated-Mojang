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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GroundBushFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private final BlockState leaf;
    private final BlockState trunk;

    public GroundBushFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, BlockState blockState, BlockState blockState2) {
        super(function, false);
        this.trunk = blockState;
        this.leaf = blockState2;
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        if (GroundBushFeature.isGrassOrDirt(levelSimulatedRW, blockPos = levelSimulatedRW.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).below())) {
            blockPos = blockPos.above();
            this.setBlock(set, levelSimulatedRW, blockPos, this.trunk, boundingBox);
            for (int i = blockPos.getY(); i <= blockPos.getY() + 2; ++i) {
                int j = i - blockPos.getY();
                int k = 2 - j;
                for (int l = blockPos.getX() - k; l <= blockPos.getX() + k; ++l) {
                    int m = l - blockPos.getX();
                    for (int n = blockPos.getZ() - k; n <= blockPos.getZ() + k; ++n) {
                        BlockPos blockPos2;
                        int o = n - blockPos.getZ();
                        if (Math.abs(m) == k && Math.abs(o) == k && random.nextInt(2) == 0 || !GroundBushFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(l, i, n))) continue;
                        this.setBlock(set, levelSimulatedRW, blockPos2, this.leaf, boundingBox);
                    }
                }
            }
        }
        return true;
    }
}

