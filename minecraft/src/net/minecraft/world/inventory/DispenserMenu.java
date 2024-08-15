package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DispenserMenu extends AbstractContainerMenu {
	private static final int SLOT_COUNT = 9;
	private static final int INV_SLOT_START = 9;
	private static final int INV_SLOT_END = 36;
	private static final int USE_ROW_SLOT_START = 36;
	private static final int USE_ROW_SLOT_END = 45;
	private final Container dispenser;

	public DispenserMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(9));
	}

	public DispenserMenu(int i, Inventory inventory, Container container) {
		super(MenuType.GENERIC_3x3, i);
		checkContainerSize(container, 9);
		this.dispenser = container;
		container.startOpen(inventory.player);
		this.add3x3GridSlots(container, 62, 17);
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	protected void add3x3GridSlots(Container container, int i, int j) {
		for (int k = 0; k < 3; k++) {
			for (int l = 0; l < 3; l++) {
				int m = l + k * 3;
				this.addSlot(new Slot(container, m, i + l * 18, j + k * 18));
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.dispenser.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < 9) {
				if (!this.moveItemStackTo(itemStack2, 9, 45, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.dispenser.stopOpen(player);
	}
}
