/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.apply2(RandomFeatureConfiguration::new, ((MapCodec)WeightedConfiguredFeature.CODEC.listOf().fieldOf("features")).forGetter(randomFeatureConfiguration -> randomFeatureConfiguration.features), ((MapCodec)ConfiguredFeature.CODEC.fieldOf("default")).forGetter(randomFeatureConfiguration -> randomFeatureConfiguration.defaultFeature)));
    public final List<WeightedConfiguredFeature> features;
    public final Supplier<ConfiguredFeature<?, ?>> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedConfiguredFeature> list, ConfiguredFeature<?, ?> configuredFeature) {
        this(list, () -> configuredFeature);
    }

    private RandomFeatureConfiguration(List<WeightedConfiguredFeature> list, Supplier<ConfiguredFeature<?, ?>> supplier) {
        this.features = list;
        this.defaultFeature = supplier;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.features.stream().flatMap(weightedConfiguredFeature -> weightedConfiguredFeature.feature.get().getFeatures()), this.defaultFeature.get().getFeatures());
    }
}

