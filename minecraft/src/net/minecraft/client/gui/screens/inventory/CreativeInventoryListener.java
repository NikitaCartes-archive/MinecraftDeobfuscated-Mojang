package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class CreativeInventoryListener implements ContainerListener {
	private final Minecraft minecraft;

	public CreativeInventoryListener(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList) {
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
		this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack, i);
	}

	@Override
	public void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}
}
