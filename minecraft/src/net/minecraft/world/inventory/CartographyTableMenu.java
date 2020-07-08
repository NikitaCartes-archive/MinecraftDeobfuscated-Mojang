package net.minecraft.world.inventory;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu extends AbstractContainerMenu {
	private final ContainerLevelAccess access;
	private long lastSoundTime;
	public final Container container = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			CartographyTableMenu.this.slotsChanged(this);
			super.setChanged();
		}
	};
	private final ResultContainer resultContainer = new ResultContainer() {
		@Override
		public void setChanged() {
			CartographyTableMenu.this.slotsChanged(this);
			super.setChanged();
		}
	};

	public CartographyTableMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public CartographyTableMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.CARTOGRAPHY_TABLE, i);
		this.access = containerLevelAccess;
		this.addSlot(new Slot(this.container, 0, 15, 15) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return itemStack.getItem() == Items.FILLED_MAP;
			}
		});
		this.addSlot(new Slot(this.container, 1, 15, 52) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				Item item = itemStack.getItem();
				return item == Items.PAPER || item == Items.MAP || item == Items.GLASS_PANE;
			}
		});
		this.addSlot(new Slot(this.resultContainer, 2, 145, 39) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}

			@Override
			public ItemStack onTake(Player player, ItemStack itemStack) {
				((Slot)CartographyTableMenu.this.slots.get(0)).remove(1);
				((Slot)CartographyTableMenu.this.slots.get(1)).remove(1);
				itemStack.getItem().onCraftedBy(itemStack, player.level, player);
				containerLevelAccess.execute((level, blockPos) -> {
					long l = level.getGameTime();
					if (CartographyTableMenu.this.lastSoundTime != l) {
						level.playSound(null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
						CartographyTableMenu.this.lastSoundTime = l;
					}
				});
				return super.onTake(player, itemStack);
			}
		});

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, Blocks.CARTOGRAPHY_TABLE);
	}

	@Override
	public void slotsChanged(Container container) {
		ItemStack itemStack = this.container.getItem(0);
		ItemStack itemStack2 = this.container.getItem(1);
		ItemStack itemStack3 = this.resultContainer.getItem(2);
		if (itemStack3.isEmpty() || !itemStack.isEmpty() && !itemStack2.isEmpty()) {
			if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
				this.setupResultSlot(itemStack, itemStack2, itemStack3);
			}
		} else {
			this.resultContainer.removeItemNoUpdate(2);
		}
	}

	private void setupResultSlot(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3) {
		this.access.execute((level, blockPos) -> {
			Item item = itemStack2.getItem();
			MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
			if (mapItemSavedData != null) {
				ItemStack itemStack4;
				if (item == Items.PAPER && !mapItemSavedData.locked && mapItemSavedData.scale < 4) {
					itemStack4 = itemStack.copy();
					itemStack4.setCount(1);
					itemStack4.getOrCreateTag().putInt("map_scale_direction", 1);
					this.broadcastChanges();
				} else if (item == Items.GLASS_PANE && !mapItemSavedData.locked) {
					itemStack4 = itemStack.copy();
					itemStack4.setCount(1);
					itemStack4.getOrCreateTag().putBoolean("map_to_lock", true);
					this.broadcastChanges();
				} else {
					if (item != Items.MAP) {
						this.resultContainer.removeItemNoUpdate(2);
						this.broadcastChanges();
						return;
					}

					itemStack4 = itemStack.copy();
					itemStack4.setCount(2);
					this.broadcastChanges();
				}

				if (!ItemStack.matches(itemStack4, itemStack3)) {
					this.resultContainer.setItem(2, itemStack4);
					this.broadcastChanges();
				}
			}
		});
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return slot.container != this.resultContainer && super.canTakeItemForPickAll(itemStack, slot);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = (Slot)this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			Item item = itemStack2.getItem();
			itemStack = itemStack2.copy();
			if (i == 2) {
				item.onCraftedBy(itemStack2, player.level, player);
				if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i != 1 && i != 0) {
				if (item == Items.FILLED_MAP) {
					if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (item != Items.PAPER && item != Items.MAP && item != Items.GLASS_PANE) {
					if (i >= 3 && i < 30) {
						if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
							return ItemStack.EMPTY;
						}
					} else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			}

			slot.setChanged();
			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
			this.broadcastChanges();
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultContainer.removeItemNoUpdate(2);
		this.access.execute((level, blockPos) -> this.clearContainer(player, player.level, this.container));
	}
}
