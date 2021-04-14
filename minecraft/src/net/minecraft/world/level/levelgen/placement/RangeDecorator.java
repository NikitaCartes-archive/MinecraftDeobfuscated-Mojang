package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends VerticalDecorator<RangeDecoratorConfiguration> {
	public RangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int y(DecorationContext decorationContext, Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, int i) {
		return rangeDecoratorConfiguration.height.sample(random, decorationContext);
	}
}
