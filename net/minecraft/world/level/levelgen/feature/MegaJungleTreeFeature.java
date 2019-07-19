/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.MegaTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleTreeFeature
extends MegaTreeFeature<NoneFeatureConfiguration> {
    public MegaJungleTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, int i, int j, BlockState blockState, BlockState blockState2) {
        super(function, bl, i, j, blockState, blockState2);
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        int i = this.calcTreeHeigth(random);
        if (!this.prepareTree(levelSimulatedRW, blockPos, i)) {
            return false;
        }
        this.createCrown(levelSimulatedRW, blockPos.above(i), 2, boundingBox, set);
        for (int j = blockPos.getY() + i - 2 - random.nextInt(4); j > blockPos.getY() + i / 2; j -= 2 + random.nextInt(4)) {
            int m;
            float f = random.nextFloat() * ((float)Math.PI * 2);
            int k = blockPos.getX() + (int)(0.5f + Mth.cos(f) * 4.0f);
            int l = blockPos.getZ() + (int)(0.5f + Mth.sin(f) * 4.0f);
            for (m = 0; m < 5; ++m) {
                k = blockPos.getX() + (int)(1.5f + Mth.cos(f) * (float)m);
                l = blockPos.getZ() + (int)(1.5f + Mth.sin(f) * (float)m);
                this.setBlock(set, levelSimulatedRW, new BlockPos(k, j - 3 + m / 2, l), this.trunk, boundingBox);
            }
            m = 1 + random.nextInt(2);
            int n = j;
            for (int o = n - m; o <= n; ++o) {
                int p = o - n;
                this.placeSingleTrunkLeaves(levelSimulatedRW, new BlockPos(k, o, l), 1 - p, boundingBox, set);
            }
        }
        for (int q = 0; q < i; ++q) {
            BlockPos blockPos5;
            BlockPos blockPos4;
            BlockPos blockPos2 = blockPos.above(q);
            if (MegaJungleTreeFeature.isFree(levelSimulatedRW, blockPos2)) {
                this.setBlock(set, levelSimulatedRW, blockPos2, this.trunk, boundingBox);
                if (q > 0) {
                    this.placeVine(levelSimulatedRW, random, blockPos2.west(), VineBlock.EAST);
                    this.placeVine(levelSimulatedRW, random, blockPos2.north(), VineBlock.SOUTH);
                }
            }
            if (q >= i - 1) continue;
            BlockPos blockPos3 = blockPos2.east();
            if (MegaJungleTreeFeature.isFree(levelSimulatedRW, blockPos3)) {
                this.setBlock(set, levelSimulatedRW, blockPos3, this.trunk, boundingBox);
                if (q > 0) {
                    this.placeVine(levelSimulatedRW, random, blockPos3.east(), VineBlock.WEST);
                    this.placeVine(levelSimulatedRW, random, blockPos3.north(), VineBlock.SOUTH);
                }
            }
            if (MegaJungleTreeFeature.isFree(levelSimulatedRW, blockPos4 = blockPos2.south().east())) {
                this.setBlock(set, levelSimulatedRW, blockPos4, this.trunk, boundingBox);
                if (q > 0) {
                    this.placeVine(levelSimulatedRW, random, blockPos4.east(), VineBlock.WEST);
                    this.placeVine(levelSimulatedRW, random, blockPos4.south(), VineBlock.NORTH);
                }
            }
            if (!MegaJungleTreeFeature.isFree(levelSimulatedRW, blockPos5 = blockPos2.south())) continue;
            this.setBlock(set, levelSimulatedRW, blockPos5, this.trunk, boundingBox);
            if (q <= 0) continue;
            this.placeVine(levelSimulatedRW, random, blockPos5.west(), VineBlock.EAST);
            this.placeVine(levelSimulatedRW, random, blockPos5.south(), VineBlock.NORTH);
        }
        return true;
    }

    private void placeVine(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BooleanProperty booleanProperty) {
        if (random.nextInt(3) > 0 && MegaJungleTreeFeature.isAir(levelSimulatedRW, blockPos)) {
            this.setBlock(levelSimulatedRW, blockPos, (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true));
        }
    }

    private void createCrown(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, int i, BoundingBox boundingBox, Set<BlockPos> set) {
        int j = 2;
        for (int k = -2; k <= 0; ++k) {
            this.placeDoubleTrunkLeaves(levelSimulatedRW, blockPos.above(k), i + 1 - k, boundingBox, set);
        }
    }
}

