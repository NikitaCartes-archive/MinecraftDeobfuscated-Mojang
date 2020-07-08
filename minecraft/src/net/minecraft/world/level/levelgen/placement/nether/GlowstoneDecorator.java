package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class GlowstoneDecorator extends SimpleFeatureDecorator<CountConfiguration> {
	public GlowstoneDecorator(Codec<CountConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
		return IntStream.range(0, random.nextInt(random.nextInt(countConfiguration.count().sample(random)) + 1)).mapToObj(i -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = random.nextInt(120) + 4;
			return new BlockPos(j, l, k);
		});
	}
}
