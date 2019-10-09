package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EmeraldPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
	public EmeraldPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
		int i = 3 + random.nextInt(6);
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = random.nextInt(28) + 4;
			return new BlockPos(j, l, k);
		});
	}
}
