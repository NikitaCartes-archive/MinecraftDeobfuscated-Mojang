package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;

public class CountBiasedRangeDecorator extends SimpleFeatureDecorator<DecoratorCountRange> {
	public CountBiasedRangeDecorator(Function<Dynamic<?>, ? extends DecoratorCountRange> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DecoratorCountRange decoratorCountRange, BlockPos blockPos) {
		return IntStream.range(0, decoratorCountRange.count).mapToObj(i -> {
			int j = random.nextInt(16);
			int k = random.nextInt(random.nextInt(decoratorCountRange.maximum - decoratorCountRange.topOffset) + decoratorCountRange.bottomOffset);
			int l = random.nextInt(16);
			return blockPos.offset(j, k, l);
		});
	}
}
