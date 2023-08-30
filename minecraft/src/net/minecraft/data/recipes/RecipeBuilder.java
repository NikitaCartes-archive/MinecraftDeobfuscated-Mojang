package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
	ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

	RecipeBuilder unlockedBy(String string, Criterion<?> criterion);

	RecipeBuilder group(@Nullable String string);

	Item getResult();

	void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation);

	default void save(RecipeOutput recipeOutput) {
		this.save(recipeOutput, getDefaultRecipeId(this.getResult()));
	}

	default void save(RecipeOutput recipeOutput, String string) {
		ResourceLocation resourceLocation = getDefaultRecipeId(this.getResult());
		ResourceLocation resourceLocation2 = new ResourceLocation(string);
		if (resourceLocation2.equals(resourceLocation)) {
			throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
		} else {
			this.save(recipeOutput, resourceLocation2);
		}
	}

	static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
		return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
	}
}
