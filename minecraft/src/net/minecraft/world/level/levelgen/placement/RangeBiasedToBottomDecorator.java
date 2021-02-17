package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;

public class RangeBiasedToBottomDecorator extends AbstractBiasedRangeDecorator {
	public RangeBiasedToBottomDecorator(Codec<BiasedRangeDecoratorConfiguration> codec) {
		super(codec);
	}

	@Override
	protected int y(Random random, int i, int j, int k) {
		int l = Mth.nextInt(random, i + k, j);
		return Mth.nextInt(random, i, l - 1);
	}
}
