package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.DecoratorChanceRange;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class ChanceRangeDecorator extends SimpleFeatureDecorator<DecoratorChanceRange> {
	public ChanceRangeDecorator(Function<Dynamic<?>, ? extends DecoratorChanceRange> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DecoratorChanceRange decoratorChanceRange, BlockPos blockPos) {
		if (random.nextFloat() < decoratorChanceRange.chance) {
			int i = random.nextInt(16);
			int j = random.nextInt(decoratorChanceRange.top - decoratorChanceRange.topOffset) + decoratorChanceRange.bottomOffset;
			int k = random.nextInt(16);
			return Stream.of(blockPos.offset(i, j, k));
		} else {
			return Stream.empty();
		}
	}
}
