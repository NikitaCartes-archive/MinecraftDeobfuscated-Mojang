package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class LightGemChanceDecorator extends SimpleFeatureDecorator<DecoratorFrequency> {
	public LightGemChanceDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DecoratorFrequency decoratorFrequency, BlockPos blockPos) {
		return IntStream.range(0, random.nextInt(random.nextInt(decoratorFrequency.count) + 1)).mapToObj(i -> {
			int j = random.nextInt(16);
			int k = random.nextInt(120) + 4;
			int l = random.nextInt(16);
			return blockPos.offset(j, k, l);
		});
	}
}
