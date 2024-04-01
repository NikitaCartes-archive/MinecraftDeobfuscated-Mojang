package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.state),
					IntProvider.codec(0, 3).fieldOf("reach").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.reach),
					IntProvider.codec(1, 20).fieldOf("height").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.height)
				)
				.apply(instance, ColumnFeatureConfiguration::new)
	);
	private final IntProvider reach;
	private final IntProvider height;
	private final BlockState state;

	public ColumnFeatureConfiguration(BlockState blockState, IntProvider intProvider, IntProvider intProvider2) {
		this.reach = intProvider;
		this.height = intProvider2;
		this.state = blockState;
	}

	public IntProvider reach() {
		return this.reach;
	}

	public IntProvider height() {
		return this.height;
	}

	public BlockState state() {
		return this.state;
	}
}
