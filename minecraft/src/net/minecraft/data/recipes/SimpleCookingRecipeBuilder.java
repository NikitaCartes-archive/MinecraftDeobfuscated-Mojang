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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final CookingBookCategory bookCategory;
	private final Item result;
	private final Ingredient ingredient;
	private final float experience;
	private final int cookingTime;
	private final Advancement.Builder advancement = Advancement.Builder.advancement();
	@Nullable
	private String group;
	private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

	private SimpleCookingRecipeBuilder(
		RecipeCategory recipeCategory,
		CookingBookCategory cookingBookCategory,
		ItemLike itemLike,
		Ingredient ingredient,
		float f,
		int i,
		RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer
	) {
		this.category = recipeCategory;
		this.bookCategory = cookingBookCategory;
		this.result = itemLike.asItem();
		this.ingredient = ingredient;
		this.experience = f;
		this.cookingTime = i;
		this.serializer = recipeSerializer;
	}

	public static SimpleCookingRecipeBuilder generic(
		Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer
	) {
		return new SimpleCookingRecipeBuilder(recipeCategory, determineRecipeCategory(recipeSerializer, itemLike), itemLike, ingredient, f, i, recipeSerializer);
	}

	public static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, CookingBookCategory.FOOD, itemLike, ingredient, f, i, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
	}

	public static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, determineBlastingRecipeCategory(itemLike), itemLike, ingredient, f, i, RecipeSerializer.BLASTING_RECIPE);
	}

	public static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, determineSmeltingRecipeCategory(itemLike), itemLike, ingredient, f, i, RecipeSerializer.SMELTING_RECIPE);
	}

	public static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, CookingBookCategory.FOOD, itemLike, ingredient, f, i, RecipeSerializer.SMOKING_RECIPE);
	}

	public SimpleCookingRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public SimpleCookingRecipeBuilder group(@Nullable String string) {
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
			new SimpleCookingRecipeBuilder.Result(
				resourceLocation,
				this.group == null ? "" : this.group,
				this.bookCategory,
				this.ingredient,
				this.result,
				this.experience,
				this.cookingTime,
				this.advancement,
				resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/"),
				this.serializer
			)
		);
	}

	private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike itemLike) {
		if (itemLike.asItem().isEdible()) {
			return CookingBookCategory.FOOD;
		} else {
			return itemLike.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
		}
	}

	private static CookingBookCategory determineBlastingRecipeCategory(ItemLike itemLike) {
		return itemLike.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
	}

	private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, ItemLike itemLike) {
		if (recipeSerializer == RecipeSerializer.SMELTING_RECIPE) {
			return determineSmeltingRecipeCategory(itemLike);
		} else if (recipeSerializer == RecipeSerializer.BLASTING_RECIPE) {
			return determineBlastingRecipeCategory(itemLike);
		} else if (recipeSerializer != RecipeSerializer.SMOKING_RECIPE && recipeSerializer != RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
			throw new IllegalStateException("Unknown cooking recipe type");
		} else {
			return CookingBookCategory.FOOD;
		}
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.advancement.getCriteria().isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}

	static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final String group;
		private final CookingBookCategory category;
		private final Ingredient ingredient;
		private final Item result;
		private final float experience;
		private final int cookingTime;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

		public Result(
			ResourceLocation resourceLocation,
			String string,
			CookingBookCategory cookingBookCategory,
			Ingredient ingredient,
			Item item,
			float f,
			int i,
			Advancement.Builder builder,
			ResourceLocation resourceLocation2,
			RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer
		) {
			this.id = resourceLocation;
			this.group = string;
			this.category = cookingBookCategory;
			this.ingredient = ingredient;
			this.result = item;
			this.experience = f;
			this.cookingTime = i;
			this.advancement = builder;
			this.advancementId = resourceLocation2;
			this.serializer = recipeSerializer;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			if (!this.group.isEmpty()) {
				jsonObject.addProperty("group", this.group);
			}

			jsonObject.addProperty("category", this.category.getSerializedName());
			jsonObject.add("ingredient", this.ingredient.toJson());
			jsonObject.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
			jsonObject.addProperty("experience", this.experience);
			jsonObject.addProperty("cookingtime", this.cookingTime);
		}

		@Override
		public RecipeSerializer<?> getType() {
			return this.serializer;
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
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
