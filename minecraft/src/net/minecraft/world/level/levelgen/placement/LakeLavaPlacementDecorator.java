package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class LakeLavaPlacementDecorator extends FilterDecorator<ChanceDecoratorConfiguration> {
	public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	protected boolean shouldPlace(DecorationContext decorationContext, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
		return blockPos.getY() < 63 || random.nextInt(10) == 0;
	}
}
