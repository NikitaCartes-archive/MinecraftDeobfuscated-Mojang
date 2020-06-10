package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.Registry;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RecipeManager recipes;
	private final Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = Maps.<RecipeBookCategories, List<RecipeCollection>>newHashMap();
	private final List<RecipeCollection> collections = Lists.<RecipeCollection>newArrayList();

	public ClientRecipeBook(RecipeManager recipeManager) {
		this.recipes = recipeManager;
	}

	public void setupCollections() {
		this.collections.clear();
		this.collectionsByTab.clear();
		Table<RecipeBookCategories, String, RecipeCollection> table = HashBasedTable.create();

		for (Recipe<?> recipe : this.recipes.getRecipes()) {
			if (!recipe.isSpecial()) {
				RecipeBookCategories recipeBookCategories = getCategory(recipe);
				String string = recipe.getGroup();
				RecipeCollection recipeCollection;
				if (string.isEmpty()) {
					recipeCollection = this.createCollection(recipeBookCategories);
				} else {
					recipeCollection = table.get(recipeBookCategories, string);
					if (recipeCollection == null) {
						recipeCollection = this.createCollection(recipeBookCategories);
						table.put(recipeBookCategories, string, recipeCollection);
					}
				}

				recipeCollection.add(recipe);
			}
		}
	}

	private RecipeCollection createCollection(RecipeBookCategories recipeBookCategories) {
		RecipeCollection recipeCollection = new RecipeCollection();
		this.collections.add(recipeCollection);
		((List)this.collectionsByTab.computeIfAbsent(recipeBookCategories, recipeBookCategoriesx -> Lists.newArrayList())).add(recipeCollection);
		if (recipeBookCategories == RecipeBookCategories.FURNACE_BLOCKS
			|| recipeBookCategories == RecipeBookCategories.FURNACE_FOOD
			|| recipeBookCategories == RecipeBookCategories.FURNACE_MISC) {
			this.addToCollection(RecipeBookCategories.FURNACE_SEARCH, recipeCollection);
		} else if (recipeBookCategories == RecipeBookCategories.BLAST_FURNACE_BLOCKS || recipeBookCategories == RecipeBookCategories.BLAST_FURNACE_MISC) {
			this.addToCollection(RecipeBookCategories.BLAST_FURNACE_SEARCH, recipeCollection);
		} else if (recipeBookCategories == RecipeBookCategories.SMOKER_FOOD) {
			this.addToCollection(RecipeBookCategories.SMOKER_SEARCH, recipeCollection);
		} else if (recipeBookCategories == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS
			|| recipeBookCategories == RecipeBookCategories.CRAFTING_REDSTONE
			|| recipeBookCategories == RecipeBookCategories.CRAFTING_EQUIPMENT
			|| recipeBookCategories == RecipeBookCategories.CRAFTING_MISC) {
			this.addToCollection(RecipeBookCategories.SEARCH, recipeCollection);
		}

		return recipeCollection;
	}

	private void addToCollection(RecipeBookCategories recipeBookCategories, RecipeCollection recipeCollection) {
		((List)this.collectionsByTab.computeIfAbsent(recipeBookCategories, recipeBookCategoriesx -> Lists.newArrayList())).add(recipeCollection);
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

	public static List<RecipeBookCategories> getCategories(RecipeBookMenu<?> recipeBookMenu) {
		if (recipeBookMenu instanceof CraftingMenu || recipeBookMenu instanceof InventoryMenu) {
			return Lists.<RecipeBookCategories>newArrayList(
				RecipeBookCategories.SEARCH,
				RecipeBookCategories.CRAFTING_EQUIPMENT,
				RecipeBookCategories.CRAFTING_BUILDING_BLOCKS,
				RecipeBookCategories.CRAFTING_MISC,
				RecipeBookCategories.CRAFTING_REDSTONE
			);
		} else if (recipeBookMenu instanceof FurnaceMenu) {
			return Lists.<RecipeBookCategories>newArrayList(
				RecipeBookCategories.FURNACE_SEARCH, RecipeBookCategories.FURNACE_FOOD, RecipeBookCategories.FURNACE_BLOCKS, RecipeBookCategories.FURNACE_MISC
			);
		} else if (recipeBookMenu instanceof BlastFurnaceMenu) {
			return Lists.<RecipeBookCategories>newArrayList(
				RecipeBookCategories.BLAST_FURNACE_SEARCH, RecipeBookCategories.BLAST_FURNACE_BLOCKS, RecipeBookCategories.BLAST_FURNACE_MISC
			);
		} else {
			return recipeBookMenu instanceof SmokerMenu
				? Lists.<RecipeBookCategories>newArrayList(RecipeBookCategories.SMOKER_SEARCH, RecipeBookCategories.SMOKER_FOOD)
				: Lists.<RecipeBookCategories>newArrayList();
		}
	}

	public List<RecipeCollection> getCollections() {
		return this.collections;
	}

	public List<RecipeCollection> getCollection(RecipeBookCategories recipeBookCategories) {
		return (List<RecipeCollection>)this.collectionsByTab.getOrDefault(recipeBookCategories, Collections.emptyList());
	}
}
