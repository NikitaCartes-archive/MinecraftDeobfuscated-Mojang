package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record TwistingVinesConfig() implements FeatureConfiguration {
	private final int spreadWidth;
	private final int spreadHeight;
	private final int maxHeight;
	public static final Codec<TwistingVinesConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.POSITIVE_INT.fieldOf("spread_width").forGetter(TwistingVinesConfig::spreadWidth),
					ExtraCodecs.POSITIVE_INT.fieldOf("spread_height").forGetter(TwistingVinesConfig::spreadHeight),
					ExtraCodecs.POSITIVE_INT.fieldOf("max_height").forGetter(TwistingVinesConfig::maxHeight)
				)
				.apply(instance, TwistingVinesConfig::new)
	);

	public TwistingVinesConfig(int i, int j, int k) {
		this.spreadWidth = i;
		this.spreadHeight = j;
		this.maxHeight = k;
	}
}
