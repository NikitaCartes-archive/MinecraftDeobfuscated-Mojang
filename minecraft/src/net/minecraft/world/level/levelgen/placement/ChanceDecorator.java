package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class ChanceDecorator extends RepeatingDecorator<ChanceDecoratorConfiguration> {
	public ChanceDecorator(Codec<ChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int count(Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
		return random.nextFloat() < 1.0F / (float)chanceDecoratorConfiguration.chance ? 1 : 0;
	}
}
