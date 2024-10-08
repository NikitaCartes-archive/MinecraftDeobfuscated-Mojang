package net.minecraft.world.item.crafting;

import java.util.Optional;
import net.minecraft.world.level.Level;

public interface SmithingRecipe extends Recipe<SmithingRecipeInput> {
	@Override
	default RecipeType<SmithingRecipe> getType() {
		return RecipeType.SMITHING;
	}

	@Override
	RecipeSerializer<? extends SmithingRecipe> getSerializer();

	default boolean matches(SmithingRecipeInput smithingRecipeInput, Level level) {
		return Ingredient.testOptionalIngredient(this.templateIngredient(), smithingRecipeInput.template())
			&& Ingredient.testOptionalIngredient(this.baseIngredient(), smithingRecipeInput.base())
			&& Ingredient.testOptionalIngredient(this.additionIngredient(), smithingRecipeInput.addition());
	}

	Optional<Ingredient> templateIngredient();

	Optional<Ingredient> baseIngredient();

	Optional<Ingredient> additionIngredient();

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.SMITHING;
	}
}
