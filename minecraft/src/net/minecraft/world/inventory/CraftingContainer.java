package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class CraftingContainer implements Container, StackedContentsCompatible {
	private final NonNullList<ItemStack> items;
	private final int width;
	private final int height;
	private final AbstractContainerMenu menu;

	public CraftingContainer(AbstractContainerMenu abstractContainerMenu, int i, int j) {
		this.items = NonNullList.withSize(i * j, ItemStack.EMPTY);
		this.menu = abstractContainerMenu;
		this.width = i;
		this.height = j;
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.items) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int i) {
		return i >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(i);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(this.items, i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
		if (!itemStack.isEmpty()) {
			this.menu.slotsChanged(this);
		}

		return itemStack;
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.items.set(i, itemStack);
		this.menu.slotsChanged(this);
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	@Override
	public void fillStackedContents(StackedContents stackedContents) {
		for (ItemStack itemStack : this.items) {
			stackedContents.accountSimpleStack(itemStack);
		}
	}
}
