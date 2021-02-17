package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class CountWithExtraChanceDecorator extends RepeatingDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
	public CountWithExtraChanceDecorator(Codec<FrequencyWithExtraChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int count(Random random, FrequencyWithExtraChanceDecoratorConfiguration frequencyWithExtraChanceDecoratorConfiguration, BlockPos blockPos) {
		return frequencyWithExtraChanceDecoratorConfiguration.count
			+ (random.nextFloat() < frequencyWithExtraChanceDecoratorConfiguration.extraChance ? frequencyWithExtraChanceDecoratorConfiguration.extraCount : 0);
	}
}
