package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FletchingTableBlock extends CraftingTableBlock {
	protected FletchingTableBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		return false;
	}
}
