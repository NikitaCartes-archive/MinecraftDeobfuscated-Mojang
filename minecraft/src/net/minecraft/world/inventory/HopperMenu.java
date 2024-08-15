package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HopperMenu extends AbstractContainerMenu {
	public static final int CONTAINER_SIZE = 5;
	private final Container hopper;

	public HopperMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(5));
	}

	public HopperMenu(int i, Inventory inventory, Container container) {
		super(MenuType.HOPPER, i);
		this.hopper = container;
		checkContainerSize(container, 5);
		container.startOpen(inventory.player);

		for (int j = 0; j < 5; j++) {
			this.addSlot(new Slot(container, j, 44 + j * 18, 20));
		}

		this.addStandardInventorySlots(inventory, 8, 51);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.hopper.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < this.hopper.getContainerSize()) {
				if (!this.moveItemStackTo(itemStack2, this.hopper.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, this.hopper.getContainerSize(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.hopper.stopOpen(player);
	}
}
