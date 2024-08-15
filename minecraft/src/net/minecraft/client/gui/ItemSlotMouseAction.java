package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public interface ItemSlotMouseAction {
	boolean matches(Slot slot);

	boolean onMouseScrolled(double d, double e, int i, ItemStack itemStack);

	void onStopHovering(Slot slot);

	boolean onKeyPressed(ItemStack itemStack, int i, int j, int k);
}
