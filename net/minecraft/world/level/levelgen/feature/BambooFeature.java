/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class BambooFeature
extends Feature<ProbabilityFeatureConfiguration> {
    private static final BlockState BAMBOO_TRUNK = (BlockState)((BlockState)((BlockState)Blocks.BAMBOO.defaultBlockState().setValue(BambooBlock.AGE, 1)).setValue(BambooBlock.LEAVES, BambooLeaves.NONE)).setValue(BambooBlock.STAGE, 0);
    private static final BlockState BAMBOO_FINAL_LARGE = (BlockState)((BlockState)BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE)).setValue(BambooBlock.STAGE, 1);
    private static final BlockState BAMBOO_TOP_LARGE = (BlockState)BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE);
    private static final BlockState BAMBOO_TOP_SMALL = (BlockState)BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL);

    public BambooFeature(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> featurePlaceContext) {
        int i = 0;
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        ProbabilityFeatureConfiguration probabilityFeatureConfiguration = featurePlaceContext.config();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();
        if (worldGenLevel.isEmptyBlock(mutableBlockPos)) {
            if (Blocks.BAMBOO.defaultBlockState().canSurvive(worldGenLevel, mutableBlockPos)) {
                int k;
                int j = randomSource.nextInt(12) + 5;
                if (randomSource.nextFloat() < probabilityFeatureConfiguration.probability) {
                    k = randomSource.nextInt(4) + 1;
                    for (int l = blockPos.getX() - k; l <= blockPos.getX() + k; ++l) {
                        for (int m = blockPos.getZ() - k; m <= blockPos.getZ() + k; ++m) {
                            int o;
                            int n = l - blockPos.getX();
                            if (n * n + (o = m - blockPos.getZ()) * o > k * k) continue;
                            mutableBlockPos2.set(l, worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE, l, m) - 1, m);
                            if (!BambooFeature.isDirt(worldGenLevel.getBlockState(mutableBlockPos2))) continue;
                            worldGenLevel.setBlock(mutableBlockPos2, Blocks.PODZOL.defaultBlockState(), 2);
                        }
                    }
                }
                for (k = 0; k < j && worldGenLevel.isEmptyBlock(mutableBlockPos); ++k) {
                    worldGenLevel.setBlock(mutableBlockPos, BAMBOO_TRUNK, 2);
                    mutableBlockPos.move(Direction.UP, 1);
                }
                if (mutableBlockPos.getY() - blockPos.getY() >= 3) {
                    worldGenLevel.setBlock(mutableBlockPos, BAMBOO_FINAL_LARGE, 2);
                    worldGenLevel.setBlock(mutableBlockPos.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
                    worldGenLevel.setBlock(mutableBlockPos.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
                }
            }
            ++i;
        }
        return i > 0;
    }
}

