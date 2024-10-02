package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceKey;

public interface RecipeAccess {
	RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourceKey);

	SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes();
}
