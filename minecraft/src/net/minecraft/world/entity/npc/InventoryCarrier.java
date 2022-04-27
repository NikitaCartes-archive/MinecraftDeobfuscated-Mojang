package net.minecraft.world.entity.npc;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface InventoryCarrier {
	SimpleContainer getInventory();

	static void pickUpItem(Mob mob, InventoryCarrier inventoryCarrier, ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		if (mob.wantsToPickUp(itemStack)) {
			SimpleContainer simpleContainer = inventoryCarrier.getInventory();
			boolean bl = simpleContainer.canAddItem(itemStack);
			if (!bl) {
				return;
			}

			mob.onItemPickup(itemEntity);
			int i = itemStack.getCount();
			ItemStack itemStack2 = simpleContainer.addItem(itemStack);
			mob.take(itemEntity, i - itemStack2.getCount());
			if (itemStack2.isEmpty()) {
				itemEntity.discard();
			} else {
				itemStack.setCount(itemStack2.getCount());
			}
		}
	}
}
