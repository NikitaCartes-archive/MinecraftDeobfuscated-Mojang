package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShapelessRecipeBuilder {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Item result;
	private final int count;
	private final List<Ingredient> ingredients = Lists.<Ingredient>newArrayList();
	private final Advancement.Builder advancement = Advancement.Builder.advancement();
	private String group;

	public ShapelessRecipeBuilder(ItemLike itemLike, int i) {
		this.result = itemLike.asItem();
		this.count = i;
	}

	public static ShapelessRecipeBuilder shapeless(ItemLike itemLike) {
		return new ShapelessRecipeBuilder(itemLike, 1);
	}

	public static ShapelessRecipeBuilder shapeless(ItemLike itemLike, int i) {
		return new ShapelessRecipeBuilder(itemLike, i);
	}

	public ShapelessRecipeBuilder requires(Tag<Item> tag) {
		return this.requires(Ingredient.of(tag));
	}

	public ShapelessRecipeBuilder requires(ItemLike itemLike) {
		return this.requires(itemLike, 1);
	}

	public ShapelessRecipeBuilder requires(ItemLike itemLike, int i) {
		for (int j = 0; j < i; j++) {
			this.requires(Ingredient.of(itemLike));
		}

		return this;
	}

	public ShapelessRecipeBuilder requires(Ingredient ingredient) {
		return this.requires(ingredient, 1);
	}

	public ShapelessRecipeBuilder requires(Ingredient ingredient, int i) {
		for (int j = 0; j < i; j++) {
			this.ingredients.add(ingredient);
		}

		return this;
	}

	public ShapelessRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	public ShapelessRecipeBuilder group(String string) {
		this.group = string;
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer) {
		this.save(consumer, Registry.ITEM.getKey(this.result));
	}

	public void save(Consumer<FinishedRecipe> consumer, String string) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(this.result);
		if (new ResourceLocation(string).equals(resourceLocation)) {
			throw new IllegalStateException("Shapeless Recipe " + string + " should remove its 'save' argument");
		} else {
			this.save(consumer, new ResourceLocation(string));
		}
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement
			.parent(new ResourceLocation("recipes/root"))
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(RequirementsStrategy.OR);
		consumer.accept(
			new ShapelessRecipeBuilder.Result(
				resourceLocation,
				this.result,
				this.count,
				this.group == null ? "" : this.group,
				this.ingredients,
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
		private final Item result;
		private final int count;
		private final String group;
		private final List<Ingredient> ingredients;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;

		public Result(
			ResourceLocation resourceLocation, Item item, int i, String string, List<Ingredient> list, Advancement.Builder builder, ResourceLocation resourceLocation2
		) {
			this.id = resourceLocation;
			this.result = item;
			this.count = i;
			this.group = string;
			this.ingredients = list;
			this.advancement = builder;
			this.advancementId = resourceLocation2;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			if (!this.group.isEmpty()) {
				jsonObject.addProperty("group", this.group);
			}

			JsonArray jsonArray = new JsonArray();

			for (Ingredient ingredient : this.ingredients) {
				jsonArray.add(ingredient.toJson());
			}

			jsonObject.add("ingredients", jsonArray);
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", Registry.ITEM.getKey(this.result).toString());
			if (this.count > 1) {
				jsonObject2.addProperty("count", this.count);
			}

			jsonObject.add("result", jsonObject2);
		}

		@Override
		public RecipeSerializer<?> getType() {
			return RecipeSerializer.SHAPELESS_RECIPE;
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
