package net.minecraft.data.recipes;

import java.util.function.Consumer;
import net.minecraft.advancements.CriterionTriggerInstance;

public interface RecipeBuilder {
	RecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance);

	RecipeBuilder group(String string);

	void save(Consumer<FinishedRecipe> consumer);
}
