package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class CountRangeDecorator extends SimpleFeatureDecorator<CountRangeDecoratorConfiguration> {
	public CountRangeDecorator(
		Function<Dynamic<?>, ? extends CountRangeDecoratorConfiguration> function, Function<Random, ? extends CountRangeDecoratorConfiguration> function2
	) {
		super(function, function2);
	}

	public Stream<BlockPos> place(Random random, CountRangeDecoratorConfiguration countRangeDecoratorConfiguration, BlockPos blockPos) {
		return IntStream.range(0, countRangeDecoratorConfiguration.count)
			.mapToObj(
				i -> {
					int j = random.nextInt(16) + blockPos.getX();
					int k = random.nextInt(16) + blockPos.getZ();
					int l = random.nextInt(countRangeDecoratorConfiguration.maximum - countRangeDecoratorConfiguration.topOffset)
						+ countRangeDecoratorConfiguration.bottomOffset;
					return new BlockPos(j, l, k);
				}
			);
	}
}
