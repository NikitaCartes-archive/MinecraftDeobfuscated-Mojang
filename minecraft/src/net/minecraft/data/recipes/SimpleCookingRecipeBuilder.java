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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final CookingBookCategory bookCategory;
	private final Item result;
	private final Ingredient ingredient;
	private final float experience;
	private final int cookingTime;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
	@Nullable
	private String group;
	private final AbstractCookingRecipe.Factory<?> factory;

	private SimpleCookingRecipeBuilder(
		RecipeCategory recipeCategory,
		CookingBookCategory cookingBookCategory,
		ItemLike itemLike,
		Ingredient ingredient,
		float f,
		int i,
		AbstractCookingRecipe.Factory<?> factory
	) {
		this.category = recipeCategory;
		this.bookCategory = cookingBookCategory;
		this.result = itemLike.asItem();
		this.ingredient = ingredient;
		this.experience = f;
		this.cookingTime = i;
		this.factory = factory;
	}

	public static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(
		Ingredient ingredient,
		RecipeCategory recipeCategory,
		ItemLike itemLike,
		float f,
		int i,
		RecipeSerializer<T> recipeSerializer,
		AbstractCookingRecipe.Factory<T> factory
	) {
		return new SimpleCookingRecipeBuilder(recipeCategory, determineRecipeCategory(recipeSerializer, itemLike), itemLike, ingredient, f, i, factory);
	}

	public static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, CookingBookCategory.FOOD, itemLike, ingredient, f, i, CampfireCookingRecipe::new);
	}

	public static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, determineBlastingRecipeCategory(itemLike), itemLike, ingredient, f, i, BlastingRecipe::new);
	}

	public static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, determineSmeltingRecipeCategory(itemLike), itemLike, ingredient, f, i, SmeltingRecipe::new);
	}

	public static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
		return new SimpleCookingRecipeBuilder(recipeCategory, CookingBookCategory.FOOD, itemLike, ingredient, f, i, SmokingRecipe::new);
	}

	public SimpleCookingRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
		this.criteria.put(string, criterion);
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
	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement()
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation))
			.rewards(AdvancementRewards.Builder.recipe(resourceLocation))
			.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(builder::addCriterion);
		AbstractCookingRecipe abstractCookingRecipe = this.factory
			.create(
				(String)Objects.requireNonNullElse(this.group, ""), this.bookCategory, this.ingredient, new ItemStack(this.result), this.experience, this.cookingTime
			);
		recipeOutput.accept(resourceLocation, abstractCookingRecipe, builder.build(resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
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
		if (this.criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}
}
