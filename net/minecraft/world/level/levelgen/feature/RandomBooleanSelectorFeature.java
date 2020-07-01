/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public class RandomBooleanSelectorFeature
extends Feature<RandomBooleanFeatureConfiguration> {
    public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration) {
        boolean bl = random.nextBoolean();
        if (bl) {
            return randomBooleanFeatureConfiguration.featureTrue.place(worldGenLevel, chunkGenerator, random, blockPos);
        }
        return randomBooleanFeatureConfiguration.featureFalse.place(worldGenLevel, chunkGenerator, random, blockPos);
    }
}

