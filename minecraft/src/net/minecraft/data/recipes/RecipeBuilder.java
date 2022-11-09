package net.minecraft.data.recipes;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
	ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

	RecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance);

	RecipeBuilder group(@Nullable String string);

	Item getResult();

	void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation);

	default void save(Consumer<FinishedRecipe> consumer) {
		this.save(consumer, getDefaultRecipeId(this.getResult()));
	}

	default void save(Consumer<FinishedRecipe> consumer, String string) {
		ResourceLocation resourceLocation = getDefaultRecipeId(this.getResult());
		ResourceLocation resourceLocation2 = new ResourceLocation(string);
		if (resourceLocation2.equals(resourceLocation)) {
			throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
		} else {
			this.save(consumer, resourceLocation2);
		}
	}

	static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
		return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
	}
}
