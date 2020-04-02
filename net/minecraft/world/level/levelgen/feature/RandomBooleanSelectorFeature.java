/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public class RandomBooleanSelectorFeature
extends Feature<RandomBooleanFeatureConfiguration> {
    public RandomBooleanSelectorFeature(Function<Dynamic<?>, ? extends RandomBooleanFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration) {
        boolean bl = random.nextBoolean();
        if (bl) {
            return randomBooleanFeatureConfiguration.featureTrue.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
        }
        return randomBooleanFeatureConfiguration.featureFalse.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
    }
}

