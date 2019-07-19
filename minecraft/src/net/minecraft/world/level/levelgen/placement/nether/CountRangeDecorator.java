package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class CountRangeDecorator extends SimpleFeatureDecorator<DecoratorCountRange> {
	public CountRangeDecorator(Function<Dynamic<?>, ? extends DecoratorCountRange> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DecoratorCountRange decoratorCountRange, BlockPos blockPos) {
		return IntStream.range(0, decoratorCountRange.count).mapToObj(i -> {
			int j = random.nextInt(16);
			int k = random.nextInt(decoratorCountRange.maximum - decoratorCountRange.topOffset) + decoratorCountRange.bottomOffset;
			int l = random.nextInt(16);
			return blockPos.offset(j, k, l);
		});
	}
}
