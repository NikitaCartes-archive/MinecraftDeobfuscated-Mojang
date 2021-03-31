package net.minecraft.world.inventory;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;

public class BrewingStandMenu extends AbstractContainerMenu {
	private static final int BOTTLE_SLOT_START = 0;
	private static final int BOTTLE_SLOT_END = 2;
	private static final int INGREDIENT_SLOT = 3;
	private static final int FUEL_SLOT = 4;
	private static final int SLOT_COUNT = 5;
	private static final int DATA_COUNT = 2;
	private static final int INV_SLOT_START = 5;
	private static final int INV_SLOT_END = 32;
	private static final int USE_ROW_SLOT_START = 32;
	private static final int USE_ROW_SLOT_END = 41;
	private final Container brewingStand;
	private final ContainerData brewingStandData;
	private final Slot ingredientSlot;

	public BrewingStandMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(5), new SimpleContainerData(2));
	}

	public BrewingStandMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
		super(MenuType.BREWING_STAND, i);
		checkContainerSize(container, 5);
		checkContainerDataCount(containerData, 2);
		this.brewingStand = container;
		this.brewingStandData = containerData;
		this.addSlot(new BrewingStandMenu.PotionSlot(container, 0, 56, 51));
		this.addSlot(new BrewingStandMenu.PotionSlot(container, 1, 79, 58));
		this.addSlot(new BrewingStandMenu.PotionSlot(container, 2, 102, 51));
		this.ingredientSlot = this.addSlot(new BrewingStandMenu.IngredientsSlot(container, 3, 79, 17));
		this.addSlot(new BrewingStandMenu.FuelSlot(container, 4, 17, 17));
		this.addDataSlots(containerData);

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
		return this.brewingStand.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if ((i < 0 || i > 2) && i != 3 && i != 4) {
				if (BrewingStandMenu.FuelSlot.mayPlaceItem(itemStack)) {
					if (this.moveItemStackTo(itemStack2, 4, 5, false) || this.ingredientSlot.mayPlace(itemStack2) && !this.moveItemStackTo(itemStack2, 3, 4, false)) {
						return ItemStack.EMPTY;
					}
				} else if (this.ingredientSlot.mayPlace(itemStack2)) {
					if (!this.moveItemStackTo(itemStack2, 3, 4, false)) {
						return ItemStack.EMPTY;
					}
				} else if (BrewingStandMenu.PotionSlot.mayPlaceItem(itemStack) && itemStack.getCount() == 1) {
					if (!this.moveItemStackTo(itemStack2, 0, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 5 && i < 32) {
					if (!this.moveItemStackTo(itemStack2, 32, 41, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 32 && i < 41) {
					if (!this.moveItemStackTo(itemStack2, 5, 32, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemStack2, 5, 41, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.moveItemStackTo(itemStack2, 5, 41, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			}

			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
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

	public int getFuel() {
		return this.brewingStandData.get(1);
	}

	public int getBrewingTicks() {
		return this.brewingStandData.get(0);
	}

	static class FuelSlot extends Slot {
		public FuelSlot(Container container, int i, int j, int k) {
			super(container, i, j, k);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return mayPlaceItem(itemStack);
		}

		public static boolean mayPlaceItem(ItemStack itemStack) {
			return itemStack.is(Items.BLAZE_POWDER);
		}

		@Override
		public int getMaxStackSize() {
			return 64;
		}
	}

	static class IngredientsSlot extends Slot {
		public IngredientsSlot(Container container, int i, int j, int k) {
			super(container, i, j, k);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return PotionBrewing.isIngredient(itemStack);
		}

		@Override
		public int getMaxStackSize() {
			return 64;
		}
	}

	static class PotionSlot extends Slot {
		public PotionSlot(Container container, int i, int j, int k) {
			super(container, i, j, k);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return mayPlaceItem(itemStack);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public void onTake(Player player, ItemStack itemStack) {
			Potion potion = PotionUtils.getPotion(itemStack);
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.BREWED_POTION.trigger((ServerPlayer)player, potion);
			}

			super.onTake(player, itemStack);
		}

		public static boolean mayPlaceItem(ItemStack itemStack) {
			return itemStack.is(Items.POTION) || itemStack.is(Items.SPLASH_POTION) || itemStack.is(Items.LINGERING_POTION) || itemStack.is(Items.GLASS_BOTTLE);
		}
	}
}
