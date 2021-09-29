package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class FilterDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
	public FilterDecorator(Codec<DC> codec) {
		super(codec);
	}

	@Override
	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
		return this.shouldPlace(decorationContext, random, decoratorConfiguration, blockPos) ? Stream.of(blockPos) : Stream.of();
	}

	protected abstract boolean shouldPlace(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos);
}
