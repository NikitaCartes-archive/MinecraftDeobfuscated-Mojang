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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.GrassConfiguration;

public class GrassFeature
extends Feature<GrassConfiguration> {
    public GrassFeature(Function<Dynamic<?>, ? extends GrassConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, GrassConfiguration grassConfiguration) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        while ((blockState.isAir() || blockState.is(BlockTags.LEAVES)) && blockPos.getY() > 0) {
            blockPos = blockPos.below();
            blockState = levelAccessor.getBlockState(blockPos);
        }
        int i = 0;
        for (int j = 0; j < 128; ++j) {
            BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            if (!levelAccessor.isEmptyBlock(blockPos2) || !grassConfiguration.state.canSurvive(levelAccessor, blockPos2)) continue;
            levelAccessor.setBlock(blockPos2, grassConfiguration.state, 2);
            ++i;
        }
        return i > 0;
    }
}

