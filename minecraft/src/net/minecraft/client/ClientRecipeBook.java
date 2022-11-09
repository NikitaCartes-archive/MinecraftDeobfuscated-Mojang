package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
	private static final Logger LOGGER = LogUtils.getLogger();
	private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
	private List<RecipeCollection> allCollections = ImmutableList.of();

	public void setupCollections(Iterable<Recipe<?>> iterable) {
		Map<RecipeBookCategories, List<List<Recipe<?>>>> map = categorizeAndGroupRecipes(iterable);
		Map<RecipeBookCategories, List<RecipeCollection>> map2 = Maps.<RecipeBookCategories, List<RecipeCollection>>newHashMap();
		Builder<RecipeCollection> builder = ImmutableList.builder();
		map.forEach(
			(recipeBookCategories, list) -> map2.put(
					recipeBookCategories, (List)list.stream().map(RecipeCollection::new).peek(builder::add).collect(ImmutableList.toImmutableList())
				)
		);
		RecipeBookCategories.AGGREGATE_CATEGORIES
			.forEach(
				(recipeBookCategories, list) -> map2.put(
						recipeBookCategories,
						(List)list.stream()
							.flatMap(recipeBookCategoriesx -> ((List)map2.getOrDefault(recipeBookCategoriesx, ImmutableList.of())).stream())
							.collect(ImmutableList.toImmutableList())
					)
			);
		this.collectionsByTab = ImmutableMap.copyOf(map2);
		this.allCollections = builder.build();
	}

	private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> iterable) {
		Map<RecipeBookCategories, List<List<Recipe<?>>>> map = Maps.<RecipeBookCategories, List<List<Recipe<?>>>>newHashMap();
		Table<RecipeBookCategories, String, List<Recipe<?>>> table = HashBasedTable.create();

		for (Recipe<?> recipe : iterable) {
			if (!recipe.isSpecial() && !recipe.isIncomplete()) {
				RecipeBookCategories recipeBookCategories = getCategory(recipe);
				String string = recipe.getGroup();
				if (string.isEmpty()) {
					((List)map.computeIfAbsent(recipeBookCategories, recipeBookCategoriesx -> Lists.newArrayList())).add(ImmutableList.of(recipe));
				} else {
					List<Recipe<?>> list = table.get(recipeBookCategories, string);
					if (list == null) {
						list = Lists.<Recipe<?>>newArrayList();
						table.put(recipeBookCategories, string, list);
						((List)map.computeIfAbsent(recipeBookCategories, recipeBookCategoriesx -> Lists.newArrayList())).add(list);
					}

					list.add(recipe);
				}
			}
		}

		return map;
	}

	private static RecipeBookCategories getCategory(Recipe<?> recipe) {
		if (recipe instanceof CraftingRecipe craftingRecipe) {
			return switch (craftingRecipe.category()) {
				case BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
				case EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
				case REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
				case MISC -> RecipeBookCategories.CRAFTING_MISC;
			};
		} else {
			RecipeType<?> recipeType = recipe.getType();
			if (recipe instanceof AbstractCookingRecipe abstractCookingRecipe) {
				CookingBookCategory cookingBookCategory = abstractCookingRecipe.category();
				if (recipeType == RecipeType.SMELTING) {
					return switch (cookingBookCategory) {
						case BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
						case FOOD -> RecipeBookCategories.FURNACE_FOOD;
						case MISC -> RecipeBookCategories.FURNACE_MISC;
					};
				}

				if (recipeType == RecipeType.BLASTING) {
					return cookingBookCategory == CookingBookCategory.BLOCKS ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
				}

				if (recipeType == RecipeType.SMOKING) {
					return RecipeBookCategories.SMOKER_FOOD;
				}

				if (recipeType == RecipeType.CAMPFIRE_COOKING) {
					return RecipeBookCategories.CAMPFIRE;
				}
			}

			if (recipeType == RecipeType.STONECUTTING) {
				return RecipeBookCategories.STONECUTTER;
			} else if (recipeType == RecipeType.SMITHING) {
				return RecipeBookCategories.SMITHING;
			} else {
				LOGGER.warn("Unknown recipe category: {}/{}", LogUtils.defer(() -> BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType())), LogUtils.defer(recipe::getId));
				return RecipeBookCategories.UNKNOWN;
			}
		}
	}

	public List<RecipeCollection> getCollections() {
		return this.allCollections;
	}

	public List<RecipeCollection> getCollection(RecipeBookCategories recipeBookCategories) {
		return (List<RecipeCollection>)this.collectionsByTab.getOrDefault(recipeBookCategories, Collections.emptyList());
	}
}
