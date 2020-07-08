package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ConfiguredFeature.CODEC.fieldOf("feature_true").forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureTrue),
					ConfiguredFeature.CODEC.fieldOf("feature_false").forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureFalse)
				)
				.apply(instance, RandomBooleanFeatureConfiguration::new)
	);
	public final Supplier<ConfiguredFeature<?, ?>> featureTrue;
	public final Supplier<ConfiguredFeature<?, ?>> featureFalse;

	public RandomBooleanFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> supplier, Supplier<ConfiguredFeature<?, ?>> supplier2) {
		this.featureTrue = supplier;
		this.featureFalse = supplier2;
	}
}
