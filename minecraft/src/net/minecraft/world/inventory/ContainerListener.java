package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ContainerListener {
	void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList);

	void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack);

	void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j);
}
