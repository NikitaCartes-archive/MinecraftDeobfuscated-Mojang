/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

public class RandomFeatureConfig
implements FeatureConfiguration {
    public final List<WeightedConfiguredFeature<?>> features;
    public final ConfiguredFeature<?> defaultFeature;

    public RandomFeatureConfig(List<WeightedConfiguredFeature<?>> list, ConfiguredFeature<?> configuredFeature) {
        this.features = list;
        this.defaultFeature = configuredFeature;
    }

    public RandomFeatureConfig(Feature<?>[] features, FeatureConfiguration[] featureConfigurations, float[] fs, Feature<?> feature, FeatureConfiguration featureConfiguration) {
        this(IntStream.range(0, features.length).mapToObj(i -> RandomFeatureConfig.getWeightedConfiguredFeature(features[i], featureConfigurations[i], fs[i])).collect(Collectors.toList()), RandomFeatureConfig.getDefaultFeature(feature, featureConfiguration));
    }

    private static <FC extends FeatureConfiguration> WeightedConfiguredFeature<FC> getWeightedConfiguredFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration, float f) {
        return new WeightedConfiguredFeature<FeatureConfiguration>(feature, featureConfiguration, Float.valueOf(f));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getDefaultFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration) {
        return new ConfiguredFeature<FeatureConfiguration>(feature, featureConfiguration);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        Object object = dynamicOps.createList(this.features.stream().map(weightedConfiguredFeature -> weightedConfiguredFeature.serialize(dynamicOps).getValue()));
        T object2 = this.defaultFeature.serialize(dynamicOps).getValue();
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("features"), object, dynamicOps.createString("default"), object2)));
    }

    public static <T> RandomFeatureConfig deserialize(Dynamic<T> dynamic) {
        List<WeightedConfiguredFeature<?>> list = dynamic.get("features").asList(WeightedConfiguredFeature::deserialize);
        ConfiguredFeature<?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("default").orElseEmptyMap());
        return new RandomFeatureConfig(list, configuredFeature);
    }
}

