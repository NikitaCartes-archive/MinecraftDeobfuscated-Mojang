package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceFuelSlot extends Slot {
	private final AbstractFurnaceMenu menu;

	public FurnaceFuelSlot(AbstractFurnaceMenu abstractFurnaceMenu, Container container, int i, int j, int k) {
		super(container, i, j, k);
		this.menu = abstractFurnaceMenu;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return this.menu.isFuel(itemStack) || isBucket(itemStack);
	}

	@Override
	public int getMaxStackSize(ItemStack itemStack) {
		return isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
	}

	public static boolean isBucket(ItemStack itemStack) {
		return itemStack.is(Items.BUCKET);
	}
}
