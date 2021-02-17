package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;

public class DepthAverageDecorator extends VerticalDecorator<DepthAverageConfiguration> {
	public DepthAverageDecorator(Codec<DepthAverageConfiguration> codec) {
		super(codec);
	}

	protected int y(DecorationContext decorationContext, Random random, DepthAverageConfiguration depthAverageConfiguration, int i) {
		int j = depthAverageConfiguration.spread();
		return random.nextInt(j) + random.nextInt(j) - j + depthAverageConfiguration.baseline().resolveY(decorationContext);
	}
}
