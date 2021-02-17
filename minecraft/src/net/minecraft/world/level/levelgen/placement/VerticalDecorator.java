package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class VerticalDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
	public VerticalDecorator(Codec<DC> codec) {
		super(codec);
	}

	protected abstract int y(DecorationContext decorationContext, Random random, DC decoratorConfiguration, int i);

	@Override
	public final Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
		return Stream.of(new BlockPos(blockPos.getX(), this.y(decorationContext, random, decoratorConfiguration, blockPos.getY()), blockPos.getZ()));
	}
}
