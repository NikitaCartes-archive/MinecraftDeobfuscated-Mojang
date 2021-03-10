package net.minecraft.world.inventory;

import net.minecraft.world.item.ItemStack;

public interface ContainerListener {
	void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack);

	void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j);
}
