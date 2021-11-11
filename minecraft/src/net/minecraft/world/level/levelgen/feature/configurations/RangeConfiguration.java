package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeConfiguration implements FeatureConfiguration {
	public static final Codec<RangeConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(HeightProvider.CODEC.fieldOf("height").forGetter(rangeConfiguration -> rangeConfiguration.height))
				.apply(instance, RangeConfiguration::new)
	);
	public final HeightProvider height;

	public RangeConfiguration(HeightProvider heightProvider) {
		this.height = heightProvider;
	}
}
