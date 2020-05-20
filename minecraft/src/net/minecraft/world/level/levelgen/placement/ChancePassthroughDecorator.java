package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class ChancePassthroughDecorator extends SimpleFeatureDecorator<ChanceDecoratorConfiguration> {
	public ChancePassthroughDecorator(Codec<ChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
		return random.nextFloat() < 1.0F / (float)chanceDecoratorConfiguration.chance ? Stream.of(blockPos) : Stream.empty();
	}
}
