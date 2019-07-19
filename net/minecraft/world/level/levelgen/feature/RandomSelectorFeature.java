/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

public class RandomSelectorFeature
extends Feature<RandomFeatureConfig> {
    public RandomSelectorFeature(Function<Dynamic<?>, ? extends RandomFeatureConfig> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, RandomFeatureConfig randomFeatureConfig) {
        for (WeightedConfiguredFeature<?> weightedConfiguredFeature : randomFeatureConfig.features) {
            if (!(random.nextFloat() < weightedConfiguredFeature.chance.floatValue())) continue;
            return weightedConfiguredFeature.place(levelAccessor, chunkGenerator, random, blockPos);
        }
        return randomFeatureConfig.defaultFeature.place(levelAccessor, chunkGenerator, random, blockPos);
    }
}

