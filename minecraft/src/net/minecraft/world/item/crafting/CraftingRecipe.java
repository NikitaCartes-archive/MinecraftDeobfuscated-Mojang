package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface CraftingRecipe extends Recipe<CraftingInput> {
	@Override
	default RecipeType<CraftingRecipe> getType() {
		return RecipeType.CRAFTING;
	}

	@Override
	RecipeSerializer<? extends CraftingRecipe> getSerializer();

	CraftingBookCategory category();

	default NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
		return defaultCraftingReminder(craftingInput);
	}

	static NonNullList<ItemStack> defaultCraftingReminder(CraftingInput craftingInput) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			Item item = craftingInput.getItem(i).getItem();
			nonNullList.set(i, item.getCraftingRemainder());
		}

		return nonNullList;
	}

	@Override
	default BasicRecipeBookCategory recipeBookCategory() {
		return switch (this.category()) {
			case BUILDING -> BasicRecipeBookCategory.CRAFTING_BUILDING_BLOCKS;
			case EQUIPMENT -> BasicRecipeBookCategory.CRAFTING_EQUIPMENT;
			case REDSTONE -> BasicRecipeBookCategory.CRAFTING_REDSTONE;
			case MISC -> BasicRecipeBookCategory.CRAFTING_MISC;
		};
	}
}
