package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("min").forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.min),
					Codec.INT.fieldOf("max").forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.max)
				)
				.apply(instance, RangeDecoratorConfiguration::new)
	);
	public final int min;
	public final int max;

	public RangeDecoratorConfiguration(int i, int j) {
		this.min = i;
		this.max = j;
	}
}
