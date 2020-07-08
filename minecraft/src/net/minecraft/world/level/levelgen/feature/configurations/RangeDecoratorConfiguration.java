package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("bottom_offset").orElse(0).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.bottomOffset),
					Codec.INT.fieldOf("top_offset").orElse(0).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.topOffset),
					Codec.INT.fieldOf("maximum").orElse(0).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.maximum)
				)
				.apply(instance, RangeDecoratorConfiguration::new)
	);
	public final int bottomOffset;
	public final int topOffset;
	public final int maximum;

	public RangeDecoratorConfiguration(int i, int j, int k) {
		this.bottomOffset = i;
		this.topOffset = j;
		this.maximum = k;
	}
}
