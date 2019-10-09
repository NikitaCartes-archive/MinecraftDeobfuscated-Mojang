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
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature
extends Feature<RandomFeatureConfiguration> {
    public RandomSelectorFeature(Function<Dynamic<?>, ? extends RandomFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, RandomFeatureConfiguration randomFeatureConfiguration) {
        for (WeightedConfiguredFeature<?> weightedConfiguredFeature : randomFeatureConfiguration.features) {
            if (!(random.nextFloat() < weightedConfiguredFeature.chance)) continue;
            return weightedConfiguredFeature.place(levelAccessor, chunkGenerator, random, blockPos);
        }
        return randomFeatureConfiguration.defaultFeature.place(levelAccessor, chunkGenerator, random, blockPos);
    }
}

