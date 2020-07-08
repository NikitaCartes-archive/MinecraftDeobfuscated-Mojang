package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ConfiguredFeature.CODEC
		.listOf()
		.fieldOf("features")
		.<SimpleRandomFeatureConfiguration>xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features)
		.codec();
	public final List<Supplier<ConfiguredFeature<?, ?>>> features;

	public SimpleRandomFeatureConfiguration(List<Supplier<ConfiguredFeature<?, ?>>> list) {
		this.features = list;
	}
}
