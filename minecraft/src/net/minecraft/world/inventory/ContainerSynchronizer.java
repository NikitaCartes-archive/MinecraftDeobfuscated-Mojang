package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ContainerSynchronizer {
	void sendInitialData(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList, ItemStack itemStack, int[] is);

	void sendSlotChange(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack);

	void sendCarriedChange(AbstractContainerMenu abstractContainerMenu, ItemStack itemStack);

	void sendDataChange(AbstractContainerMenu abstractContainerMenu, int i, int j);
}
