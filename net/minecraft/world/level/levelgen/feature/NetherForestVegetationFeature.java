/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature
extends Feature<BlockPileConfiguration> {
    public NetherForestVegetationFeature(Codec<BlockPileConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration) {
        return NetherForestVegetationFeature.place(worldGenLevel, random, blockPos, blockPileConfiguration, 8, 4);
    }

    public static boolean place(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration, int i, int j) {
        Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
        while (!block.is(BlockTags.NYLIUM) && blockPos.getY() > 0) {
            blockPos = blockPos.below();
            block = levelAccessor.getBlockState(blockPos).getBlock();
        }
        int k = blockPos.getY();
        if (k < 1 || k + 1 >= 256) {
            return false;
        }
        int l = 0;
        for (int m = 0; m < i * i; ++m) {
            BlockPos blockPos2 = blockPos.offset(random.nextInt(i) - random.nextInt(i), random.nextInt(j) - random.nextInt(j), random.nextInt(i) - random.nextInt(i));
            BlockState blockState = blockPileConfiguration.stateProvider.getState(random, blockPos2);
            if (!levelAccessor.isEmptyBlock(blockPos2) || blockPos2.getY() <= 0 || !blockState.canSurvive(levelAccessor, blockPos2)) continue;
            levelAccessor.setBlock(blockPos2, blockState, 2);
            ++l;
        }
        return l > 0;
    }
}

