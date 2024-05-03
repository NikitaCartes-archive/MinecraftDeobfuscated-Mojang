package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<I extends RecipeInput, R extends Recipe<I>> implements PlaceRecipe<Integer> {
	private static final int ITEM_NOT_FOUND = -1;
	protected final StackedContents stackedContents = new StackedContents();
	protected Inventory inventory;
	protected RecipeBookMenu<I, R> menu;

	public ServerPlaceRecipe(RecipeBookMenu<I, R> recipeBookMenu) {
		this.menu = recipeBookMenu;
	}

	public void recipeClicked(ServerPlayer serverPlayer, @Nullable RecipeHolder<R> recipeHolder, boolean bl) {
		if (recipeHolder != null && serverPlayer.getRecipeBook().contains(recipeHolder)) {
			this.inventory = serverPlayer.getInventory();
			if (this.testClearGrid() || serverPlayer.isCreative()) {
				this.stackedContents.clear();
				serverPlayer.getInventory().fillStackedContents(this.stackedContents);
				this.menu.fillCraftSlotsStackedContents(this.stackedContents);
				if (this.stackedContents.canCraft(recipeHolder.value(), null)) {
					this.handleRecipeClicked(recipeHolder, bl);
				} else {
					this.clearGrid();
					serverPlayer.connection.send(new ClientboundPlaceGhostRecipePacket(serverPlayer.containerMenu.containerId, recipeHolder));
				}

				serverPlayer.getInventory().setChanged();
			}
		}
	}

	protected void clearGrid() {
		for (int i = 0; i < this.menu.getSize(); i++) {
			if (this.menu.shouldMoveToInventory(i)) {
				ItemStack itemStack = this.menu.getSlot(i).getItem().copy();
				this.inventory.placeItemBackInInventory(itemStack, false);
				this.menu.getSlot(i).set(itemStack);
			}
		}

		this.menu.clearCraftingContent();
	}

	protected void handleRecipeClicked(RecipeHolder<R> recipeHolder, boolean bl) {
		boolean bl2 = this.menu.recipeMatches(recipeHolder);
		int i = this.stackedContents.getBiggestCraftableStack(recipeHolder, null);
		if (bl2) {
			for (int j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; j++) {
				if (j != this.menu.getResultSlotIndex()) {
					ItemStack itemStack = this.menu.getSlot(j).getItem();
					if (!itemStack.isEmpty() && Math.min(i, itemStack.getMaxStackSize()) < itemStack.getCount() + 1) {
						return;
					}
				}
			}
		}

		int jx = this.getStackSize(bl, i, bl2);
		IntList intList = new IntArrayList();
		if (this.stackedContents.canCraft(recipeHolder.value(), intList, jx)) {
			int k = jx;

			for (int l : intList) {
				ItemStack itemStack2 = StackedContents.fromStackingIndex(l);
				if (!itemStack2.isEmpty()) {
					int m = itemStack2.getMaxStackSize();
					if (m < k) {
						k = m;
					}
				}
			}

			if (this.stackedContents.canCraft(recipeHolder.value(), intList, k)) {
				this.clearGrid();
				this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipeHolder, intList.iterator(), k);
			}
		}
	}

	public void addItemToSlot(Integer integer, int i, int j, int k, int l) {
		Slot slot = this.menu.getSlot(i);
		ItemStack itemStack = StackedContents.fromStackingIndex(integer);
		if (!itemStack.isEmpty()) {
			int m = j;

			while (m > 0) {
				m = this.moveItemToGrid(slot, itemStack, m);
				if (m == -1) {
					return;
				}
			}
		}
	}

	protected int getStackSize(boolean bl, int i, boolean bl2) {
		int j = 1;
		if (bl) {
			j = i;
		} else if (bl2) {
			j = Integer.MAX_VALUE;

			for (int k = 0; k < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; k++) {
				if (k != this.menu.getResultSlotIndex()) {
					ItemStack itemStack = this.menu.getSlot(k).getItem();
					if (!itemStack.isEmpty() && j > itemStack.getCount()) {
						j = itemStack.getCount();
					}
				}
			}

			if (j != Integer.MAX_VALUE) {
				j++;
			}
		}

		return j;
	}

	protected int moveItemToGrid(Slot slot, ItemStack itemStack, int i) {
		int j = this.inventory.findSlotMatchingUnusedItem(itemStack);
		if (j == -1) {
			return -1;
		} else {
			ItemStack itemStack2 = this.inventory.getItem(j);
			int k;
			if (i < itemStack2.getCount()) {
				this.inventory.removeItem(j, i);
				k = i;
			} else {
				this.inventory.removeItemNoUpdate(j);
				k = itemStack2.getCount();
			}

			if (slot.getItem().isEmpty()) {
				slot.set(itemStack2.copyWithCount(k));
			} else {
				slot.getItem().grow(k);
			}

			return i - k;
		}
	}

	private boolean testClearGrid() {
		List<ItemStack> list = Lists.<ItemStack>newArrayList();
		int i = this.getAmountOfFreeSlotsInInventory();

		for (int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; j++) {
			if (j != this.menu.getResultSlotIndex()) {
				ItemStack itemStack = this.menu.getSlot(j).getItem().copy();
				if (!itemStack.isEmpty()) {
					int k = this.inventory.getSlotWithRemainingSpace(itemStack);
					if (k == -1 && list.size() <= i) {
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
					} else if (k == -1) {
						return false;
					}
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
}
