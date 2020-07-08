package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends SimpleFeatureDecorator<RangeDecoratorConfiguration> {
	public RangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = random.nextInt(rangeDecoratorConfiguration.maximum - rangeDecoratorConfiguration.topOffset) + rangeDecoratorConfiguration.bottomOffset;
		return Stream.of(new BlockPos(i, k, j));
	}
}
