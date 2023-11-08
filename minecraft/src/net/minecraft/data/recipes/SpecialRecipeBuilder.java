package net.minecraft.data.recipes;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;

public class SpecialRecipeBuilder {
	private final Function<CraftingBookCategory, Recipe<?>> factory;

	public SpecialRecipeBuilder(Function<CraftingBookCategory, Recipe<?>> function) {
		this.factory = function;
	}

	public static SpecialRecipeBuilder special(Function<CraftingBookCategory, Recipe<?>> function) {
		return new SpecialRecipeBuilder(function);
	}

	public void save(RecipeOutput recipeOutput, String string) {
		this.save(recipeOutput, new ResourceLocation(string));
	}

	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		recipeOutput.accept(resourceLocation, (Recipe<?>)this.factory.apply(CraftingBookCategory.MISC), null);
	}
}
