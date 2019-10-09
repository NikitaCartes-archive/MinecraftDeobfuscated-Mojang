/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMegaTreeGrower
extends AbstractTreeGrower {
    @Override
    public boolean growTree(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
        for (int i = 0; i >= -1; --i) {
            for (int j = 0; j >= -1; --j) {
                if (!AbstractMegaTreeGrower.isTwoByTwoSapling(blockState, levelAccessor, blockPos, i, j)) continue;
                return this.placeMega(levelAccessor, chunkGenerator, blockPos, blockState, random, i, j);
            }
        }
        return super.growTree(levelAccessor, chunkGenerator, blockPos, blockState, random);
    }

    @Nullable
    protected abstract ConfiguredFeature<MegaTreeConfiguration, ?> getConfiguredMegaFeature(Random var1);

    public boolean placeMega(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, int i, int j) {
        ConfiguredFeature<MegaTreeConfiguration, ?> configuredFeature = this.getConfiguredMegaFeature(random);
        if (configuredFeature == null) {
            return false;
        }
        BlockState blockState2 = Blocks.AIR.defaultBlockState();
        levelAccessor.setBlock(blockPos.offset(i, 0, j), blockState2, 4);
        levelAccessor.setBlock(blockPos.offset(i + 1, 0, j), blockState2, 4);
        levelAccessor.setBlock(blockPos.offset(i, 0, j + 1), blockState2, 4);
        levelAccessor.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState2, 4);
        if (configuredFeature.place(levelAccessor, chunkGenerator, random, blockPos.offset(i, 0, j))) {
            return true;
        }
        levelAccessor.setBlock(blockPos.offset(i, 0, j), blockState, 4);
        levelAccessor.setBlock(blockPos.offset(i + 1, 0, j), blockState, 4);
        levelAccessor.setBlock(blockPos.offset(i, 0, j + 1), blockState, 4);
        levelAccessor.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState, 4);
        return false;
    }

    public static boolean isTwoByTwoSapling(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, int i, int j) {
        Block block = blockState.getBlock();
        return block == blockGetter.getBlockState(blockPos.offset(i, 0, j)).getBlock() && block == blockGetter.getBlockState(blockPos.offset(i + 1, 0, j)).getBlock() && block == blockGetter.getBlockState(blockPos.offset(i, 0, j + 1)).getBlock() && block == blockGetter.getBlockState(blockPos.offset(i + 1, 0, j + 1)).getBlock();
    }
}

