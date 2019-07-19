package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemFrameItem extends HangingEntityItem {
	public ItemFrameItem(Item.Properties properties) {
		super(EntityType.ITEM_FRAME, properties);
	}

	@Override
	protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
		return !Level.isOutsideBuildHeight(blockPos) && player.mayUseItemAt(blockPos, direction, itemStack);
	}
}
