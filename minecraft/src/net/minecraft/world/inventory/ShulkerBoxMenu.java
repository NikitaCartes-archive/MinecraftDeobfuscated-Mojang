package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxMenu extends AbstractContainerMenu {
	private final Container container;

	public ShulkerBoxMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(27));
	}

	public ShulkerBoxMenu(int i, Inventory inventory, Container container) {
		super(MenuType.SHULKER_BOX, i);
		checkContainerSize(container, 27);
		this.container = container;
		container.startOpen(inventory.player);
		int j = 3;
		int k = 9;

		for (int l = 0; l < 3; l++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new ShulkerBoxSlot(container, m + l * 9, 8 + m * 18, 18 + l * 18));
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new Slot(inventory, m + l * 9 + 9, 8 + m * 18, 84 + l * 18));
			}
		}

		for (int l = 0; l < 9; l++) {
			this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < this.container.getContainerSize()) {
				if (!this.moveItemStackTo(itemStack2, this.container.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, this.container.getContainerSize(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}
}
