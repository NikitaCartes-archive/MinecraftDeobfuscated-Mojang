package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends AbstractRangeDecorator {
	public RangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
		super(codec);
	}

	@Override
	protected int y(Random random, int i, int j) {
		return Mth.nextInt(random, i, j);
	}
}
