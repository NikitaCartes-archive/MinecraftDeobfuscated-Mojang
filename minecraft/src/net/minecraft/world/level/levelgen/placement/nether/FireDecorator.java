package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class FireDecorator extends SimpleFeatureDecorator<CountConfiguration> {
	public FireDecorator(Codec<CountConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
		List<BlockPos> list = Lists.<BlockPos>newArrayList();

		for (int i = 0; i < random.nextInt(random.nextInt(countConfiguration.count().sample(random)) + 1) + 1; i++) {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = random.nextInt(120) + 4;
			list.add(new BlockPos(j, l, k));
		}

		return list.stream();
	}
}
