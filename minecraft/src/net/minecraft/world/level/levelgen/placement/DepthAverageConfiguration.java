package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DepthAverageConfiguration implements DecoratorConfiguration {
	public static final Codec<DepthAverageConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("baseline").forGetter(DepthAverageConfiguration::baseline),
					Codec.INT.fieldOf("spread").forGetter(DepthAverageConfiguration::spread)
				)
				.apply(instance, DepthAverageConfiguration::new)
	);
	private final VerticalAnchor baseline;
	private final int spread;

	public DepthAverageConfiguration(VerticalAnchor verticalAnchor, int i) {
		this.baseline = verticalAnchor;
		this.spread = i;
	}

	public VerticalAnchor baseline() {
		return this.baseline;
	}

	public int spread() {
		return this.spread;
	}
}
