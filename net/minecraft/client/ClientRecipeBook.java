/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.RecipeBookCategories;
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
import org.apache.logging.log4j.util.Supplier;

@Environment(value=EnvType.CLIENT)
public class ClientRecipeBook
extends RecipeBook {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RecipeManager recipes;
    private final Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = Maps.newHashMap();
    private final List<RecipeCollection> collections = Lists.newArrayList();

    public ClientRecipeBook(RecipeManager recipeManager) {
        this.recipes = recipeManager;
    }

    public void setupCollections() {
        this.collections.clear();
        this.collectionsByTab.clear();
        HashBasedTable<RecipeBookCategories, String, RecipeCollection> table = HashBasedTable.create();
        for (Recipe<?> recipe : this.recipes.getRecipes()) {
            RecipeCollection recipeCollection;
            if (recipe.isSpecial()) continue;
            RecipeBookCategories recipeBookCategories = ClientRecipeBook.getCategory(recipe);
            String string = recipe.getGroup();
            if (string.isEmpty()) {
                recipeCollection = this.createCollection(recipeBookCategories);
            } else {
                recipeCollection = (RecipeCollection)table.get((Object)recipeBookCategories, string);
                if (recipeCollection == null) {
                    recipeCollection = this.createCollection(recipeBookCategories);
                    table.put(recipeBookCategories, string, recipeCollection);
                }
            }
            recipeCollection.add(recipe);
        }
    }

    private RecipeCollection createCollection(RecipeBookCategories recipeBookCategories2) {
        RecipeCollection recipeCollection = new RecipeCollection();
        this.collections.add(recipeCollection);
        this.collectionsByTab.computeIfAbsent(recipeBookCategories2, recipeBookCategories -> Lists.newArrayList()).add(recipeCollection);
        if (recipeBookCategories2 == RecipeBookCategories.FURNACE_BLOCKS || recipeBookCategories2 == RecipeBookCategories.FURNACE_FOOD || recipeBookCategories2 == RecipeBookCategories.FURNACE_MISC) {
            this.addToCollection(RecipeBookCategories.FURNACE_SEARCH, recipeCollection);
        } else if (recipeBookCategories2 == RecipeBookCategories.BLAST_FURNACE_BLOCKS || recipeBookCategories2 == RecipeBookCategories.BLAST_FURNACE_MISC) {
            this.addToCollection(RecipeBookCategories.BLAST_FURNACE_SEARCH, recipeCollection);
        } else if (recipeBookCategories2 == RecipeBookCategories.SMOKER_FOOD) {
            this.addToCollection(RecipeBookCategories.SMOKER_SEARCH, recipeCollection);
        } else if (recipeBookCategories2 == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS || recipeBookCategories2 == RecipeBookCategories.CRAFTING_REDSTONE || recipeBookCategories2 == RecipeBookCategories.CRAFTING_EQUIPMENT || recipeBookCategories2 == RecipeBookCategories.CRAFTING_MISC) {
            this.addToCollection(RecipeBookCategories.SEARCH, recipeCollection);
        }
        return recipeCollection;
    }

    private void addToCollection(RecipeBookCategories recipeBookCategories2, RecipeCollection recipeCollection) {
        this.collectionsByTab.computeIfAbsent(recipeBookCategories2, recipeBookCategories -> Lists.newArrayList()).add(recipeCollection);
    }

    private static RecipeBookCategories getCategory(Recipe<?> recipe) {
        RecipeType<?> recipeType = recipe.getType();
        if (recipeType == RecipeType.CRAFTING) {
            ItemStack itemStack = recipe.getResultItem();
            CreativeModeTab creativeModeTab = itemStack.getItem().getItemCategory();
            if (creativeModeTab == CreativeModeTab.TAB_BUILDING_BLOCKS) {
                return RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
            }
            if (creativeModeTab == CreativeModeTab.TAB_TOOLS || creativeModeTab == CreativeModeTab.TAB_COMBAT) {
                return RecipeBookCategories.CRAFTING_EQUIPMENT;
            }
            if (creativeModeTab == CreativeModeTab.TAB_REDSTONE) {
                return RecipeBookCategories.CRAFTING_REDSTONE;
            }
            return RecipeBookCategories.CRAFTING_MISC;
        }
        if (recipeType == RecipeType.SMELTING) {
            if (recipe.getResultItem().getItem().isEdible()) {
                return RecipeBookCategories.FURNACE_FOOD;
            }
            if (recipe.getResultItem().getItem() instanceof BlockItem) {
                return RecipeBookCategories.FURNACE_BLOCKS;
            }
            return RecipeBookCategories.FURNACE_MISC;
        }
        if (recipeType == RecipeType.BLASTING) {
            if (recipe.getResultItem().getItem() instanceof BlockItem) {
                return RecipeBookCategories.BLAST_FURNACE_BLOCKS;
            }
            return RecipeBookCategories.BLAST_FURNACE_MISC;
        }
        if (recipeType == RecipeType.SMOKING) {
            return RecipeBookCategories.SMOKER_FOOD;
        }
        if (recipeType == RecipeType.STONECUTTING) {
            return RecipeBookCategories.STONECUTTER;
        }
        if (recipeType == RecipeType.CAMPFIRE_COOKING) {
            return RecipeBookCategories.CAMPFIRE;
        }
        if (recipeType == RecipeType.SMITHING) {
            return RecipeBookCategories.SMITHING;
        }
        Supplier[] supplierArray = new Supplier[2];
        supplierArray[0] = () -> Registry.RECIPE_TYPE.getKey(recipe.getType());
        supplierArray[1] = recipe::getId;
        LOGGER.warn("Unknown recipe category: {}/{}", supplierArray);
        return RecipeBookCategories.UNKNOWN;
    }

    public static List<RecipeBookCategories> getCategories(RecipeBookMenu<?> recipeBookMenu) {
        if (recipeBookMenu instanceof CraftingMenu || recipeBookMenu instanceof InventoryMenu) {
            return Lists.newArrayList(RecipeBookCategories.SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
        }
        if (recipeBookMenu instanceof FurnaceMenu) {
            return Lists.newArrayList(RecipeBookCategories.FURNACE_SEARCH, RecipeBookCategories.FURNACE_FOOD, RecipeBookCategories.FURNACE_BLOCKS, RecipeBookCategories.FURNACE_MISC);
        }
        if (recipeBookMenu instanceof BlastFurnaceMenu) {
            return Lists.newArrayList(RecipeBookCategories.BLAST_FURNACE_SEARCH, RecipeBookCategories.BLAST_FURNACE_BLOCKS, RecipeBookCategories.BLAST_FURNACE_MISC);
        }
        if (recipeBookMenu instanceof SmokerMenu) {
            return Lists.newArrayList(RecipeBookCategories.SMOKER_SEARCH, RecipeBookCategories.SMOKER_FOOD);
        }
        return Lists.newArrayList();
    }

    public List<RecipeCollection> getCollections() {
        return this.collections;
    }

    public List<RecipeCollection> getCollection(RecipeBookCategories recipeBookCategories) {
        return this.collectionsByTab.getOrDefault((Object)recipeBookCategories, Collections.emptyList());
    }
}

