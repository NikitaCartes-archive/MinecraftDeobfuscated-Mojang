package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class DecoratedDecorator extends FeatureDecorator<DecoratedDecoratorConfiguration> {
	public DecoratedDecorator(Codec<DecoratedDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, DecoratedDecoratorConfiguration decoratedDecoratorConfiguration, BlockPos blockPos
	) {
		return decoratedDecoratorConfiguration.outer()
			.getPositions(decorationContext, random, blockPos)
			.flatMap(blockPosx -> decoratedDecoratorConfiguration.inner().getPositions(decorationContext, random, blockPosx));
	}
}
