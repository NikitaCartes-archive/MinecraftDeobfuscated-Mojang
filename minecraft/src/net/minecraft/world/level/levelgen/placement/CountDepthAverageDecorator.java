package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountDepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
	public CountDepthAverageDecorator(
		Function<Dynamic<?>, ? extends DepthAverageConfigation> function, Function<Random, ? extends DepthAverageConfigation> function2
	) {
		super(function, function2);
	}

	public Stream<BlockPos> place(Random random, DepthAverageConfigation depthAverageConfigation, BlockPos blockPos) {
		int i = depthAverageConfigation.count;
		int j = depthAverageConfigation.baseline;
		int k = depthAverageConfigation.spread;
		return IntStream.range(0, i).mapToObj(kx -> {
			int l = random.nextInt(16) + blockPos.getX();
			int m = random.nextInt(16) + blockPos.getZ();
			int n = random.nextInt(k) + random.nextInt(k) - k + j;
			return new BlockPos(l, n, m);
		});
	}
}
