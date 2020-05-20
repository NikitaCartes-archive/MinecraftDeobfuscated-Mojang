package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class ChanceRangeDecorator extends SimpleFeatureDecorator<ChanceRangeDecoratorConfiguration> {
	public ChanceRangeDecorator(Codec<ChanceRangeDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, ChanceRangeDecoratorConfiguration chanceRangeDecoratorConfiguration, BlockPos blockPos) {
		if (random.nextFloat() < chanceRangeDecoratorConfiguration.chance) {
			int i = random.nextInt(16) + blockPos.getX();
			int j = random.nextInt(16) + blockPos.getZ();
			int k = random.nextInt(chanceRangeDecoratorConfiguration.top - chanceRangeDecoratorConfiguration.topOffset) + chanceRangeDecoratorConfiguration.bottomOffset;
			return Stream.of(new BlockPos(i, k, j));
		} else {
			return Stream.empty();
		}
	}
}
