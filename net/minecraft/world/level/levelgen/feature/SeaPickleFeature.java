/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class SeaPickleFeature
extends Feature<CountConfiguration> {
    public SeaPickleFeature(Codec<CountConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CountConfiguration> featurePlaceContext) {
        int i = 0;
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        int j = featurePlaceContext.config().count().sample(randomSource);
        for (int k = 0; k < j; ++k) {
            int l = randomSource.nextInt(8) - randomSource.nextInt(8);
            int m = randomSource.nextInt(8) - randomSource.nextInt(8);
            int n = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + l, blockPos.getZ() + m);
            BlockPos blockPos2 = new BlockPos(blockPos.getX() + l, n, blockPos.getZ() + m);
            BlockState blockState = (BlockState)Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, randomSource.nextInt(4) + 1);
            if (!worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER) || !blockState.canSurvive(worldGenLevel, blockPos2)) continue;
            worldGenLevel.setBlock(blockPos2, blockState, 2);
            ++i;
        }
        return i > 0;
    }
}

