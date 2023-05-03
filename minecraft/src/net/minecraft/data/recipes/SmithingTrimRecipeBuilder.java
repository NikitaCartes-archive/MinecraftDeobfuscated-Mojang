package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
	private final RecipeCategory category;
	private final Ingredient template;
	private final Ingredient base;
	private final Ingredient addition;
	private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
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

	public SmithingTrimRecipeBuilder unlocks(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement
			.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(RequirementsStrategy.OR);
		consumer.accept(
			new SmithingTrimRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.template,
				this.base,
				this.addition,
				this.advancement,
				resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")
			)
		);
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.advancement.getCriteria().isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}

	public static record Result(
		ResourceLocation id,
		RecipeSerializer<?> type,
		Ingredient template,
		Ingredient base,
		Ingredient addition,
		Advancement.Builder advancement,
		ResourceLocation advancementId
	) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			jsonObject.add("template", this.template.toJson());
			jsonObject.add("base", this.base.toJson());
			jsonObject.add("addition", this.addition.toJson());
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return this.type;
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return this.advancement.serializeToJson();
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return this.advancementId;
		}
	}
}
