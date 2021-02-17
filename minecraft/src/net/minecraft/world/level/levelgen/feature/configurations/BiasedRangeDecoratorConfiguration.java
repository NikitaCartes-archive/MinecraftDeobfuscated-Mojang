package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class BiasedRangeDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<BiasedRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(BiasedRangeDecoratorConfiguration::bottomInclusive),
					VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(BiasedRangeDecoratorConfiguration::topInclusive),
					Codec.INT.fieldOf("cutoff").forGetter(BiasedRangeDecoratorConfiguration::cutoff)
				)
				.apply(instance, BiasedRangeDecoratorConfiguration::new)
	);
	private final VerticalAnchor bottomInclusive;
	private final VerticalAnchor topInclusive;
	private final int cutoff;

	public BiasedRangeDecoratorConfiguration(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		this.bottomInclusive = verticalAnchor;
		this.cutoff = i;
		this.topInclusive = verticalAnchor2;
	}

	public VerticalAnchor bottomInclusive() {
		return this.bottomInclusive;
	}

	public int cutoff() {
		return this.cutoff;
	}

	public VerticalAnchor topInclusive() {
		return this.topInclusive;
	}
}
