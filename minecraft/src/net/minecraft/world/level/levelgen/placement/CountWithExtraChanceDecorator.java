package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountWithExtraChanceDecorator extends SimpleFeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
	public CountWithExtraChanceDecorator(Codec<FrequencyWithExtraChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, FrequencyWithExtraChanceDecoratorConfiguration frequencyWithExtraChanceDecoratorConfiguration, BlockPos blockPos) {
		int i = frequencyWithExtraChanceDecoratorConfiguration.count
			+ (random.nextFloat() < frequencyWithExtraChanceDecoratorConfiguration.extraChance ? frequencyWithExtraChanceDecoratorConfiguration.extraCount : 0);
		return IntStream.range(0, i).mapToObj(ix -> blockPos);
	}
}
