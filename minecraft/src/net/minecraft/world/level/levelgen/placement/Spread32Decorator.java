package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class Spread32Decorator extends FeatureDecorator<NoneDecoratorConfiguration> {
	public Spread32Decorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos
	) {
		int i = random.nextInt(Math.max(blockPos.getY(), 0) + 32);
		return Stream.of(new BlockPos(blockPos.getX(), i, blockPos.getZ()));
	}
}
