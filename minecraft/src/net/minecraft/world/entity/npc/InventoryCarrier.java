package net.minecraft.world.entity.npc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface InventoryCarrier {
	String TAG_INVENTORY = "Inventory";

	SimpleContainer getInventory();

	static void pickUpItem(ServerLevel serverLevel, Mob mob, InventoryCarrier inventoryCarrier, ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		if (mob.wantsToPickUp(serverLevel, itemStack)) {
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

	default void readInventoryFromTag(CompoundTag compoundTag, HolderLookup.Provider provider) {
		if (compoundTag.contains("Inventory", 9)) {
			this.getInventory().fromTag(compoundTag.getList("Inventory", 10), provider);
		}
	}

	default void writeInventoryToTag(CompoundTag compoundTag, HolderLookup.Provider provider) {
		compoundTag.put("Inventory", this.getInventory().createTag(provider));
	}
}
