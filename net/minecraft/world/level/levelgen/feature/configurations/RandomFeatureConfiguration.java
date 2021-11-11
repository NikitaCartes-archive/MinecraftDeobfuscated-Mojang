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
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.apply2(RandomFeatureConfiguration::new, ((MapCodec)WeightedPlacedFeature.CODEC.listOf().fieldOf("features")).forGetter(randomFeatureConfiguration -> randomFeatureConfiguration.features), ((MapCodec)PlacedFeature.CODEC.fieldOf("default")).forGetter(randomFeatureConfiguration -> randomFeatureConfiguration.defaultFeature)));
    public final List<WeightedPlacedFeature> features;
    public final Supplier<PlacedFeature> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedPlacedFeature> list, PlacedFeature placedFeature) {
        this(list, () -> placedFeature);
    }

    private RandomFeatureConfiguration(List<WeightedPlacedFeature> list, Supplier<PlacedFeature> supplier) {
        this.features = list;
        this.defaultFeature = supplier;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.features.stream().flatMap(weightedPlacedFeature -> weightedPlacedFeature.feature.get().getFeatures()), this.defaultFeature.get().getFeatures());
    }
}

