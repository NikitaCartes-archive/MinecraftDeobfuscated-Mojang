package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

public class PickaxeBlock extends FacingTriggerableBlock {
	protected PickaxeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected TickPriority getTickPriority() {
		return TickPriority.EXTREMELY_HIGH;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (Rules.PICKAXE_BLOCK.get()) {
			BlockPos blockPos2 = blockPos.relative(blockState.getValue(FACING));
			if (!serverLevel.getBlockState(blockPos2).isAir()) {
				serverLevel.destroyBlock(blockPos2, true);
			}
		}
	}
}
