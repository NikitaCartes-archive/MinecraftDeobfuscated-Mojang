package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class HellFireDecorator extends SimpleFeatureDecorator<FrequencyDecoratorConfiguration> {
	public HellFireDecorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> place(Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos) {
		List<BlockPos> list = Lists.<BlockPos>newArrayList();

		for (int i = 0; i < random.nextInt(random.nextInt(frequencyDecoratorConfiguration.count) + 1) + 1; i++) {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = random.nextInt(120) + 4;
			list.add(new BlockPos(j, l, k));
		}

		return list.stream();
	}
}
