/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeBrownMushroomFeature
extends AbstractHugeMushroomFeature {
    public HugeBrownMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfiguration> function) {
        super(function);
    }

    @Override
    protected void makeCap(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int j = hugeMushroomFeatureConfiguration.foliageRadius;
        for (int k = -j; k <= j; ++k) {
            for (int l = -j; l <= j; ++l) {
                boolean bl6;
                boolean bl = k == -j;
                boolean bl2 = k == j;
                boolean bl3 = l == -j;
                boolean bl4 = l == j;
                boolean bl5 = bl || bl2;
                boolean bl7 = bl6 = bl3 || bl4;
                if (bl5 && bl6) continue;
                mutableBlockPos.setWithOffset(blockPos, k, i, l);
                if (levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) continue;
                boolean bl72 = bl || bl6 && k == 1 - j;
                boolean bl8 = bl2 || bl6 && k == j - 1;
                boolean bl9 = bl3 || bl5 && l == 1 - j;
                boolean bl10 = bl4 || bl5 && l == j - 1;
                this.setBlock(levelAccessor, mutableBlockPos, (BlockState)((BlockState)((BlockState)((BlockState)hugeMushroomFeatureConfiguration.capProvider.getState(random, blockPos).setValue(HugeMushroomBlock.WEST, bl72)).setValue(HugeMushroomBlock.EAST, bl8)).setValue(HugeMushroomBlock.NORTH, bl9)).setValue(HugeMushroomBlock.SOUTH, bl10));
            }
        }
    }

    @Override
    protected int getTreeRadiusForHeight(int i, int j, int k, int l) {
        return l <= 3 ? 0 : k;
    }
}

