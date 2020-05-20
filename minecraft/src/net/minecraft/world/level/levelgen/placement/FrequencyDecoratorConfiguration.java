package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<FrequencyDecoratorConfiguration> CODEC = Codec.INT
		.fieldOf("count")
		.<FrequencyDecoratorConfiguration>xmap(FrequencyDecoratorConfiguration::new, frequencyDecoratorConfiguration -> frequencyDecoratorConfiguration.count)
		.codec();
	public final int count;

	public FrequencyDecoratorConfiguration(int i) {
		this.count = i;
	}
}
