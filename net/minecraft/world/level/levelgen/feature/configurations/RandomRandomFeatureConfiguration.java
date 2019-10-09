/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RandomRandomFeatureConfiguration
implements FeatureConfiguration {
    public final List<ConfiguredFeature<?, ?>> features;
    public final int count;

    public RandomRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> list, int i) {
        this.features = list;
        this.count = i;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("features"), dynamicOps.createList(this.features.stream().map(configuredFeature -> configuredFeature.serialize(dynamicOps).getValue())), dynamicOps.createString("count"), dynamicOps.createInt(this.count))));
    }

    public static <T> RandomRandomFeatureConfiguration deserialize(Dynamic<T> dynamic) {
        List<ConfiguredFeature<?, ?>> list = dynamic.get("features").asList(ConfiguredFeature::deserialize);
        int i = dynamic.get("count").asInt(0);
        return new RandomRandomFeatureConfiguration(list, i);
    }
}

