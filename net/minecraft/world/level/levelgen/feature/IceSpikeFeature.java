/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature
extends Feature<NoneFeatureConfiguration> {
    public IceSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        int l;
        int k;
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
            blockPos = blockPos.below();
        }
        if (!worldGenLevel.getBlockState(blockPos).is(Blocks.SNOW_BLOCK)) {
            return false;
        }
        blockPos = blockPos.above(randomSource.nextInt(4));
        int i = randomSource.nextInt(4) + 7;
        int j = i / 4 + randomSource.nextInt(2);
        if (j > 1 && randomSource.nextInt(60) == 0) {
            blockPos = blockPos.above(10 + randomSource.nextInt(30));
        }
        for (k = 0; k < i; ++k) {
            float f = (1.0f - (float)k / (float)i) * (float)j;
            l = Mth.ceil(f);
            for (int m = -l; m <= l; ++m) {
                float g = (float)Mth.abs(m) - 0.25f;
                for (int n = -l; n <= l; ++n) {
                    float h = (float)Mth.abs(n) - 0.25f;
                    if ((m != 0 || n != 0) && g * g + h * h > f * f || (m == -l || m == l || n == -l || n == l) && randomSource.nextFloat() > 0.75f) continue;
                    BlockState blockState = worldGenLevel.getBlockState(blockPos.offset(m, k, n));
                    if (blockState.isAir() || IceSpikeFeature.isDirt(blockState) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.ICE)) {
                        this.setBlock(worldGenLevel, blockPos.offset(m, k, n), Blocks.PACKED_ICE.defaultBlockState());
                    }
                    if (k == 0 || l <= 1 || !(blockState = worldGenLevel.getBlockState(blockPos.offset(m, -k, n))).isAir() && !IceSpikeFeature.isDirt(blockState) && !blockState.is(Blocks.SNOW_BLOCK) && !blockState.is(Blocks.ICE)) continue;
                    this.setBlock(worldGenLevel, blockPos.offset(m, -k, n), Blocks.PACKED_ICE.defaultBlockState());
                }
            }
        }
        k = j - 1;
        if (k < 0) {
            k = 0;
        } else if (k > 1) {
            k = 1;
        }
        for (int o = -k; o <= k; ++o) {
            for (l = -k; l <= k; ++l) {
                BlockState blockState2;
                BlockPos blockPos2 = blockPos.offset(o, -1, l);
                int p = 50;
                if (Math.abs(o) == 1 && Math.abs(l) == 1) {
                    p = randomSource.nextInt(5);
                }
                while (blockPos2.getY() > 50 && ((blockState2 = worldGenLevel.getBlockState(blockPos2)).isAir() || IceSpikeFeature.isDirt(blockState2) || blockState2.is(Blocks.SNOW_BLOCK) || blockState2.is(Blocks.ICE) || blockState2.is(Blocks.PACKED_ICE))) {
                    this.setBlock(worldGenLevel, blockPos2, Blocks.PACKED_ICE.defaultBlockState());
                    blockPos2 = blockPos2.below();
                    if (--p > 0) continue;
                    blockPos2 = blockPos2.below(randomSource.nextInt(5) + 1);
                    p = randomSource.nextInt(5);
                }
            }
        }
        return true;
    }
}

