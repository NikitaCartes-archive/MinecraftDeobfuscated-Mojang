package net.minecraft.world.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class BundleTooltip implements TooltipComponent {
	private final NonNullList<ItemStack> items;
	private final boolean showEmptySlot;

	public BundleTooltip(NonNullList<ItemStack> nonNullList, boolean bl) {
		this.items = nonNullList;
		this.showEmptySlot = bl;
	}

	public NonNullList<ItemStack> getItems() {
		return this.items;
	}

	public boolean showEmptySlot() {
		return this.showEmptySlot;
	}
}
