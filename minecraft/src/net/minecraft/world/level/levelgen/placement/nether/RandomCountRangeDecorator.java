package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class RandomCountRangeDecorator extends SimpleFeatureDecorator<DecoratorCountRange> {
	public RandomCountRangeDecorator(Function<Dynamic<?>, ? extends DecoratorCountRange> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DecoratorCountRange decoratorCountRange, BlockPos blockPos) {
		int i = random.nextInt(Math.max(decoratorCountRange.count, 1));
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16);
			int k = random.nextInt(decoratorCountRange.maximum - decoratorCountRange.topOffset) + decoratorCountRange.bottomOffset;
			int l = random.nextInt(16);
			return blockPos.offset(j, k, l);
		});
	}
}
