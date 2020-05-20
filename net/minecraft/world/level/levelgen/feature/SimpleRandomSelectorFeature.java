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
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;

public class SimpleRandomSelectorFeature
extends Feature<SimpleRandomFeatureConfiguration> {
    public SimpleRandomSelectorFeature(Codec<SimpleRandomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SimpleRandomFeatureConfiguration simpleRandomFeatureConfiguration) {
        int i = random.nextInt(simpleRandomFeatureConfiguration.features.size());
        ConfiguredFeature<?, ?> configuredFeature = simpleRandomFeatureConfiguration.features.get(i);
        return configuredFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos);
    }
}

