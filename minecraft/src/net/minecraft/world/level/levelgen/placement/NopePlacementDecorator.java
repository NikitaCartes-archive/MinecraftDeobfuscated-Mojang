package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class NopePlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
	public NopePlacementDecorator(
		Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function, Function<Random, ? extends NoneDecoratorConfiguration> function2
	) {
		super(function, function2);
	}

	public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
		return Stream.of(blockPos);
	}
}
