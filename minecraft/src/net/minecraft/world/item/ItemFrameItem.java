package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;

public class ItemFrameItem extends HangingEntityItem {
	public ItemFrameItem(EntityType<? extends HangingEntity> entityType, Item.Properties properties) {
		super(entityType, properties);
	}

	@Override
	protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
		return !player.level().isOutsideBuildHeight(blockPos) && player.mayUseItemAt(blockPos, direction, itemStack);
	}
}
