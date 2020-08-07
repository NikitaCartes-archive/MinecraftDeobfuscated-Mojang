package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class BiasedRangeDecorator extends SimpleFeatureDecorator<RangeDecoratorConfiguration> {
	public BiasedRangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = random.nextInt(random.nextInt(rangeDecoratorConfiguration.maximum - rangeDecoratorConfiguration.topOffset) + rangeDecoratorConfiguration.bottomOffset);
		return Stream.of(new BlockPos(i, k, j));
	}
}
