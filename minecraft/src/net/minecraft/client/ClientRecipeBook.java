package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.Registry;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
	private static final Logger LOGGER = LogManager.getLogger();
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
		RecipeType<?> recipeType = recipe.getType();
		if (recipeType == RecipeType.CRAFTING) {
			ItemStack itemStack = recipe.getResultItem();
			CreativeModeTab creativeModeTab = itemStack.getItem().getItemCategory();
			if (creativeModeTab == CreativeModeTab.TAB_BUILDING_BLOCKS) {
				return RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
			} else if (creativeModeTab == CreativeModeTab.TAB_TOOLS || creativeModeTab == CreativeModeTab.TAB_COMBAT) {
				return RecipeBookCategories.CRAFTING_EQUIPMENT;
			} else {
				return creativeModeTab == CreativeModeTab.TAB_REDSTONE ? RecipeBookCategories.CRAFTING_REDSTONE : RecipeBookCategories.CRAFTING_MISC;
			}
		} else if (recipeType == RecipeType.SMELTING) {
			if (recipe.getResultItem().getItem().isEdible()) {
				return RecipeBookCategories.FURNACE_FOOD;
			} else {
				return recipe.getResultItem().getItem() instanceof BlockItem ? RecipeBookCategories.FURNACE_BLOCKS : RecipeBookCategories.FURNACE_MISC;
			}
		} else if (recipeType == RecipeType.BLASTING) {
			return recipe.getResultItem().getItem() instanceof BlockItem ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
		} else if (recipeType == RecipeType.SMOKING) {
			return RecipeBookCategories.SMOKER_FOOD;
		} else if (recipeType == RecipeType.STONECUTTING) {
			return RecipeBookCategories.STONECUTTER;
		} else if (recipeType == RecipeType.CAMPFIRE_COOKING) {
			return RecipeBookCategories.CAMPFIRE;
		} else if (recipeType == RecipeType.SMITHING) {
			return RecipeBookCategories.SMITHING;
		} else {
			LOGGER.warn("Unknown recipe category: {}/{}", () -> Registry.RECIPE_TYPE.getKey(recipe.getType()), recipe::getId);
			return RecipeBookCategories.UNKNOWN;
		}
	}

	public List<RecipeCollection> getCollections() {
		return this.allCollections;
	}

	public List<RecipeCollection> getCollection(RecipeBookCategories recipeBookCategories) {
		return (List<RecipeCollection>)this.collectionsByTab.getOrDefault(recipeBookCategories, Collections.emptyList());
	}
}
