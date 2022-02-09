/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)PlacedFeature.CODEC.fieldOf("feature_true")).forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureTrue), ((MapCodec)PlacedFeature.CODEC.fieldOf("feature_false")).forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureFalse)).apply((Applicative<RandomBooleanFeatureConfiguration, ?>)instance, RandomBooleanFeatureConfiguration::new));
    public final Holder<PlacedFeature> featureTrue;
    public final Holder<PlacedFeature> featureFalse;

    public RandomBooleanFeatureConfiguration(Holder<PlacedFeature> holder, Holder<PlacedFeature> holder2) {
        this.featureTrue = holder;
        this.featureFalse = holder2;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.featureTrue.value().getFeatures(), this.featureFalse.value().getFeatures());
    }
}

