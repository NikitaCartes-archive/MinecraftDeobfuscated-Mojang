package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;

public class ServerPlaceRecipe<C extends Container> implements PlaceRecipe<Integer> {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final StackedContents stackedContents = new StackedContents();
	protected Inventory inventory;
	protected RecipeBookMenu<C> menu;

	public ServerPlaceRecipe(RecipeBookMenu<C> recipeBookMenu) {
		this.menu = recipeBookMenu;
	}

	public void recipeClicked(ServerPlayer serverPlayer, @Nullable Recipe<C> recipe, boolean bl) {
		if (recipe != null && serverPlayer.getRecipeBook().contains(recipe)) {
			this.inventory = serverPlayer.getInventory();
			if (this.testClearGrid() || serverPlayer.isCreative()) {
				this.stackedContents.clear();
				serverPlayer.getInventory().fillStackedContents(this.stackedContents);
				this.menu.fillCraftSlotsStackedContents(this.stackedContents);
				if (this.stackedContents.canCraft(recipe, null)) {
					this.handleRecipeClicked(recipe, bl);
				} else {
					this.clearGrid();
					serverPlayer.connection.send(new ClientboundPlaceGhostRecipePacket(serverPlayer.containerMenu.containerId, recipe));
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

	protected void handleRecipeClicked(Recipe<C> recipe, boolean bl) {
		boolean bl2 = this.menu.recipeMatches(recipe);
		int i = this.stackedContents.getBiggestCraftableStack(recipe, null);
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
		if (this.stackedContents.canCraft(recipe, intList, jx)) {
			int k = jx;

			for (int l : intList) {
				int m = StackedContents.fromStackingIndex(l).getMaxStackSize();
				if (m < k) {
					k = m;
				}
			}

			if (this.stackedContents.canCraft(recipe, intList, k)) {
				this.clearGrid();
				this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, intList.iterator(), k);
			}
		}
	}

	@Override
	public void addItemToSlot(Iterator<Integer> iterator, int i, int j, int k, int l) {
		Slot slot = this.menu.getSlot(i);
		ItemStack itemStack = StackedContents.fromStackingIndex((Integer)iterator.next());
		if (!itemStack.isEmpty()) {
			for (int m = 0; m < j; m++) {
				this.moveItemToGrid(slot, itemStack);
			}
		}
	}

	protected int getStackSize(boolean bl, int i, boolean bl2) {
		int j = 1;
		if (bl) {
			j = i;
		} else if (bl2) {
			j = 1024;

			for (int k = 0; k < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; k++) {
				if (k != this.menu.getResultSlotIndex()) {
					ItemStack itemStack = this.menu.getSlot(k).getItem();
					if (!itemStack.isEmpty() && j > itemStack.getCount()) {
						j = itemStack.getCount();
					}
				}
			}

			if (j < 1024) {
				j++;
			}
		}

		return j;
	}

	protected void moveItemToGrid(Slot slot, ItemStack itemStack) {
		int i = this.inventory.findSlotMatchingUnusedItem(itemStack);
		if (i != -1) {
			ItemStack itemStack2 = this.inventory.getItem(i).copy();
			if (!itemStack2.isEmpty()) {
				if (itemStack2.getCount() > 1) {
					this.inventory.removeItem(i, 1);
				} else {
					this.inventory.removeItemNoUpdate(i);
				}

				itemStack2.setCount(1);
				if (slot.getItem().isEmpty()) {
					slot.set(itemStack2);
				} else {
					slot.getItem().grow(1);
				}
			}
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
							if (itemStack2.sameItem(itemStack)
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
