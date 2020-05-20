package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ChanceRangeDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<ChanceRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.FLOAT.fieldOf("chance").forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.chance),
					Codec.INT.fieldOf("bottom_offset").withDefault(0).forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.bottomOffset),
					Codec.INT.fieldOf("top_offset").withDefault(0).forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.topOffset),
					Codec.INT.fieldOf("top").withDefault(0).forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.top)
				)
				.apply(instance, ChanceRangeDecoratorConfiguration::new)
	);
	public final float chance;
	public final int bottomOffset;
	public final int topOffset;
	public final int top;

	public ChanceRangeDecoratorConfiguration(float f, int i, int j, int k) {
		this.chance = f;
		this.bottomOffset = i;
		this.topOffset = j;
		this.top = k;
	}
}
