package net.minecraft.client.gui.screens.recipebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

@Environment(EnvType.CLIENT)
public interface RecipeUpdateListener {
	void recipesUpdated();

	void fillGhostRecipe(RecipeDisplay recipeDisplay);
}
