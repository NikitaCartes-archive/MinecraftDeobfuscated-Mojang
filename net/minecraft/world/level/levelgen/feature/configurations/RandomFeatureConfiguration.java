/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RandomFeatureConfiguration
implements FeatureConfiguration {
    public final List<WeightedConfiguredFeature<?>> features;
    public final ConfiguredFeature<?, ?> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedConfiguredFeature<?>> list, ConfiguredFeature<?, ?> configuredFeature) {
        this.features = list;
        this.defaultFeature = configuredFeature;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        Object object = dynamicOps.createList(this.features.stream().map(weightedConfiguredFeature -> weightedConfiguredFeature.serialize(dynamicOps).getValue()));
        T object2 = this.defaultFeature.serialize(dynamicOps).getValue();
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("features"), object, dynamicOps.createString("default"), object2)));
    }

    public static <T> RandomFeatureConfiguration deserialize(Dynamic<T> dynamic) {
        List<WeightedConfiguredFeature<?>> list = dynamic.get("features").asList(WeightedConfiguredFeature::deserialize);
        ConfiguredFeature<?, ?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("default").orElseEmptyMap());
        return new RandomFeatureConfiguration(list, configuredFeature);
    }
}

