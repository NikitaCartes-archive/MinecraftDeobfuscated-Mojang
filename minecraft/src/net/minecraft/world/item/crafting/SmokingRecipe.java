package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SmokingRecipe extends AbstractCookingRecipe {
	public SmokingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	protected Item furnaceIcon() {
		return Items.SMOKER;
	}

	@Override
	public RecipeType<SmokingRecipe> getType() {
		return RecipeType.SMOKING;
	}

	@Override
	public RecipeSerializer<SmokingRecipe> getSerializer() {
		return RecipeSerializer.SMOKING_RECIPE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.SMOKER_FOOD;
	}
}
