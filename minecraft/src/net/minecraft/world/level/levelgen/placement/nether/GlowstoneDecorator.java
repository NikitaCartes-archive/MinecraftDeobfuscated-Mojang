package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class GlowstoneDecorator extends RepeatingDecorator<CountConfiguration> {
	public GlowstoneDecorator(Codec<CountConfiguration> codec) {
		super(codec);
	}

	protected int count(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
		return random.nextInt(random.nextInt(countConfiguration.count().sample(random)) + 1);
	}
}
