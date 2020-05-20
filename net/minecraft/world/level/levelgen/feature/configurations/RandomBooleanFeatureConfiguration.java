/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RandomBooleanFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.CODEC.fieldOf("feature_true")).forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureTrue), ((MapCodec)ConfiguredFeature.CODEC.fieldOf("feature_false")).forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureFalse)).apply((Applicative<RandomBooleanFeatureConfiguration, ?>)instance, RandomBooleanFeatureConfiguration::new));
    public final ConfiguredFeature<?, ?> featureTrue;
    public final ConfiguredFeature<?, ?> featureFalse;

    public RandomBooleanFeatureConfiguration(ConfiguredFeature<?, ?> configuredFeature, ConfiguredFeature<?, ?> configuredFeature2) {
        this.featureTrue = configuredFeature;
        this.featureFalse = configuredFeature2;
    }
}

