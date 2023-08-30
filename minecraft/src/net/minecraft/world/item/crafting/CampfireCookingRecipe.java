package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
	public CampfireCookingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(RecipeType.CAMPFIRE_COOKING, string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.CAMPFIRE);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
	}
}
