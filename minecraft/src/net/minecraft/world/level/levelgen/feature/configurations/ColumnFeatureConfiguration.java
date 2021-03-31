package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					IntProvider.codec(0, 3).fieldOf("reach").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.reach),
					IntProvider.codec(1, 10).fieldOf("height").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.height)
				)
				.apply(instance, ColumnFeatureConfiguration::new)
	);
	private final IntProvider reach;
	private final IntProvider height;

	public ColumnFeatureConfiguration(IntProvider intProvider, IntProvider intProvider2) {
		this.reach = intProvider;
		this.height = intProvider2;
	}

	public IntProvider reach() {
		return this.reach;
	}

	public IntProvider height() {
		return this.height;
	}
}
