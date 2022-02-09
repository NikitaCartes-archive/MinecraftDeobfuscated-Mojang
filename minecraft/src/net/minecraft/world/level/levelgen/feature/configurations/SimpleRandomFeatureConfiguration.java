package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC)
		.fieldOf("features")
		.<SimpleRandomFeatureConfiguration>xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features)
		.codec();
	public final HolderSet<PlacedFeature> features;

	public SimpleRandomFeatureConfiguration(HolderSet<PlacedFeature> holderSet) {
		this.features = holderSet;
	}

	@Override
	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return this.features.stream().flatMap(holder -> ((PlacedFeature)holder.value()).getFeatures());
	}
}
