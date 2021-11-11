package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountConfiguration implements FeatureConfiguration {
	public static final Codec<CountConfiguration> CODEC = IntProvider.codec(0, 256)
		.fieldOf("count")
		.<CountConfiguration>xmap(CountConfiguration::new, CountConfiguration::count)
		.codec();
	private final IntProvider count;

	public CountConfiguration(int i) {
		this.count = ConstantInt.of(i);
	}

	public CountConfiguration(IntProvider intProvider) {
		this.count = intProvider;
	}

	public IntProvider count() {
		return this.count;
	}
}
