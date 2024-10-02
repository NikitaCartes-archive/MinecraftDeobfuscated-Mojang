package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
	ResourceLocation ROOT_RECIPE_ADVANCEMENT = ResourceLocation.withDefaultNamespace("recipes/root");

	RecipeBuilder unlockedBy(String string, Criterion<?> criterion);

	RecipeBuilder group(@Nullable String string);

	Item getResult();

	void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> resourceKey);

	default void save(RecipeOutput recipeOutput) {
		this.save(recipeOutput, ResourceKey.create(Registries.RECIPE, getDefaultRecipeId(this.getResult())));
	}

	default void save(RecipeOutput recipeOutput, String string) {
		ResourceLocation resourceLocation = getDefaultRecipeId(this.getResult());
		ResourceLocation resourceLocation2 = ResourceLocation.parse(string);
		if (resourceLocation2.equals(resourceLocation)) {
			throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
		} else {
			this.save(recipeOutput, ResourceKey.create(Registries.RECIPE, resourceLocation2));
		}
	}

	static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
		return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
	}

	static CraftingBookCategory determineBookCategory(RecipeCategory recipeCategory) {
		return switch (recipeCategory) {
			case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
			case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
			case REDSTONE -> CraftingBookCategory.REDSTONE;
			default -> CraftingBookCategory.MISC;
		};
	}
}
