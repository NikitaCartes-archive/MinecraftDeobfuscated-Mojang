package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final Item result;
	private final Ingredient ingredient;
	private final int count;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
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

	public SingleItemRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
		this.criteria.put(string, criterion);
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
	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		recipeOutput.accept(
			new SingleItemRecipeBuilder.Result(
				resourceLocation,
				this.type,
				this.group == null ? "" : this.group,
				this.ingredient,
				this.result,
				this.count,
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
		ResourceLocation id, RecipeSerializer<?> type, String group, Ingredient ingredient, Item result, int count, AdvancementHolder advancement
	) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			if (!this.group.isEmpty()) {
				jsonObject.addProperty("group", this.group);
			}

			jsonObject.add("ingredient", this.ingredient.toJson(false));
			jsonObject.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
			jsonObject.addProperty("count", this.count);
		}
	}
}
