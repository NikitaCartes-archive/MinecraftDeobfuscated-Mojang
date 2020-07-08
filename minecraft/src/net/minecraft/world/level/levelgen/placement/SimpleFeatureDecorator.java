package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class SimpleFeatureDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
	public SimpleFeatureDecorator(Codec<DC> codec) {
		super(codec);
	}

	@Override
	public final Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
		return this.place(random, decoratorConfiguration, blockPos);
	}

	protected abstract Stream<BlockPos> place(Random random, DC decoratorConfiguration, BlockPos blockPos);
}
