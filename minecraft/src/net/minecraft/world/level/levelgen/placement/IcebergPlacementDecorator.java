package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class IcebergPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
	public IcebergPlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
		int i = random.nextInt(8) + 4 + blockPos.getX();
		int j = random.nextInt(8) + 4 + blockPos.getZ();
		return Stream.of(new BlockPos(i, blockPos.getY(), j));
	}
}
