/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class WeightedConfiguredFeature<FC extends FeatureConfiguration> {
    public static final Codec<WeightedConfiguredFeature<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.CODEC.fieldOf("feature")).forGetter(weightedConfiguredFeature -> weightedConfiguredFeature.feature), ((MapCodec)Codec.FLOAT.fieldOf("chance")).forGetter(weightedConfiguredFeature -> Float.valueOf(weightedConfiguredFeature.chance))).apply((Applicative<WeightedConfiguredFeature, ?>)instance, WeightedConfiguredFeature::new));
    public final ConfiguredFeature<FC, ?> feature;
    public final float chance;

    public WeightedConfiguredFeature(ConfiguredFeature<FC, ?> configuredFeature, float f) {
        this.feature = configuredFeature;
        this.chance = f;
    }

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return this.feature.place(worldGenLevel, chunkGenerator, random, blockPos);
    }
}

