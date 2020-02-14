package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GoldBlock extends Block {
	public GoldBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		super.playerWillDestroy(level, blockPos, blockState, player);
		PiglinAi.angerNearbyPiglinsThatSee(player);
	}
}
