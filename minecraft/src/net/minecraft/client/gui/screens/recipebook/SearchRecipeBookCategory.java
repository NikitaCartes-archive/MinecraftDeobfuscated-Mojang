package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.BasicRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;

@Environment(EnvType.CLIENT)
public enum SearchRecipeBookCategory implements RecipeBookCategory {
	CRAFTING(
		BasicRecipeBookCategory.CRAFTING_EQUIPMENT,
		BasicRecipeBookCategory.CRAFTING_BUILDING_BLOCKS,
		BasicRecipeBookCategory.CRAFTING_MISC,
		BasicRecipeBookCategory.CRAFTING_REDSTONE
	),
	FURNACE(BasicRecipeBookCategory.FURNACE_FOOD, BasicRecipeBookCategory.FURNACE_BLOCKS, BasicRecipeBookCategory.FURNACE_MISC),
	BLAST_FURNACE(BasicRecipeBookCategory.BLAST_FURNACE_BLOCKS, BasicRecipeBookCategory.BLAST_FURNACE_MISC),
	SMOKER(BasicRecipeBookCategory.SMOKER_FOOD);

	private final List<BasicRecipeBookCategory> includedCategories;

	private SearchRecipeBookCategory(final BasicRecipeBookCategory... basicRecipeBookCategorys) {
		this.includedCategories = List.of(basicRecipeBookCategorys);
	}

	public List<BasicRecipeBookCategory> includedCategories() {
		return this.includedCategories;
	}
}
