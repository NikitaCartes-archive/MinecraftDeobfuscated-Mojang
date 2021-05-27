package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeDecoratorConfiguration implements DecoratorConfiguration, FeatureConfiguration {
	public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(HeightProvider.CODEC.fieldOf("height").forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.height))
				.apply(instance, RangeDecoratorConfiguration::new)
	);
	public final HeightProvider height;

	public RangeDecoratorConfiguration(HeightProvider heightProvider) {
		this.height = heightProvider;
	}
}
