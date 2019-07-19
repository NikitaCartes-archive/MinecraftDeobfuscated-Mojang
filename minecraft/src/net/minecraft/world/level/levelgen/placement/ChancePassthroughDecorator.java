package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class ChancePassthroughDecorator extends SimpleFeatureDecorator<DecoratorChance> {
	public ChancePassthroughDecorator(Function<Dynamic<?>, ? extends DecoratorChance> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DecoratorChance decoratorChance, BlockPos blockPos) {
		return random.nextFloat() < 1.0F / (float)decoratorChance.chance ? Stream.of(blockPos) : Stream.empty();
	}
}
