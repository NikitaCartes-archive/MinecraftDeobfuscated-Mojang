package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
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

	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		SmithingTrimRecipe smithingTrimRecipe = new SmithingTrimRecipe(this.template, this.base, this.addition);
		recipeOutput.accept(resourceLocation, smithingTrimRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}
}
