package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ChestMenu extends AbstractContainerMenu {
	private static final int SLOTS_PER_ROW = 9;
	private final Container container;
	private final int containerRows;

	private ChestMenu(MenuType<?> menuType, int i, Inventory inventory, int j) {
		this(menuType, i, inventory, new SimpleContainer(9 * j), j);
	}

	public static ChestMenu oneRow(int i, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x1, i, inventory, 1);
	}

	public static ChestMenu twoRows(int i, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x2, i, inventory, 2);
	}

	public static ChestMenu threeRows(int i, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, 3);
	}

	public static ChestMenu fourRows(int i, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x4, i, inventory, 4);
	}

	public static ChestMenu fiveRows(int i, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x5, i, inventory, 5);
	}

	public static ChestMenu sixRows(int i, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, 6);
	}

	public static ChestMenu threeRows(int i, Inventory inventory, Container container) {
		return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, container, 3);
	}

	public static ChestMenu sixRows(int i, Inventory inventory, Container container) {
		return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, container, 6);
	}

	public ChestMenu(MenuType<?> menuType, int i, Inventory inventory, Container container, int j) {
		super(menuType, i);
		checkContainerSize(container, j * 9);
		this.container = container;
		this.containerRows = j;
		container.startOpen(inventory.player);
		int k = (this.containerRows - 4) * 18;

		for (int l = 0; l < this.containerRows; l++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new Slot(container, m + l * 9, 8 + m * 18, 18 + l * 18));
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new Slot(inventory, m + l * 9 + 9, 8 + m * 18, 103 + l * 18 + k));
			}
		}

		for (int l = 0; l < 9; l++) {
			this.addSlot(new Slot(inventory, l, 8 + l * 18, 161 + k));
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
			if (i < this.containerRows * 9) {
				if (!this.moveItemStackTo(itemStack2, this.containerRows * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, this.containerRows * 9, false)) {
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

	public Container getContainer() {
		return this.container;
	}

	public int getRowCount() {
		return this.containerRows;
	}
}
