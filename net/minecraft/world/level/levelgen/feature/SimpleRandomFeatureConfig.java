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

public class SimpleRandomFeatureConfig
implements FeatureConfiguration {
    public final List<ConfiguredFeature<?>> features;

    public SimpleRandomFeatureConfig(List<ConfiguredFeature<?>> list) {
        this.features = list;
    }

    public SimpleRandomFeatureConfig(Feature<?>[] features, FeatureConfiguration[] featureConfigurations) {
        this(IntStream.range(0, features.length).mapToObj(i -> SimpleRandomFeatureConfig.getConfiguredFeature(features[i], featureConfigurations[i])).collect(Collectors.toList()));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC> getConfiguredFeature(Feature<FC> feature, FeatureConfiguration featureConfiguration) {
        return new ConfiguredFeature<FeatureConfiguration>(feature, featureConfiguration);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("features"), dynamicOps.createList(this.features.stream().map(configuredFeature -> configuredFeature.serialize(dynamicOps).getValue())))));
    }

    public static <T> SimpleRandomFeatureConfig deserialize(Dynamic<T> dynamic) {
        List<ConfiguredFeature<?>> list = dynamic.get("features").asList(ConfiguredFeature::deserialize);
        return new SimpleRandomFeatureConfig(list);
    }
}

