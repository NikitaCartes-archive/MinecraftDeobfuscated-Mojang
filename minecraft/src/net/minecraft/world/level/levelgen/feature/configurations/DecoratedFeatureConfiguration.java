package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

public class DecoratedFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<DecoratedFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ConfiguredFeature.CODEC.fieldOf("feature").forGetter(decoratedFeatureConfiguration -> decoratedFeatureConfiguration.feature),
					ConfiguredDecorator.CODEC.fieldOf("decorator").forGetter(decoratedFeatureConfiguration -> decoratedFeatureConfiguration.decorator)
				)
				.apply(instance, DecoratedFeatureConfiguration::new)
	);
	public final Supplier<ConfiguredFeature<?, ?>> feature;
	public final ConfiguredDecorator<?> decorator;

	public DecoratedFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> supplier, ConfiguredDecorator<?> configuredDecorator) {
		this.feature = supplier;
		this.decorator = configuredDecorator;
	}

	public String toString() {
		return String.format(
			"< %s [%s | %s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(((ConfiguredFeature)this.feature.get()).feature()), this.decorator
		);
	}

	@Override
	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return ((ConfiguredFeature)this.feature.get()).getFeatures();
	}
}
