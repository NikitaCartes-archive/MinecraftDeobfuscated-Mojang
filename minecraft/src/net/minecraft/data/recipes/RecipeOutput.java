package net.minecraft.data.recipes;

import net.minecraft.advancements.Advancement;

public interface RecipeOutput {
	void accept(FinishedRecipe finishedRecipe);

	Advancement.Builder advancement();
}
