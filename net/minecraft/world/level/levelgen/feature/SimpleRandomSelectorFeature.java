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
import net.minecraft.world.level.levelgen.feature.SimpleRandomFeatureConfig;

public class SimpleRandomSelectorFeature
extends Feature<SimpleRandomFeatureConfig> {
    public SimpleRandomSelectorFeature(Function<Dynamic<?>, ? extends SimpleRandomFeatureConfig> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, SimpleRandomFeatureConfig simpleRandomFeatureConfig) {
        int i = random.nextInt(simpleRandomFeatureConfig.features.size());
        ConfiguredFeature<?> configuredFeature = simpleRandomFeatureConfig.features.get(i);
        return configuredFeature.place(levelAccessor, chunkGenerator, random, blockPos);
    }
}

