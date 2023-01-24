/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientRecipeBook
extends RecipeBook {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
    private List<RecipeCollection> allCollections = ImmutableList.of();

    public void setupCollections(Iterable<Recipe<?>> iterable, RegistryAccess registryAccess) {
        Map<RecipeBookCategories, List<List<Recipe<?>>>> map = ClientRecipeBook.categorizeAndGroupRecipes(iterable);
        HashMap map2 = Maps.newHashMap();
        ImmutableList.Builder builder = ImmutableList.builder();
        map.forEach((recipeBookCategories, list2) -> map2.put(recipeBookCategories, (List)list2.stream().map(list -> new RecipeCollection(registryAccess, (List<Recipe<?>>)list)).peek(builder::add).collect(ImmutableList.toImmutableList())));
        RecipeBookCategories.AGGREGATE_CATEGORIES.forEach((recipeBookCategories2, list) -> map2.put(recipeBookCategories2, (List)list.stream().flatMap(recipeBookCategories -> ((List)map2.getOrDefault(recipeBookCategories, ImmutableList.of())).stream()).collect(ImmutableList.toImmutableList())));
        this.collectionsByTab = ImmutableMap.copyOf(map2);
        this.allCollections = builder.build();
    }

    private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> iterable) {
        HashMap<RecipeBookCategories, List<List<Recipe<?>>>> map = Maps.newHashMap();
        HashBasedTable table = HashBasedTable.create();
        for (Recipe<?> recipe : iterable) {
            if (recipe.isSpecial() || recipe.isIncomplete()) continue;
            RecipeBookCategories recipeBookCategories2 = ClientRecipeBook.getCategory(recipe);
            String string = recipe.getGroup();
            if (string.isEmpty()) {
                map.computeIfAbsent(recipeBookCategories2, recipeBookCategories -> Lists.newArrayList()).add(ImmutableList.of(recipe));
                continue;
            }
            ArrayList<Recipe<?>> list = (ArrayList<Recipe<?>>)table.get((Object)recipeBookCategories2, string);
            if (list == null) {
                list = Lists.newArrayList();
                table.put(recipeBookCategories2, string, list);
                map.computeIfAbsent(recipeBookCategories2, recipeBookCategories -> Lists.newArrayList()).add(list);
            }
            list.add(recipe);
        }
        return map;
    }

    private static RecipeBookCategories getCategory(Recipe<?> recipe) {
        if (recipe instanceof CraftingRecipe) {
            CraftingRecipe craftingRecipe = (CraftingRecipe)recipe;
            return switch (craftingRecipe.category()) {
                default -> throw new IncompatibleClassChangeError();
                case CraftingBookCategory.BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
                case CraftingBookCategory.EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
                case CraftingBookCategory.REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
                case CraftingBookCategory.MISC -> RecipeBookCategories.CRAFTING_MISC;
            };
        }
        RecipeType<?> recipeType = recipe.getType();
        if (recipe instanceof AbstractCookingRecipe) {
            AbstractCookingRecipe abstractCookingRecipe = (AbstractCookingRecipe)recipe;
            CookingBookCategory cookingBookCategory = abstractCookingRecipe.category();
            if (recipeType == RecipeType.SMELTING) {
                return switch (cookingBookCategory) {
                    default -> throw new IncompatibleClassChangeError();
                    case CookingBookCategory.BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
                    case CookingBookCategory.FOOD -> RecipeBookCategories.FURNACE_FOOD;
                    case CookingBookCategory.MISC -> RecipeBookCategories.FURNACE_MISC;
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
        }
        if (recipeType == RecipeType.SMITHING) {
            return RecipeBookCategories.SMITHING;
        }
        LOGGER.warn("Unknown recipe category: {}/{}", LogUtils.defer(() -> BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType())), LogUtils.defer(recipe::getId));
        return RecipeBookCategories.UNKNOWN;
    }

    public List<RecipeCollection> getCollections() {
        return this.allCollections;
    }

    public List<RecipeCollection> getCollection(RecipeBookCategories recipeBookCategories) {
        return this.collectionsByTab.getOrDefault((Object)recipeBookCategories, Collections.emptyList());
    }
}

