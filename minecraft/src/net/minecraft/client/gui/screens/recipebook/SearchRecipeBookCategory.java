package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

@Environment(EnvType.CLIENT)
public enum SearchRecipeBookCategory implements ExtendedRecipeBookCategory {
	CRAFTING(
		RecipeBookCategories.CRAFTING_EQUIPMENT,
		RecipeBookCategories.CRAFTING_BUILDING_BLOCKS,
		RecipeBookCategories.CRAFTING_MISC,
		RecipeBookCategories.CRAFTING_REDSTONE
	),
	FURNACE(RecipeBookCategories.FURNACE_FOOD, RecipeBookCategories.FURNACE_BLOCKS, RecipeBookCategories.FURNACE_MISC),
	BLAST_FURNACE(RecipeBookCategories.BLAST_FURNACE_BLOCKS, RecipeBookCategories.BLAST_FURNACE_MISC),
	SMOKER(RecipeBookCategories.SMOKER_FOOD);

	private final List<RecipeBookCategory> includedCategories;

	private SearchRecipeBookCategory(final RecipeBookCategory... recipeBookCategorys) {
		this.includedCategories = List.of(recipeBookCategorys);
	}

	public List<RecipeBookCategory> includedCategories() {
		return this.includedCategories;
	}
}
