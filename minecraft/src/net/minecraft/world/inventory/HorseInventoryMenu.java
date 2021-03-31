package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu extends AbstractContainerMenu {
	private final Container horseContainer;
	private final AbstractHorse horse;

	public HorseInventoryMenu(int i, Inventory inventory, Container container, AbstractHorse abstractHorse) {
		super(null, i);
		this.horseContainer = container;
		this.horse = abstractHorse;
		int j = 3;
		container.startOpen(inventory.player);
		int k = -18;
		this.addSlot(new Slot(container, 0, 8, 18) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return itemStack.is(Items.SADDLE) && !this.hasItem() && abstractHorse.isSaddleable();
			}

			@Override
			public boolean isActive() {
				return abstractHorse.isSaddleable();
			}
		});
		this.addSlot(new Slot(container, 1, 8, 36) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return abstractHorse.isArmor(itemStack);
			}

			@Override
			public boolean isActive() {
				return abstractHorse.canWearArmor();
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		if (this.hasChest(abstractHorse)) {
			for (int l = 0; l < 3; l++) {
				for (int m = 0; m < ((AbstractChestedHorse)abstractHorse).getInventoryColumns(); m++) {
					this.addSlot(new Slot(container, 2 + m + l * ((AbstractChestedHorse)abstractHorse).getInventoryColumns(), 80 + m * 18, 18 + l * 18));
				}
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new Slot(inventory, m + l * 9 + 9, 8 + m * 18, 102 + l * 18 + -18));
			}
		}

		for (int l = 0; l < 9; l++) {
			this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return !this.horse.hasInventoryChanged(this.horseContainer)
			&& this.horseContainer.stillValid(player)
			&& this.horse.isAlive()
			&& this.horse.distanceTo(player) < 8.0F;
	}

	private boolean hasChest(AbstractHorse abstractHorse) {
		return abstractHorse instanceof AbstractChestedHorse && ((AbstractChestedHorse)abstractHorse).hasChest();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			int j = this.horseContainer.getContainerSize();
			if (i < j) {
				if (!this.moveItemStackTo(itemStack2, j, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(1).mayPlace(itemStack2) && !this.getSlot(1).hasItem()) {
				if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(0).mayPlace(itemStack2)) {
				if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (j <= 2 || !this.moveItemStackTo(itemStack2, 2, j, false)) {
				int l = j + 27;
				int n = l + 9;
				if (i >= l && i < n) {
					if (!this.moveItemStackTo(itemStack2, j, l, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= j && i < l) {
					if (!this.moveItemStackTo(itemStack2, l, n, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemStack2, l, l, false)) {
					return ItemStack.EMPTY;
				}

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
		this.horseContainer.stopOpen(player);
	}
}
