package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlastingRecipe extends AbstractCookingRecipe {
	public BlastingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(RecipeType.BLASTING, string, cookingBookCategory, ingredient, itemStack, f, i);
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.BLAST_FURNACE);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.BLASTING_RECIPE;
	}
}
