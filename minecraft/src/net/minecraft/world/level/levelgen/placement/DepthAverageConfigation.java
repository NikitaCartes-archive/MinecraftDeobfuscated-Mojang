package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DepthAverageConfigation implements DecoratorConfiguration {
	public static final Codec<DepthAverageConfigation> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("count").forGetter(depthAverageConfigation -> depthAverageConfigation.count),
					Codec.INT.fieldOf("baseline").forGetter(depthAverageConfigation -> depthAverageConfigation.baseline),
					Codec.INT.fieldOf("spread").forGetter(depthAverageConfigation -> depthAverageConfigation.spread)
				)
				.apply(instance, DepthAverageConfigation::new)
	);
	public final int count;
	public final int baseline;
	public final int spread;

	public DepthAverageConfigation(int i, int j, int k) {
		this.count = i;
		this.baseline = j;
		this.spread = k;
	}
}
