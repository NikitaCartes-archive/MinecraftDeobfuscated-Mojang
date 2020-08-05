package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class ResultContainer implements Container, RecipeHolder {
	private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
	@Nullable
	private Recipe<?> recipeUsed;

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.itemStacks) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int i) {
		return this.itemStacks.get(0);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		return ContainerHelper.takeItem(this.itemStacks, 0);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(this.itemStacks, 0);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.itemStacks.set(0, itemStack);
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
		this.itemStacks.clear();
	}

	@Override
	public void setRecipeUsed(@Nullable Recipe<?> recipe) {
		this.recipeUsed = recipe;
	}

	@Nullable
	@Override
	public Recipe<?> getRecipeUsed() {
		return this.recipeUsed;
	}
}
