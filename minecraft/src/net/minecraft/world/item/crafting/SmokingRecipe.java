package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class SmokingRecipe extends AbstractCookingRecipe {
	public SmokingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(RecipeType.SMOKING, string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.SMOKER);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMOKING_RECIPE;
	}
}
