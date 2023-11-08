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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final Item result;
	private final Ingredient ingredient;
	private final int count;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
	@Nullable
	private String group;
	private final SingleItemRecipe.Factory<?> factory;

	public SingleItemRecipeBuilder(RecipeCategory recipeCategory, SingleItemRecipe.Factory<?> factory, Ingredient ingredient, ItemLike itemLike, int i) {
		this.category = recipeCategory;
		this.factory = factory;
		this.result = itemLike.asItem();
		this.ingredient = ingredient;
		this.count = i;
	}

	public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike) {
		return new SingleItemRecipeBuilder(recipeCategory, StonecutterRecipe::new, ingredient, itemLike, 1);
	}

	public static SingleItemRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, int i) {
		return new SingleItemRecipeBuilder(recipeCategory, StonecutterRecipe::new, ingredient, itemLike, i);
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
		SingleItemRecipe singleItemRecipe = this.factory
			.create((String)Objects.requireNonNullElse(this.group, ""), this.ingredient, new ItemStack(this.result, this.count));
		recipeOutput.accept(resourceLocation, singleItemRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}
}
