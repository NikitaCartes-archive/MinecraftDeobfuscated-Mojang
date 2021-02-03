/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature
extends Feature<HugeMushroomFeatureConfiguration> {
    public AbstractHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    protected void placeTrunk(LevelAccessor levelAccessor, Random random, BlockPos blockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration, int i, BlockPos.MutableBlockPos mutableBlockPos) {
        for (int j = 0; j < i; ++j) {
            mutableBlockPos.set(blockPos).move(Direction.UP, j);
            if (levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) continue;
            this.setBlock(levelAccessor, mutableBlockPos, hugeMushroomFeatureConfiguration.stemProvider.getState(random, blockPos));
        }
    }

    protected int getTreeHeight(Random random) {
        int i = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            i *= 2;
        }
        return i;
    }

    protected boolean isValidPosition(LevelAccessor levelAccessor, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int j = blockPos.getY();
        if (j < levelAccessor.getMinBuildHeight() + 1 || j + i + 1 >= levelAccessor.getMaxBuildHeight()) {
            return false;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos.below());
        if (!AbstractHugeMushroomFeature.isDirt(blockState) && !blockState.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
        }
        for (int k = 0; k <= i; ++k) {
            int l = this.getTreeRadiusForHeight(-1, -1, hugeMushroomFeatureConfiguration.foliageRadius, k);
            for (int m = -l; m <= l; ++m) {
                for (int n = -l; n <= l; ++n) {
                    BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, m, k, n));
                    if (blockState2.isAir() || blockState2.is(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> featurePlaceContext) {
        BlockPos.MutableBlockPos mutableBlockPos;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        Random random = featurePlaceContext.random();
        HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration = featurePlaceContext.config();
        int i = this.getTreeHeight(random);
        if (!this.isValidPosition(worldGenLevel, blockPos, i, mutableBlockPos = new BlockPos.MutableBlockPos(), hugeMushroomFeatureConfiguration)) {
            return false;
        }
        this.makeCap(worldGenLevel, random, blockPos, i, mutableBlockPos, hugeMushroomFeatureConfiguration);
        this.placeTrunk(worldGenLevel, random, blockPos, hugeMushroomFeatureConfiguration, i, mutableBlockPos);
        return true;
    }

    protected abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected abstract void makeCap(LevelAccessor var1, Random var2, BlockPos var3, int var4, BlockPos.MutableBlockPos var5, HugeMushroomFeatureConfiguration var6);
}

