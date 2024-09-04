package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu extends AbstractContainerMenu {
	private final Container horseContainer;
	private final Container armorContainer;
	private final AbstractHorse horse;
	private static final int SLOT_BODY_ARMOR = 1;
	private static final int SLOT_HORSE_INVENTORY_START = 2;

	public HorseInventoryMenu(int i, Inventory inventory, Container container, AbstractHorse abstractHorse, int j) {
		super(null, i);
		this.horseContainer = container;
		this.armorContainer = abstractHorse.getBodyArmorAccess();
		this.horse = abstractHorse;
		container.startOpen(inventory.player);
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
		this.addSlot(new ArmorSlot(this.armorContainer, abstractHorse, EquipmentSlot.BODY, 0, 8, 36, null) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return abstractHorse.isEquippableInSlot(itemStack, EquipmentSlot.BODY);
			}

			@Override
			public boolean isActive() {
				return abstractHorse.canUseSlot(EquipmentSlot.BODY);
			}
		});
		if (j > 0) {
			for (int k = 0; k < 3; k++) {
				for (int l = 0; l < j; l++) {
					this.addSlot(new Slot(container, 1 + l + k * j, 80 + l * 18, 18 + k * 18));
				}
			}
		}

		this.addStandardInventorySlots(inventory, 8, 84);
	}

	@Override
	public boolean stillValid(Player player) {
		return !this.horse.hasInventoryChanged(this.horseContainer)
			&& this.horseContainer.stillValid(player)
			&& this.armorContainer.stillValid(player)
			&& this.horse.isAlive()
			&& player.canInteractWithEntity(this.horse, 4.0);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			int j = this.horseContainer.getContainerSize() + 1;
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
			} else if (j <= 1 || !this.moveItemStackTo(itemStack2, 2, j, false)) {
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
		this.horseContainer.stopOpen(player);
	}
}
