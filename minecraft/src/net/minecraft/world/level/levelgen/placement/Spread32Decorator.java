package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class Spread32Decorator extends VerticalDecorator<NoneDecoratorConfiguration> {
	public Spread32Decorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int y(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, int i) {
		return random.nextInt(Math.max(i, 0) + 32);
	}
}
