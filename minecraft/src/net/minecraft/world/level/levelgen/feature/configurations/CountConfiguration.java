package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.util.UniformInt;

public class CountConfiguration implements DecoratorConfiguration, FeatureConfiguration {
	public static final Codec<CountConfiguration> CODEC = UniformInt.codec(-10, 128, 128)
		.fieldOf("count")
		.<CountConfiguration>xmap(CountConfiguration::new, CountConfiguration::count)
		.codec();
	private final UniformInt count;

	public CountConfiguration(int i) {
		this.count = UniformInt.fixed(i);
	}

	public CountConfiguration(UniformInt uniformInt) {
		this.count = uniformInt;
	}

	public UniformInt count() {
		return this.count;
	}
}
