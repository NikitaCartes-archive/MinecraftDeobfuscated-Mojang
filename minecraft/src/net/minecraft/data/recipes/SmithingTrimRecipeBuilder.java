package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public class SmithingTrimRecipeBuilder {
	private final RecipeCategory category;
	private final Ingredient template;
	private final Ingredient base;
	private final Ingredient addition;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();

	public SmithingTrimRecipeBuilder(RecipeCategory recipeCategory, Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3) {
		this.category = recipeCategory;
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
	}

	public static SmithingTrimRecipeBuilder smithingTrim(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, RecipeCategory recipeCategory) {
		return new SmithingTrimRecipeBuilder(recipeCategory, ingredient, ingredient2, ingredient3);
	}

	public SmithingTrimRecipeBuilder unlocks(String string, Criterion<?> criterion) {
		this.criteria.put(string, criterion);
		return this;
	}

	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> resourceKey) {
		this.ensureValid(resourceKey);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey))
			.rewards(AdvancementRewards.Builder.recipe(resourceKey))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		SmithingTrimRecipe smithingTrimRecipe = new SmithingTrimRecipe(Optional.of(this.template), Optional.of(this.base), Optional.of(this.addition));
		recipeOutput.accept(resourceKey, smithingTrimRecipe, builder.build(resourceKey.location().withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceKey<Recipe<?>> resourceKey) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceKey.location());
		}
	}
}
