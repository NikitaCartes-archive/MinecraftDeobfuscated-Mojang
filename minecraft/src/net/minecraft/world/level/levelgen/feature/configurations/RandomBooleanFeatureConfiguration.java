package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ConfiguredFeature.CODEC.fieldOf("feature_true").forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureTrue),
					ConfiguredFeature.CODEC.fieldOf("feature_false").forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureFalse)
				)
				.apply(instance, RandomBooleanFeatureConfiguration::new)
	);
	public final ConfiguredFeature<?, ?> featureTrue;
	public final ConfiguredFeature<?, ?> featureFalse;

	public RandomBooleanFeatureConfiguration(ConfiguredFeature<?, ?> configuredFeature, ConfiguredFeature<?, ?> configuredFeature2) {
		this.featureTrue = configuredFeature;
		this.featureFalse = configuredFeature2;
	}
}
