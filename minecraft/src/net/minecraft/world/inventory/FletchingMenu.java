package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.FletchingBlockEntity;

public class FletchingMenu extends AbstractContainerMenu {
	public static final int FEATHER_PADDING = 59;
	public static final int MIDDLE_COLUMN = 79;
	public static final int FEATHER_AXIS = 38;
	private static final int DATA_COUNT = 6;
	private static final int INV_SLOT_START = 3;
	private static final int INV_SLOT_END = 30;
	private static final int USE_ROW_SLOT_START = 30;
	private static final int USE_ROW_SLOT_END = 39;
	public static final int TITLE_PADDING = 160;
	private final Container fletching;
	private final ContainerData fletchingData;
	private final Slot ingredientSlot;

	public FletchingMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(3), new SimpleContainerData(6));
	}

	public FletchingMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
		super(MenuType.FLETCHING, i);
		checkContainerSize(container, 3);
		checkContainerDataCount(containerData, 6);
		this.fletching = container;
		this.fletchingData = containerData;
		this.ingredientSlot = this.addSlot(new FletchingMenu.IngredientsSlot(container, 0, 239, 17));
		this.addSlot(new FurnaceResultSlot(inventory.player, container, 1, 239, 59));
		this.addSlot(new Slot(container, 2, 180, 38) {
			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addDataSlots(containerData);

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 168 + k * 18, 84 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 168 + j * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.fletching.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i != 0 && i != 1 && i != 2) {
				if (this.ingredientSlot.mayPlace(itemStack2)) {
					if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (itemStack2.is(Items.FEATHER)) {
					if (!this.moveItemStackTo(itemStack2, 2, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 3 && i < 30) {
					if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 30 && i < 39) {
					if (!this.moveItemStackTo(itemStack2, 3, 30, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
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

	public int getProgresss() {
		return this.fletchingData.get(0);
	}

	public char getSourceQuality() {
		return (char)this.fletchingData.get(1);
	}

	public char getSourceImpurities() {
		return (char)this.fletchingData.get(2);
	}

	public char getResultImpurities() {
		return (char)this.fletchingData.get(3);
	}

	public int getProcessTime() {
		return this.fletchingData.get(4);
	}

	public boolean isExplored() {
		return this.fletchingData.get(5) > 0;
	}

	class IngredientsSlot extends Slot {
		public IngredientsSlot(Container container, int i, int j, int k) {
			super(container, i, j, k);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return FletchingBlockEntity.canAcceptItem(itemStack, FletchingMenu.this.getSourceQuality(), FletchingMenu.this.getSourceImpurities());
		}

		@Override
		public int getMaxStackSize() {
			return 64;
		}
	}
}
