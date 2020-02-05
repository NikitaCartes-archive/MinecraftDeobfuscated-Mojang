/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature
extends Feature<BlockPileConfiguration> {
    public NetherForestVegetationFeature(Function<Dynamic<?>, ? extends BlockPileConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration) {
        Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
        while (!block.is(BlockTags.NYLIUM) && blockPos.getY() > 0) {
            blockPos = blockPos.below();
            block = levelAccessor.getBlockState(blockPos).getBlock();
        }
        int i = blockPos.getY();
        if (i < 1 || i + 1 >= 256) {
            return false;
        }
        int j = 0;
        for (int k = 0; k < 64; ++k) {
            BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            BlockState blockState = blockPileConfiguration.stateProvider.getState(random, blockPos2);
            if (!levelAccessor.isEmptyBlock(blockPos2) || blockPos2.getY() <= 0 || !blockState.canSurvive(levelAccessor, blockPos2)) continue;
            levelAccessor.setBlock(blockPos2, blockState, 2);
            ++j;
        }
        return j > 0;
    }
}

