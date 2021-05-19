package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyList(ConfiguredFeature.LIST_CODEC)
		.fieldOf("features")
		.<SimpleRandomFeatureConfiguration>xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features)
		.codec();
	public final List<Supplier<ConfiguredFeature<?, ?>>> features;

	public SimpleRandomFeatureConfiguration(List<Supplier<ConfiguredFeature<?, ?>>> list) {
		this.features = list;
	}

	@Override
	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return this.features.stream().flatMap(supplier -> ((ConfiguredFeature)supplier.get()).getFeatures());
	}
}
