package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public abstract class RecipeBookMenu<I extends RecipeInput, R extends Recipe<I>> extends AbstractContainerMenu {
	public RecipeBookMenu(MenuType<?> menuType, int i) {
		super(menuType, i);
	}

	public void handlePlacement(boolean bl, RecipeHolder<?> recipeHolder, ServerPlayer serverPlayer) {
		RecipeHolder<R> recipeHolder2 = (RecipeHolder<R>)recipeHolder;
		this.beginPlacingRecipe();

		try {
			new ServerPlaceRecipe<>(this).recipeClicked(serverPlayer, recipeHolder2, bl);
		} finally {
			this.finishPlacingRecipe((RecipeHolder<R>)recipeHolder);
		}
	}

	protected void beginPlacingRecipe() {
	}

	protected void finishPlacingRecipe(RecipeHolder<R> recipeHolder) {
	}

	public abstract void fillCraftSlotsStackedContents(StackedContents stackedContents);

	public abstract void clearCraftingContent();

	public abstract boolean recipeMatches(RecipeHolder<R> recipeHolder);

	public abstract int getResultSlotIndex();

	public abstract int getGridWidth();

	public abstract int getGridHeight();

	public abstract int getSize();

	public abstract RecipeBookType getRecipeBookType();

	public abstract boolean shouldMoveToInventory(int i);
}
