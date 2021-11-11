package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyList(PlacedFeature.LIST_CODEC)
		.fieldOf("features")
		.<SimpleRandomFeatureConfiguration>xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features)
		.codec();
	public final List<Supplier<PlacedFeature>> features;

	public SimpleRandomFeatureConfiguration(List<Supplier<PlacedFeature>> list) {
		this.features = list;
	}

	@Override
	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return this.features.stream().flatMap(supplier -> ((PlacedFeature)supplier.get()).getFeatures());
	}
}
