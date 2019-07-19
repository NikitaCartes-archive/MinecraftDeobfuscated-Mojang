/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class ReedsFeature
extends Feature<NoneFeatureConfiguration> {
    public ReedsFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        int i = 0;
        for (int j = 0; j < 20; ++j) {
            BlockPos blockPos3;
            BlockPos blockPos2 = blockPos.offset(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
            if (!levelAccessor.isEmptyBlock(blockPos2) || !levelAccessor.getFluidState((blockPos3 = blockPos2.below()).west()).is(FluidTags.WATER) && !levelAccessor.getFluidState(blockPos3.east()).is(FluidTags.WATER) && !levelAccessor.getFluidState(blockPos3.north()).is(FluidTags.WATER) && !levelAccessor.getFluidState(blockPos3.south()).is(FluidTags.WATER)) continue;
            int k = 2 + random.nextInt(random.nextInt(3) + 1);
            for (int l = 0; l < k; ++l) {
                if (!Blocks.SUGAR_CANE.defaultBlockState().canSurvive(levelAccessor, blockPos2)) continue;
                levelAccessor.setBlock(blockPos2.above(l), Blocks.SUGAR_CANE.defaultBlockState(), 2);
                ++i;
            }
        }
        return i > 0;
    }
}

