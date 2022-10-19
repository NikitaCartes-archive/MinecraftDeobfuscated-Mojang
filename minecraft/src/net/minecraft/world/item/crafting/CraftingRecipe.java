package net.minecraft.world.item.crafting;

import net.minecraft.world.inventory.CraftingContainer;

public interface CraftingRecipe extends Recipe<CraftingContainer> {
	@Override
	default RecipeType<?> getType() {
		return RecipeType.CRAFTING;
	}

	CraftingBookCategory category();
}
