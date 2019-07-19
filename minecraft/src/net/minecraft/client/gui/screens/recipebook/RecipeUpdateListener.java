package net.minecraft.client.gui.screens.recipebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RecipeUpdateListener {
	void recipesUpdated();

	RecipeBookComponent getRecipeBookComponent();
}
