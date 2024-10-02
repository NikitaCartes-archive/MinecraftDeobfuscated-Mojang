package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SmeltingRecipe extends AbstractCookingRecipe {
	public SmeltingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	protected Item furnaceIcon() {
		return Items.FURNACE;
	}

	@Override
	public RecipeSerializer<SmeltingRecipe> getSerializer() {
		return RecipeSerializer.SMELTING_RECIPE;
	}

	@Override
	public RecipeType<SmeltingRecipe> getType() {
		return RecipeType.SMELTING;
	}

	@Override
	public BasicRecipeBookCategory recipeBookCategory() {
		return switch (this.category()) {
			case BLOCKS -> BasicRecipeBookCategory.FURNACE_BLOCKS;
			case FOOD -> BasicRecipeBookCategory.FURNACE_FOOD;
			case MISC -> BasicRecipeBookCategory.FURNACE_MISC;
		};
	}
}
