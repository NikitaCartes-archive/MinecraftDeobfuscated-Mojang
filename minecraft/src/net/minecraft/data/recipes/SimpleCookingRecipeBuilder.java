package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder {
	private final Item result;
	private final Ingredient ingredient;
	private final float experience;
	private final int cookingTime;
	private final Advancement.Builder advancement = Advancement.Builder.advancement();
	private String group;
	private final SimpleCookingSerializer<?> serializer;

	private SimpleCookingRecipeBuilder(ItemLike itemLike, Ingredient ingredient, float f, int i, SimpleCookingSerializer<?> simpleCookingSerializer) {
		this.result = itemLike.asItem();
		this.ingredient = ingredient;
		this.experience = f;
		this.cookingTime = i;
		this.serializer = simpleCookingSerializer;
	}

	public static SimpleCookingRecipeBuilder cooking(Ingredient ingredient, ItemLike itemLike, float f, int i, SimpleCookingSerializer<?> simpleCookingSerializer) {
		return new SimpleCookingRecipeBuilder(itemLike, ingredient, f, i, simpleCookingSerializer);
	}

	public static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, ItemLike itemLike, float f, int i) {
		return cooking(ingredient, itemLike, f, i, RecipeSerializer.BLASTING_RECIPE);
	}

	public static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, ItemLike itemLike, float f, int i) {
		return cooking(ingredient, itemLike, f, i, RecipeSerializer.SMELTING_RECIPE);
	}

	public SimpleCookingRecipeBuilder unlocks(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer) {
		this.save(consumer, Registry.ITEM.getKey(this.result));
	}

	public void save(Consumer<FinishedRecipe> consumer, String string) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(this.result);
		ResourceLocation resourceLocation2 = new ResourceLocation(string);
		if (resourceLocation2.equals(resourceLocation)) {
			throw new IllegalStateException("Recipe " + resourceLocation2 + " should remove its 'save' argument");
		} else {
			this.save(consumer, resourceLocation2);
		}
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement
			.parent(new ResourceLocation("recipes/root"))
			.addCriterion("has_the_recipe", new RecipeUnlockedTrigger.TriggerInstance(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(RequirementsStrategy.OR);
		consumer.accept(
			new SimpleCookingRecipeBuilder.Result(
				resourceLocation,
				this.group == null ? "" : this.group,
				this.ingredient,
				this.result,
				this.experience,
				this.cookingTime,
				this.advancement,
				new ResourceLocation(resourceLocation.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + resourceLocation.getPath()),
				(RecipeSerializer<? extends AbstractCookingRecipe>)this.serializer
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
		private final float experience;
		private final int cookingTime;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

		public Result(
			ResourceLocation resourceLocation,
			String string,
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

			jsonObject.add("ingredient", this.ingredient.toJson());
			jsonObject.addProperty("result", Registry.ITEM.getKey(this.result).toString());
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
