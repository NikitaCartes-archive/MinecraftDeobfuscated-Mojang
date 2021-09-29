package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class BlockFilterDecorator extends FilterDecorator<BlockFilterConfiguration> {
	public BlockFilterDecorator(Codec<BlockFilterConfiguration> codec) {
		super(codec);
	}

	protected boolean shouldPlace(DecorationContext decorationContext, Random random, BlockFilterConfiguration blockFilterConfiguration, BlockPos blockPos) {
		return blockFilterConfiguration.predicate().test(decorationContext.getLevel(), blockPos);
	}
}
