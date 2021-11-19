package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					PlacedFeature.CODEC.fieldOf("feature_true").forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureTrue),
					PlacedFeature.CODEC.fieldOf("feature_false").forGetter(randomBooleanFeatureConfiguration -> randomBooleanFeatureConfiguration.featureFalse)
				)
				.apply(instance, RandomBooleanFeatureConfiguration::new)
	);
	public final Supplier<PlacedFeature> featureTrue;
	public final Supplier<PlacedFeature> featureFalse;

	public RandomBooleanFeatureConfiguration(Supplier<PlacedFeature> supplier, Supplier<PlacedFeature> supplier2) {
		this.featureTrue = supplier;
		this.featureFalse = supplier2;
	}

	@Override
	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return Stream.concat(((PlacedFeature)this.featureTrue.get()).getFeatures(), ((PlacedFeature)this.featureFalse.get()).getFeatures());
	}
}
