package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
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
	private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
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

	public SmithingTransformRecipeBuilder unlocks(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer, String string) {
		this.save(consumer, new ResourceLocation(string));
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement
			.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(RequirementsStrategy.OR);
		consumer.accept(
			new SmithingTransformRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.template,
				this.base,
				this.addition,
				this.result,
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
		Item result,
		Advancement.Builder advancement,
		ResourceLocation advancementId
	) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			jsonObject.add("template", this.template.toJson());
			jsonObject.add("base", this.base.toJson());
			jsonObject.add("addition", this.addition.toJson());
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
			jsonObject.add("result", jsonObject2);
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
