package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
	private final RecipeCategory category;
	private final Ingredient template;
	private final Ingredient base;
	private final Ingredient addition;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
	private final RecipeSerializer<?> type;

	public SmithingTrimRecipeBuilder(
		RecipeSerializer<?> recipeSerializer, RecipeCategory recipeCategory, Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3
	) {
		this.category = recipeCategory;
		this.type = recipeSerializer;
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
	}

	public static SmithingTrimRecipeBuilder smithingTrim(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, RecipeCategory recipeCategory) {
		return new SmithingTrimRecipeBuilder(RecipeSerializer.SMITHING_TRIM, recipeCategory, ingredient, ingredient2, ingredient3);
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
		recipeOutput.accept(
			new SmithingTrimRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.template,
				this.base,
				this.addition,
				builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/"))
			)
		);
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}

	public static record Result(
		ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, AdvancementHolder advancement
	) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			jsonObject.add("template", this.template.toJson(true));
			jsonObject.add("base", this.base.toJson(true));
			jsonObject.add("addition", this.addition.toJson(true));
		}
	}
}
