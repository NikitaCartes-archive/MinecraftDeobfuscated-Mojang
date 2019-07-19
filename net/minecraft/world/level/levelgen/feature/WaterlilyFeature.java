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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class WaterlilyFeature
extends Feature<NoneFeatureConfiguration> {
    public WaterlilyFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        BlockPos blockPos3;
        BlockPos blockPos2 = blockPos;
        while (blockPos2.getY() > 0 && levelAccessor.isEmptyBlock(blockPos3 = blockPos2.below())) {
            blockPos2 = blockPos3;
        }
        for (int i = 0; i < 10; ++i) {
            BlockPos blockPos4 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            BlockState blockState = Blocks.LILY_PAD.defaultBlockState();
            if (!levelAccessor.isEmptyBlock(blockPos4) || !blockState.canSurvive(levelAccessor, blockPos4)) continue;
            levelAccessor.setBlock(blockPos4, blockState, 2);
        }
        return true;
    }
}

