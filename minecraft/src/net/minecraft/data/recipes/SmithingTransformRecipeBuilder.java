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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTransformRecipeBuilder {
	private final Ingredient template;
	private final Ingredient base;
	private final Ingredient addition;
	private final RecipeCategory category;
	private final Item result;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
	private final RecipeSerializer<?> type;

	public SmithingTransformRecipeBuilder(
		RecipeSerializer<?> recipeSerializer, Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, RecipeCategory recipeCategory, Item item
	) {
		this.category = recipeCategory;
		this.type = recipeSerializer;
		this.template = ingredient;
		this.base = ingredient2;
		this.addition = ingredient3;
		this.result = item;
	}

	public static SmithingTransformRecipeBuilder smithing(
		Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, RecipeCategory recipeCategory, Item item
	) {
		return new SmithingTransformRecipeBuilder(RecipeSerializer.SMITHING_TRANSFORM, ingredient, ingredient2, ingredient3, recipeCategory, item);
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
		recipeOutput.accept(
			new SmithingTransformRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.template,
				this.base,
				this.addition,
				this.result,
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
		ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Item result, AdvancementHolder advancement
	) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			jsonObject.add("template", this.template.toJson(true));
			jsonObject.add("base", this.base.toJson(true));
			jsonObject.add("addition", this.addition.toJson(true));
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
			jsonObject.add("result", jsonObject2);
		}
	}
}
