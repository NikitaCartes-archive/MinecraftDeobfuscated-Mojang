package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class DepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
	public DepthAverageDecorator(Codec<DepthAverageConfigation> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, DepthAverageConfigation depthAverageConfigation, BlockPos blockPos) {
		int i = depthAverageConfigation.baseline;
		int j = depthAverageConfigation.spread;
		int k = blockPos.getX();
		int l = blockPos.getZ();
		int m = random.nextInt(j) + random.nextInt(j) - j + i;
		return Stream.of(new BlockPos(k, m, l));
	}
}
