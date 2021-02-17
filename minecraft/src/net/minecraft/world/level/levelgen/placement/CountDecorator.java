package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class CountDecorator extends RepeatingDecorator<CountConfiguration> {
	public CountDecorator(Codec<CountConfiguration> codec) {
		super(codec);
	}

	protected int count(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
		return countConfiguration.count().sample(random);
	}
}
