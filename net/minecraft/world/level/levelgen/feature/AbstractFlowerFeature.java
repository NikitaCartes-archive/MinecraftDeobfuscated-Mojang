/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class AbstractFlowerFeature<U extends FeatureConfiguration>
extends Feature<U> {
    public AbstractFlowerFeature(Function<Dynamic<?>, ? extends U> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, U featureConfiguration) {
        BlockState blockState = this.getRandomFlower(random, blockPos, featureConfiguration);
        int i = 0;
        for (int j = 0; j < this.getCount(featureConfiguration); ++j) {
            BlockPos blockPos2 = this.getPos(random, blockPos, featureConfiguration);
            if (!levelAccessor.isEmptyBlock(blockPos2) || blockPos2.getY() >= 255 || !blockState.canSurvive(levelAccessor, blockPos2) || !this.isValid(levelAccessor, blockPos2, featureConfiguration)) continue;
            levelAccessor.setBlock(blockPos2, blockState, 2);
            ++i;
        }
        return i > 0;
    }

    public abstract boolean isValid(LevelAccessor var1, BlockPos var2, U var3);

    public abstract int getCount(U var1);

    public abstract BlockPos getPos(Random var1, BlockPos var2, U var3);

    public abstract BlockState getRandomFlower(Random var1, BlockPos var2, U var3);
}

