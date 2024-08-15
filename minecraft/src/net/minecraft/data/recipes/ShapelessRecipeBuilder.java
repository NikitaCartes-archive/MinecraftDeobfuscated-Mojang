package net.minecraft.data.recipes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder implements RecipeBuilder {
	private final HolderGetter<Item> items;
	private final RecipeCategory category;
	private final ItemStack result;
	private final List<Ingredient> ingredients = new ArrayList();
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
	@Nullable
	private String group;

	private ShapelessRecipeBuilder(HolderGetter<Item> holderGetter, RecipeCategory recipeCategory, ItemStack itemStack) {
		this.items = holderGetter;
		this.category = recipeCategory;
		this.result = itemStack;
	}

	public static ShapelessRecipeBuilder shapeless(HolderGetter<Item> holderGetter, RecipeCategory recipeCategory, ItemStack itemStack) {
		return new ShapelessRecipeBuilder(holderGetter, recipeCategory, itemStack);
	}

	public static ShapelessRecipeBuilder shapeless(HolderGetter<Item> holderGetter, RecipeCategory recipeCategory, ItemLike itemLike) {
		return shapeless(holderGetter, recipeCategory, itemLike, 1);
	}

	public static ShapelessRecipeBuilder shapeless(HolderGetter<Item> holderGetter, RecipeCategory recipeCategory, ItemLike itemLike, int i) {
		return new ShapelessRecipeBuilder(holderGetter, recipeCategory, itemLike.asItem().getDefaultInstance().copyWithCount(i));
	}

	public ShapelessRecipeBuilder requires(TagKey<Item> tagKey) {
		return this.requires(Ingredient.of(this.items.getOrThrow(tagKey)));
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

	public ShapelessRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
		this.criteria.put(string, criterion);
		return this;
	}

	public ShapelessRecipeBuilder group(@Nullable String string) {
		this.group = string;
		return this;
	}

	@Override
	public Item getResult() {
		return this.result.getItem();
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		ShapelessRecipe shapelessRecipe = new ShapelessRecipe(
			(String)Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), this.result, this.ingredients
		);
		recipeOutput.accept(resourceLocation, shapelessRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}
}
