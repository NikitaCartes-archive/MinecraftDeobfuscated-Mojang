package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class CountDecorator extends SimpleFeatureDecorator<CountConfiguration> {
	public CountDecorator(Codec<CountConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
		return IntStream.range(0, countConfiguration.count().sample(random)).mapToObj(i -> blockPos);
	}
}
