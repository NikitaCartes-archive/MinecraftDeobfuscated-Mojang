package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class RecipeBookMenu extends AbstractContainerMenu {
	public RecipeBookMenu(MenuType<?> menuType, int i) {
		super(menuType, i);
	}

	public abstract RecipeBookMenu.PostPlaceAction handlePlacement(
		boolean bl, boolean bl2, RecipeHolder<?> recipeHolder, ServerLevel serverLevel, Inventory inventory
	);

	public abstract void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents);

	public abstract RecipeBookType getRecipeBookType();

	public static enum PostPlaceAction {
		NOTHING,
		PLACE_GHOST_RECIPE;
	}
}
