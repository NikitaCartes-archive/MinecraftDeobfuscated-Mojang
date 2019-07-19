package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GameMasterBlockItem extends BlockItem {
	public GameMasterBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Nullable
	@Override
	protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
		Player player = blockPlaceContext.getPlayer();
		return player != null && !player.canUseGameMasterBlocks() ? null : super.getPlacementState(blockPlaceContext);
	}
}
