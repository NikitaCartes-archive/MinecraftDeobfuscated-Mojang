package net.minecraft.world.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;

public abstract class RecipeBookMenu<C extends Container> extends AbstractContainerMenu {
	public RecipeBookMenu(MenuType<?> menuType, int i) {
		super(menuType, i);
	}

	public void handlePlacement(boolean bl, Recipe<?> recipe, ServerPlayer serverPlayer) {
		new ServerPlaceRecipe<>(this).recipeClicked(serverPlayer, (Recipe<C>)recipe, bl);
	}

	public abstract void fillCraftSlotsStackedContents(StackedContents stackedContents);

	public abstract void clearCraftingContent();

	public abstract boolean recipeMatches(Recipe<? super C> recipe);

	public abstract int getResultSlotIndex();

	public abstract int getGridWidth();

	public abstract int getGridHeight();

	@Environment(EnvType.CLIENT)
	public abstract int getSize();
}
