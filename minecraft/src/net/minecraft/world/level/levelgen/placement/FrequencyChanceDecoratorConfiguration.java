package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyChanceDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<FrequencyChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("count").forGetter(frequencyChanceDecoratorConfiguration -> frequencyChanceDecoratorConfiguration.count),
					Codec.FLOAT.fieldOf("chance").forGetter(frequencyChanceDecoratorConfiguration -> frequencyChanceDecoratorConfiguration.chance)
				)
				.apply(instance, FrequencyChanceDecoratorConfiguration::new)
	);
	public final int count;
	public final float chance;

	public FrequencyChanceDecoratorConfiguration(int i, float f) {
		this.count = i;
		this.chance = f;
	}
}
