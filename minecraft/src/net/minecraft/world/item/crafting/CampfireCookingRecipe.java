package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
	public CampfireCookingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	protected Item furnaceIcon() {
		return Items.CAMPFIRE;
	}

	@Override
	public RecipeSerializer<CampfireCookingRecipe> getSerializer() {
		return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
	}

	@Override
	public RecipeType<CampfireCookingRecipe> getType() {
		return RecipeType.CAMPFIRE_COOKING;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CAMPFIRE;
	}
}
