package net.minecraft.recipebook;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class ServerPlaceSmeltingRecipe<C extends Container> extends ServerPlaceRecipe<C> {
	private boolean recipeMatchesPlaced;

	public ServerPlaceSmeltingRecipe(RecipeBookMenu<C> recipeBookMenu) {
		super(recipeBookMenu);
	}

	@Override
	protected void handleRecipeClicked(Recipe<C> recipe, boolean bl) {
		this.recipeMatchesPlaced = this.menu.recipeMatches(recipe);
		int i = this.stackedContents.getBiggestCraftableStack(recipe, null);
		if (this.recipeMatchesPlaced) {
			ItemStack itemStack = this.menu.getSlot(0).getItem();
			if (itemStack.isEmpty() || i <= itemStack.getCount()) {
				return;
			}
		}

		int j = this.getStackSize(bl, i, this.recipeMatchesPlaced);
		IntList intList = new IntArrayList();
		if (this.stackedContents.canCraft(recipe, intList, j)) {
			if (!this.recipeMatchesPlaced) {
				this.moveItemToInventory(this.menu.getResultSlotIndex());
				this.moveItemToInventory(0);
			}

			this.placeRecipe(j, intList);
		}
	}

	@Override
	protected void clearGrid() {
		this.moveItemToInventory(this.menu.getResultSlotIndex());
		super.clearGrid();
	}

	protected void placeRecipe(int i, IntList intList) {
		Iterator<Integer> iterator = intList.iterator();
		Slot slot = this.menu.getSlot(0);
		ItemStack itemStack = StackedContents.fromStackingIndex((Integer)iterator.next());
		if (!itemStack.isEmpty()) {
			int j = Math.min(itemStack.getMaxStackSize(), i);
			if (this.recipeMatchesPlaced) {
				j -= slot.getItem().getCount();
			}

			for (int k = 0; k < j; k++) {
				this.moveItemToGrid(slot, itemStack);
			}
		}
	}
}
