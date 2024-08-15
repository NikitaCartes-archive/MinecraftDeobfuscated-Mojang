package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<R extends Recipe<?>> {
	private static final int ITEM_NOT_FOUND = -1;
	private final Inventory inventory;
	private final ServerPlaceRecipe.CraftingMenuAccess<R> menu;
	private final boolean useMaxItems;
	private final int gridWidth;
	private final int gridHeight;
	private final List<Slot> inputGridSlots;
	private final List<Slot> slotsToClear;

	public static <I extends RecipeInput, R extends Recipe<I>> RecipeBookMenu.PostPlaceAction placeRecipe(
		ServerPlaceRecipe.CraftingMenuAccess<R> craftingMenuAccess,
		int i,
		int j,
		List<Slot> list,
		List<Slot> list2,
		Inventory inventory,
		RecipeHolder<R> recipeHolder,
		boolean bl,
		boolean bl2
	) {
		ServerPlaceRecipe<R> serverPlaceRecipe = new ServerPlaceRecipe<>(craftingMenuAccess, inventory, bl, i, j, list, list2);
		if (!bl2 && !serverPlaceRecipe.testClearGrid()) {
			return RecipeBookMenu.PostPlaceAction.NOTHING;
		} else {
			StackedItemContents stackedItemContents = new StackedItemContents();
			inventory.fillStackedContents(stackedItemContents);
			craftingMenuAccess.fillCraftSlotsStackedContents(stackedItemContents);
			return serverPlaceRecipe.tryPlaceRecipe(recipeHolder, stackedItemContents);
		}
	}

	private ServerPlaceRecipe(
		ServerPlaceRecipe.CraftingMenuAccess<R> craftingMenuAccess, Inventory inventory, boolean bl, int i, int j, List<Slot> list, List<Slot> list2
	) {
		this.menu = craftingMenuAccess;
		this.inventory = inventory;
		this.useMaxItems = bl;
		this.gridWidth = i;
		this.gridHeight = j;
		this.inputGridSlots = list;
		this.slotsToClear = list2;
	}

	private RecipeBookMenu.PostPlaceAction tryPlaceRecipe(RecipeHolder<R> recipeHolder, StackedItemContents stackedItemContents) {
		if (stackedItemContents.canCraft(recipeHolder.value(), null)) {
			this.placeRecipe(recipeHolder, stackedItemContents);
			this.inventory.setChanged();
			return RecipeBookMenu.PostPlaceAction.NOTHING;
		} else {
			this.clearGrid();
			this.inventory.setChanged();
			return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
		}
	}

	private void clearGrid() {
		for (Slot slot : this.slotsToClear) {
			ItemStack itemStack = slot.getItem().copy();
			this.inventory.placeItemBackInInventory(itemStack, false);
			slot.set(itemStack);
		}

		this.menu.clearCraftingContent();
	}

	private void placeRecipe(RecipeHolder<R> recipeHolder, StackedItemContents stackedItemContents) {
		boolean bl = this.menu.recipeMatches(recipeHolder);
		int i = stackedItemContents.getBiggestCraftableStack(recipeHolder.value(), null);
		if (bl) {
			for (Slot slot : this.inputGridSlots) {
				ItemStack itemStack = slot.getItem();
				if (!itemStack.isEmpty() && Math.min(i, itemStack.getMaxStackSize()) < itemStack.getCount() + 1) {
					return;
				}
			}
		}

		int j = this.calculateAmountToCraft(i, bl);
		List<Holder<Item>> list = new ArrayList();
		if (stackedItemContents.canCraft(recipeHolder.value(), j, list::add)) {
			OptionalInt optionalInt = list.stream().mapToInt(holder -> ((Item)holder.value()).getDefaultMaxStackSize()).min();
			if (optionalInt.isPresent()) {
				j = Math.min(j, optionalInt.getAsInt());
			}

			list.clear();
			if (stackedItemContents.canCraft(recipeHolder.value(), j, list::add)) {
				this.clearGrid();
				int k = j;
				PlaceRecipeHelper.placeRecipe(this.gridWidth, this.gridHeight, recipeHolder, recipeHolder.value().placementInfo().slotInfo(), (optional, jx, kx, l) -> {
					if (!optional.isEmpty()) {
						Slot slotx = (Slot)this.inputGridSlots.get(jx);
						int m = ((PlacementInfo.SlotInfo)optional.get()).placerOutputPosition();
						int n = k;

						while (n > 0) {
							Holder<Item> holder = (Holder<Item>)list.get(m);
							n = this.moveItemToGrid(slotx, holder, n);
							if (n == -1) {
								return;
							}
						}
					}
				});
			}
		}
	}

	private int calculateAmountToCraft(int i, boolean bl) {
		if (this.useMaxItems) {
			return i;
		} else if (bl) {
			int j = Integer.MAX_VALUE;

			for (Slot slot : this.inputGridSlots) {
				ItemStack itemStack = slot.getItem();
				if (!itemStack.isEmpty() && j > itemStack.getCount()) {
					j = itemStack.getCount();
				}
			}

			if (j != Integer.MAX_VALUE) {
				j++;
			}

			return j;
		} else {
			return 1;
		}
	}

	private int moveItemToGrid(Slot slot, Holder<Item> holder, int i) {
		int j = this.inventory.findSlotMatchingCraftingIngredient(holder);
		if (j == -1) {
			return -1;
		} else {
			ItemStack itemStack = this.inventory.getItem(j);
			int k;
			if (i < itemStack.getCount()) {
				this.inventory.removeItem(j, i);
				k = i;
			} else {
				this.inventory.removeItemNoUpdate(j);
				k = itemStack.getCount();
			}

			if (slot.getItem().isEmpty()) {
				slot.set(itemStack.copyWithCount(k));
			} else {
				slot.getItem().grow(k);
			}

			return i - k;
		}
	}

	private boolean testClearGrid() {
		List<ItemStack> list = Lists.<ItemStack>newArrayList();
		int i = this.getAmountOfFreeSlotsInInventory();

		for (Slot slot : this.inputGridSlots) {
			ItemStack itemStack = slot.getItem().copy();
			if (!itemStack.isEmpty()) {
				int j = this.inventory.getSlotWithRemainingSpace(itemStack);
				if (j == -1 && list.size() <= i) {
					for (ItemStack itemStack2 : list) {
						if (ItemStack.isSameItem(itemStack2, itemStack)
							&& itemStack2.getCount() != itemStack2.getMaxStackSize()
							&& itemStack2.getCount() + itemStack.getCount() <= itemStack2.getMaxStackSize()) {
							itemStack2.grow(itemStack.getCount());
							itemStack.setCount(0);
							break;
						}
					}

					if (!itemStack.isEmpty()) {
						if (list.size() >= i) {
							return false;
						}

						list.add(itemStack);
					}
				} else if (j == -1) {
					return false;
				}
			}
		}

		return true;
	}

	private int getAmountOfFreeSlotsInInventory() {
		int i = 0;

		for (ItemStack itemStack : this.inventory.items) {
			if (itemStack.isEmpty()) {
				i++;
			}
		}

		return i;
	}

	public interface CraftingMenuAccess<T extends Recipe<?>> {
		void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents);

		void clearCraftingContent();

		boolean recipeMatches(RecipeHolder<T> recipeHolder);
	}
}
