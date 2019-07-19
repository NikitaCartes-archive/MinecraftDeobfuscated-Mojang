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
import net.minecraft.world.level.levelgen.feature.RandomBooleanFeatureConfig;

public class RandomBooleanSelectorFeature
extends Feature<RandomBooleanFeatureConfig> {
    public RandomBooleanSelectorFeature(Function<Dynamic<?>, ? extends RandomBooleanFeatureConfig> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, RandomBooleanFeatureConfig randomBooleanFeatureConfig) {
        boolean bl = random.nextBoolean();
        if (bl) {
            return randomBooleanFeatureConfig.featureTrue.place(levelAccessor, chunkGenerator, random, blockPos);
        }
        return randomBooleanFeatureConfig.featureFalse.place(levelAccessor, chunkGenerator, random, blockPos);
    }
}

