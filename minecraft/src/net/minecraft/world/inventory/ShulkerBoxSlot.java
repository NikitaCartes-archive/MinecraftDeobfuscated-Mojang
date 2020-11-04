package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxSlot extends Slot {
	public ShulkerBoxSlot(Container container, int i, int j, int k) {
		super(container, i, j, k);
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return itemStack.getItem().canFitInsideContainerItems();
	}
}
