package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class BaseHeightmapDecorator<DC extends DecoratorConfiguration> extends EdgeDecorator<DC> {
	public BaseHeightmapDecorator(Codec<DC> codec) {
		super(codec);
	}

	@Override
	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = decorationContext.getHeight(this.type(decoratorConfiguration), i, j);
		return k > decorationContext.getMinBuildHeight() ? Stream.of(new BlockPos(i, k, j)) : Stream.of();
	}
}
