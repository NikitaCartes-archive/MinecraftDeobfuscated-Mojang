/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature
extends Feature<BlockStateConfiguration> {
    public BlockBlobFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> featurePlaceContext) {
        BlockState blockState;
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockStateConfiguration blockStateConfiguration = featurePlaceContext.config();
        while (blockPos.getY() > worldGenLevel.getMinBuildHeight() + 3 && (worldGenLevel.isEmptyBlock(blockPos.below()) || !BlockBlobFeature.isDirt(blockState = worldGenLevel.getBlockState(blockPos.below())) && !BlockBlobFeature.isStone(blockState))) {
            blockPos = blockPos.below();
        }
        if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 3) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int j = randomSource.nextInt(2);
            int k = randomSource.nextInt(2);
            int l = randomSource.nextInt(2);
            float f = (float)(j + k + l) * 0.333f + 0.5f;
            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-j, -k, -l), blockPos.offset(j, k, l))) {
                if (!(blockPos2.distSqr(blockPos) <= (double)(f * f))) continue;
                worldGenLevel.setBlock(blockPos2, blockStateConfiguration.state, 3);
            }
            blockPos = blockPos.offset(-1 + randomSource.nextInt(2), -randomSource.nextInt(2), -1 + randomSource.nextInt(2));
        }
        return true;
    }
}

