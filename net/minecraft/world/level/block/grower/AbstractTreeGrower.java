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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random var1);

    public boolean growTree(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
        ConfiguredFeature<SmallTreeConfiguration, ?> configuredFeature = this.getConfiguredFeature(random);
        if (configuredFeature == null) {
            return false;
        }
        levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
        if (configuredFeature.place(levelAccessor, chunkGenerator, random, blockPos)) {
            return true;
        }
        levelAccessor.setBlock(blockPos, blockState, 4);
        return false;
    }
}

