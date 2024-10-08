package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlastingRecipe extends AbstractCookingRecipe {
	public BlastingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	protected Item furnaceIcon() {
		return Items.BLAST_FURNACE;
	}

	@Override
	public RecipeSerializer<BlastingRecipe> getSerializer() {
		return RecipeSerializer.BLASTING_RECIPE;
	}

	@Override
	public RecipeType<BlastingRecipe> getType() {
		return RecipeType.BLASTING;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return switch (this.category()) {
			case BLOCKS -> RecipeBookCategories.BLAST_FURNACE_BLOCKS;
			case FOOD, MISC -> RecipeBookCategories.BLAST_FURNACE_MISC;
		};
	}
}
