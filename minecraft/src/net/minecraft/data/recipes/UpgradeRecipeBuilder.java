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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class UpgradeRecipeBuilder {
	private final Ingredient base;
	private final Ingredient addition;
	private final Item result;
	private final Advancement.Builder advancement = Advancement.Builder.advancement();
	private final RecipeSerializer<?> type;

	public UpgradeRecipeBuilder(RecipeSerializer<?> recipeSerializer, Ingredient ingredient, Ingredient ingredient2, Item item) {
		this.type = recipeSerializer;
		this.base = ingredient;
		this.addition = ingredient2;
		this.result = item;
	}

	public static UpgradeRecipeBuilder smithing(Ingredient ingredient, Ingredient ingredient2, Item item) {
		return new UpgradeRecipeBuilder(RecipeSerializer.SMITHING, ingredient, ingredient2, item);
	}

	public UpgradeRecipeBuilder unlocks(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer, String string) {
		this.save(consumer, new ResourceLocation(string));
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement
			.parent(new ResourceLocation("recipes/root"))
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(RequirementsStrategy.OR);
		consumer.accept(
			new UpgradeRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.base,
				this.addition,
				this.result,
				this.advancement,
				new ResourceLocation(resourceLocation.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + resourceLocation.getPath())
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
		private final Ingredient base;
		private final Ingredient addition;
		private final Item result;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final RecipeSerializer<?> type;

		public Result(
			ResourceLocation resourceLocation,
			RecipeSerializer<?> recipeSerializer,
			Ingredient ingredient,
			Ingredient ingredient2,
			Item item,
			Advancement.Builder builder,
			ResourceLocation resourceLocation2
		) {
			this.id = resourceLocation;
			this.type = recipeSerializer;
			this.base = ingredient;
			this.addition = ingredient2;
			this.result = item;
			this.advancement = builder;
			this.advancementId = resourceLocation2;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			jsonObject.add("base", this.base.toJson());
			jsonObject.add("addition", this.addition.toJson());
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", Registry.ITEM.getKey(this.result).toString());
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
