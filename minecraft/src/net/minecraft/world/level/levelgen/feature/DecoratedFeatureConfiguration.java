package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class DecoratedFeatureConfiguration implements FeatureConfiguration {
	public final ConfiguredFeature<?> feature;
	public final ConfiguredDecorator<?> decorator;

	public DecoratedFeatureConfiguration(ConfiguredFeature<?> configuredFeature, ConfiguredDecorator<?> configuredDecorator) {
		this.feature = configuredFeature;
		this.decorator = configuredDecorator;
	}

	public <F extends FeatureConfiguration, D extends DecoratorConfiguration> DecoratedFeatureConfiguration(
		Feature<F> feature, F featureConfiguration, FeatureDecorator<D> featureDecorator, D decoratorConfiguration
	) {
		this(new ConfiguredFeature<>(feature, featureConfiguration), new ConfiguredDecorator<>(featureDecorator, decoratorConfiguration));
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("feature"),
					this.feature.serialize(dynamicOps).getValue(),
					dynamicOps.createString("decorator"),
					this.decorator.serialize(dynamicOps).getValue()
				)
			)
		);
	}

	public String toString() {
		return String.format(
			"< %s [%s | %s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this.feature.feature), Registry.DECORATOR.getKey(this.decorator.decorator)
		);
	}

	public static <T> DecoratedFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		ConfiguredFeature<?> configuredFeature = ConfiguredFeature.deserialize(dynamic.get("feature").orElseEmptyMap());
		ConfiguredDecorator<?> configuredDecorator = ConfiguredDecorator.deserialize(dynamic.get("decorator").orElseEmptyMap());
		return new DecoratedFeatureConfiguration(configuredFeature, configuredDecorator);
	}
}
