package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(RangeDecoratorConfiguration::bottomInclusive),
					VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(RangeDecoratorConfiguration::topInclusive)
				)
				.apply(instance, RangeDecoratorConfiguration::new)
	);
	private final VerticalAnchor bottomInclusive;
	private final VerticalAnchor topInclusive;

	public RangeDecoratorConfiguration(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		this.bottomInclusive = verticalAnchor;
		this.topInclusive = verticalAnchor2;
	}

	public VerticalAnchor bottomInclusive() {
		return this.bottomInclusive;
	}

	public VerticalAnchor topInclusive() {
		return this.topInclusive;
	}
}
