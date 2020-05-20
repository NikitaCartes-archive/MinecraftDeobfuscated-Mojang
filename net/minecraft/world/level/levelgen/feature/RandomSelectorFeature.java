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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature
extends Feature<RandomFeatureConfiguration> {
    public RandomSelectorFeature(Codec<RandomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomFeatureConfiguration randomFeatureConfiguration) {
        for (WeightedConfiguredFeature<?> weightedConfiguredFeature : randomFeatureConfiguration.features) {
            if (!(random.nextFloat() < weightedConfiguredFeature.chance)) continue;
            return weightedConfiguredFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos);
        }
        return randomFeatureConfiguration.defaultFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos);
    }
}

