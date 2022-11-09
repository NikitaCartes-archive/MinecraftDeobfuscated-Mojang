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
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final Item result;
	private final Ingredient ingredient;
	private final int count;
	private final Advancement.Builder advancement = Advancement.Builder.advancement();
	@Nullable
	private String group;
	private final RecipeSerializer<?> type;

	public SingleItemRecipeBuilder(RecipeCategory recipeCategory, RecipeSerializer<?> recipeSerializer, Ingredient ingredient, ItemLike itemLike, int i) {
		this.category = recipeCategory;
		this.type = recipeSerializer;
		this.result = itemLike.asItem();
		this.ingredient = ingredient;
		this.count = i;
	}

	public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike) {
		return new SingleItemRecipeBuilder(recipeCategory, RecipeSerializer.STONECUTTER, ingredient, itemLike, 1);
	}

	public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, int i) {
		return new SingleItemRecipeBuilder(recipeCategory, RecipeSerializer.STONECUTTER, ingredient, itemLike, i);
	}

	public SingleItemRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public SingleItemRecipeBuilder group(@Nullable String string) {
		this.group = string;
		return this;
	}

	@Override
	public Item getResult() {
		return this.result;
	}

	@Override
	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement
			.parent(ROOT_RECIPE_ADVANCEMENT)
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(RequirementsStrategy.OR);
		consumer.accept(
			new SingleItemRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.group == null ? "" : this.group,
				this.ingredient,
				this.result,
				this.count,
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

	public static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final String group;
		private final Ingredient ingredient;
		private final Item result;
		private final int count;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final RecipeSerializer<?> type;

		public Result(
			ResourceLocation resourceLocation,
			RecipeSerializer<?> recipeSerializer,
			String string,
			Ingredient ingredient,
			Item item,
			int i,
			Advancement.Builder builder,
			ResourceLocation resourceLocation2
		) {
			this.id = resourceLocation;
			this.type = recipeSerializer;
			this.group = string;
			this.ingredient = ingredient;
			this.result = item;
			this.count = i;
			this.advancement = builder;
			this.advancementId = resourceLocation2;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			if (!this.group.isEmpty()) {
				jsonObject.addProperty("group", this.group);
			}

			jsonObject.add("ingredient", this.ingredient.toJson());
			jsonObject.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
			jsonObject.addProperty("count", this.count);
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
