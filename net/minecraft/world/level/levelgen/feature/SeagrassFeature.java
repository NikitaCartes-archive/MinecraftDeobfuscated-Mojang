/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrass;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;

public class SeagrassFeature
extends Feature<SeagrassFeatureConfiguration> {
    public SeagrassFeature(Function<Dynamic<?>, ? extends SeagrassFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, SeagrassFeatureConfiguration seagrassFeatureConfiguration) {
        int i = 0;
        for (int j = 0; j < seagrassFeatureConfiguration.count; ++j) {
            BlockState blockState;
            int k = random.nextInt(8) - random.nextInt(8);
            int l = random.nextInt(8) - random.nextInt(8);
            int m = levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + k, blockPos.getZ() + l);
            BlockPos blockPos2 = new BlockPos(blockPos.getX() + k, m, blockPos.getZ() + l);
            if (levelAccessor.getBlockState(blockPos2).getBlock() != Blocks.WATER) continue;
            boolean bl = random.nextDouble() < seagrassFeatureConfiguration.tallSeagrassProbability;
            BlockState blockState2 = blockState = bl ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
            if (!blockState.canSurvive(levelAccessor, blockPos2)) continue;
            if (bl) {
                BlockState blockState22 = (BlockState)blockState.setValue(TallSeagrass.HALF, DoubleBlockHalf.UPPER);
                BlockPos blockPos3 = blockPos2.above();
                if (levelAccessor.getBlockState(blockPos3).getBlock() == Blocks.WATER) {
                    levelAccessor.setBlock(blockPos2, blockState, 2);
                    levelAccessor.setBlock(blockPos3, blockState22, 2);
                }
            } else {
                levelAccessor.setBlock(blockPos2, blockState, 2);
            }
            ++i;
        }
        return i > 0;
    }
}

