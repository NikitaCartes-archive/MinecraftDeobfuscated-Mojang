/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;

public class RandomBooleanFeatureConfig
implements FeatureConfiguration {
    public final ConfiguredFeature<?> featureTrue;
    public final ConfiguredFeature<?> featureFalse;

    public RandomBooleanFeatureConfig(ConfiguredFeature<?> configuredFeature, ConfiguredFeature<?> configuredFeature2) {
        this.featureTrue = configuredFeature;
        this.featureFalse = configuredFeature2;
    }

    public RandomBooleanFeatureConfig(Feature<?> feature, FeatureConfiguration featureConfiguration, Feature<?> feature2, FeatureConfiguration featureConfiguration2) {
        this(RandomBooleanFeatureConfig.getFeature(feature, featureConfiguration), RandomBooleanFeatureConfig.getFeature(feature2, featureConfiguration2));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration) {
        return new ConfiguredFeature<FeatureConfiguration>(feature, featureConfiguration);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("feature_true"), this.featureTrue.serialize(dynamicOps).getValue(), dynamicOps.createString("feature_false"), this.featureFalse.serialize(dynamicOps).getValue())));
    }

    public static <T> RandomBooleanFeatureConfig deserialize(Dynamic<T> dynamic) {
        ConfiguredFeature<?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("feature_true").orElseEmptyMap());
        ConfiguredFeature<?> configuredFeature2 = ConfiguredFeature.deserialize(dynamic.get("feature_false").orElseEmptyMap());
        return new RandomBooleanFeatureConfig(configuredFeature, configuredFeature2);
    }
}

