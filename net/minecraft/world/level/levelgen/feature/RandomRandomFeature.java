/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;

public class RandomRandomFeature
extends Feature<RandomRandomFeatureConfiguration> {
    public RandomRandomFeature(Codec<RandomRandomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomRandomFeatureConfiguration randomRandomFeatureConfiguration) {
        int i = random.nextInt(5) - 3 + randomRandomFeatureConfiguration.count;
        for (int j = 0; j < i; ++j) {
            int k = random.nextInt(randomRandomFeatureConfiguration.features.size());
            ConfiguredFeature<?, ?> configuredFeature = randomRandomFeatureConfiguration.features.get(k);
            configuredFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos);
        }
        return true;
    }
}

