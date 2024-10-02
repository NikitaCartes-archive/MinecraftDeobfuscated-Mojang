package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;

public class TransmuteRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final Holder<Item> result;
	private final Ingredient input;
	private final Ingredient material;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
	@Nullable
	private String group;

	private TransmuteRecipeBuilder(RecipeCategory recipeCategory, Holder<Item> holder, Ingredient ingredient, Ingredient ingredient2) {
		this.category = recipeCategory;
		this.result = holder;
		this.input = ingredient;
		this.material = ingredient2;
	}

	public static TransmuteRecipeBuilder transmute(RecipeCategory recipeCategory, Ingredient ingredient, Ingredient ingredient2, Item item) {
		return new TransmuteRecipeBuilder(recipeCategory, item.builtInRegistryHolder(), ingredient, ingredient2);
	}

	public TransmuteRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
		this.criteria.put(string, criterion);
		return this;
	}

	public TransmuteRecipeBuilder group(@Nullable String string) {
		this.group = string;
		return this;
	}

	@Override
	public Item getResult() {
		return this.result.value();
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> resourceKey) {
		this.ensureValid(resourceKey);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey))
			.rewards(AdvancementRewards.Builder.recipe(resourceKey))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		TransmuteRecipe transmuteRecipe = new TransmuteRecipe(
			(String)Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), this.input, this.material, this.result
		);
		recipeOutput.accept(resourceKey, transmuteRecipe, builder.build(resourceKey.location().withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceKey<Recipe<?>> resourceKey) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceKey.location());
		}
	}
}
