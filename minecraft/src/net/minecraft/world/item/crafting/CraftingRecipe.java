package net.minecraft.world.item.crafting;

public interface CraftingRecipe extends Recipe<CraftingInput> {
	@Override
	default RecipeType<?> getType() {
		return RecipeType.CRAFTING;
	}

	CraftingBookCategory category();
}
