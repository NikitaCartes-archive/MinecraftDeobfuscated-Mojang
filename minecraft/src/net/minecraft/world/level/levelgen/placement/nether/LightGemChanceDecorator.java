package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class LightGemChanceDecorator extends SimpleFeatureDecorator<FrequencyDecoratorConfiguration> {
	public LightGemChanceDecorator(
		Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> function, Function<Random, ? extends FrequencyDecoratorConfiguration> function2
	) {
		super(function, function2);
	}

	public Stream<BlockPos> place(Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos) {
		return IntStream.range(0, random.nextInt(random.nextInt(frequencyDecoratorConfiguration.count) + 1)).mapToObj(i -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = random.nextInt(120) + 4;
			return new BlockPos(j, l, k);
		});
	}
}
