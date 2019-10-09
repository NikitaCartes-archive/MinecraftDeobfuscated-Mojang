package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class ChancePassthroughDecorator extends SimpleFeatureDecorator<ChanceDecoratorConfiguration> {
	public ChancePassthroughDecorator(Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
		return random.nextFloat() < 1.0F / (float)chanceDecoratorConfiguration.chance ? Stream.of(blockPos) : Stream.empty();
	}
}
