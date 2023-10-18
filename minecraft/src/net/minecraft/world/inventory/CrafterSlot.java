package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot extends Slot {
	private final CrafterMenu menu;

	public CrafterSlot(Container container, int i, int j, int k, CrafterMenu crafterMenu) {
		super(container, i, j, k);
		this.menu = crafterMenu;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return !this.menu.isSlotDisabled(this.index) && super.mayPlace(itemStack);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		this.menu.slotsChanged(this.container);
	}
}
