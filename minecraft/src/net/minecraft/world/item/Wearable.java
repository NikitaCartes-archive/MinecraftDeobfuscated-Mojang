package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Wearable extends Vanishable {
	default InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
		ItemStack itemStack2 = player.getItemBySlot(equipmentSlot);
		if (ItemStack.matches(itemStack, itemStack2)) {
			return InteractionResultHolder.fail(itemStack);
		} else {
			player.setItemSlot(equipmentSlot, itemStack.copy());
			if (!level.isClientSide()) {
				player.awardStat(Stats.ITEM_USED.get(item));
			}

			if (itemStack2.isEmpty()) {
				itemStack.setCount(0);
			} else {
				player.setItemInHand(interactionHand, itemStack2.copy());
			}

			return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
		}
	}
}
