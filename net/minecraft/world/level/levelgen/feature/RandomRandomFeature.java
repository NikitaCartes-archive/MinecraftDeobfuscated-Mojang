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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomRandomFeatureConfig;

public class RandomRandomFeature
extends Feature<RandomRandomFeatureConfig> {
    public RandomRandomFeature(Function<Dynamic<?>, ? extends RandomRandomFeatureConfig> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, RandomRandomFeatureConfig randomRandomFeatureConfig) {
        int i = random.nextInt(5) - 3 + randomRandomFeatureConfig.count;
        for (int j = 0; j < i; ++j) {
            int k = random.nextInt(randomRandomFeatureConfig.features.size());
            ConfiguredFeature<?> configuredFeature = randomRandomFeatureConfig.features.get(k);
            configuredFeature.place(levelAccessor, chunkGenerator, random, blockPos);
        }
        return true;
    }
}

