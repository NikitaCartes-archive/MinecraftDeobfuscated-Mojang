package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CountRangeDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<CountRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("count").forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.count),
					Codec.INT.fieldOf("bottom_offset").withDefault(0).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.bottomOffset),
					Codec.INT.fieldOf("top_offset").withDefault(0).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.topOffset),
					Codec.INT.fieldOf("maximum").withDefault(0).forGetter(countRangeDecoratorConfiguration -> countRangeDecoratorConfiguration.maximum)
				)
				.apply(instance, CountRangeDecoratorConfiguration::new)
	);
	public final int count;
	public final int bottomOffset;
	public final int topOffset;
	public final int maximum;

	public CountRangeDecoratorConfiguration(int i, int j, int k, int l) {
		this.count = i;
		this.bottomOffset = j;
		this.topOffset = k;
		this.maximum = l;
	}
}
