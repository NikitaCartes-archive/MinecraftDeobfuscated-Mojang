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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
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
	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		TransmuteRecipe transmuteRecipe = new TransmuteRecipe(
			(String)Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), this.input, this.material, this.result
		);
		recipeOutput.accept(resourceLocation, transmuteRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}
}
