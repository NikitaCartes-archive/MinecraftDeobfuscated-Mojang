package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public class SmithingTransformRecipeBuilder {
	private final Ingredient template;
	private final Ingredient base;
	private final Ingredient addition;
	private final RecipeCategory category;
	private final Item result;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();

	public SmithingTransformRecipeBuilder(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, RecipeCategory recipeCategory, Item item) {
		this.category = recipeCategory;
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
		this.result = item;
	}

	public static SmithingTransformRecipeBuilder smithing(
		Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, RecipeCategory recipeCategory, Item item
	) {
		return new SmithingTransformRecipeBuilder(ingredient, ingredient2, ingredient3, recipeCategory, item);
	}

	public SmithingTransformRecipeBuilder unlocks(String string, Criterion<?> criterion) {
		this.criteria.put(string, criterion);
		return this;
	}

	public void save(RecipeOutput recipeOutput, String string) {
		this.save(recipeOutput, new ResourceLocation(string));
	}

	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		SmithingTransformRecipe smithingTransformRecipe = new SmithingTransformRecipe(this.template, this.base, this.addition, new ItemStack(this.result));
		recipeOutput.accept(resourceLocation, smithingTransformRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}
}
