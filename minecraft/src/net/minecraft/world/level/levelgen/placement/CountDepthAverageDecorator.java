package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CountDepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
	public CountDepthAverageDecorator(Function<Dynamic<?>, ? extends DepthAverageConfigation> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, DepthAverageConfigation depthAverageConfigation, BlockPos blockPos) {
		int i = depthAverageConfigation.count;
		int j = depthAverageConfigation.baseline;
		int k = depthAverageConfigation.spread;
		return IntStream.range(0, i).mapToObj(kx -> {
			int l = random.nextInt(16);
			int m = random.nextInt(k) + random.nextInt(k) - k + j;
			int n = random.nextInt(16);
			return blockPos.offset(l, m, n);
		});
	}
}
