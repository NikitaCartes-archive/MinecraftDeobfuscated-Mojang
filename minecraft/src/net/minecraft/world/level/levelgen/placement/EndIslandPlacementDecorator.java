package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndIslandPlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
	public EndIslandPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
		Stream<BlockPos> stream = Stream.empty();
		if (random.nextInt(14) == 0) {
			stream = Stream.concat(stream, Stream.of(blockPos.offset(random.nextInt(16), 55 + random.nextInt(16), random.nextInt(16))));
			if (random.nextInt(4) == 0) {
				stream = Stream.concat(stream, Stream.of(blockPos.offset(random.nextInt(16), 55 + random.nextInt(16), random.nextInt(16))));
			}

			return stream;
		} else {
			return Stream.empty();
		}
	}
}
