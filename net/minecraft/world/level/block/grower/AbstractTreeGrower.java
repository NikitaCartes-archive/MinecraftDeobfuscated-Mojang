/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random var1);

    public boolean growTree(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
        AbstractTreeFeature<NoneFeatureConfiguration> abstractTreeFeature = this.getFeature(random);
        if (abstractTreeFeature == null) {
            return false;
        }
        levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
        if (abstractTreeFeature.place(levelAccessor, chunkGenerator, random, blockPos, FeatureConfiguration.NONE, false)) {
            return true;
        }
        levelAccessor.setBlock(blockPos, blockState, 4);
        return false;
    }
}

