package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class SmeltingRecipe extends AbstractCookingRecipe {
	public SmeltingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(RecipeType.SMELTING, string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.FURNACE);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMELTING_RECIPE;
	}
}
