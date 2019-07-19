/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeManager
extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ImmutableMap.of();
    private boolean hasErrors;

    public RecipeManager() {
        super(GSON, "recipes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.hasErrors = false;
        HashMap<RecipeType, ImmutableMap.Builder> map2 = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, JsonObject> entry2 : map.entrySet()) {
            ResourceLocation resourceLocation = entry2.getKey();
            try {
                Recipe<?> recipe = RecipeManager.fromJson(resourceLocation, entry2.getValue());
                map2.computeIfAbsent(recipe.getType(), recipeType -> ImmutableMap.builder()).put(resourceLocation, recipe);
            } catch (JsonParseException | IllegalArgumentException runtimeException) {
                LOGGER.error("Parsing error loading recipe {}", (Object)resourceLocation, (Object)runtimeException);
            }
        }
        this.recipes = map2.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> ((ImmutableMap.Builder)entry.getValue()).build()));
        LOGGER.info("Loaded {} recipes", (Object)map2.size());
    }

    public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
        return this.byType(recipeType).values().stream().flatMap(recipe -> Util.toStream(recipeType.tryMatch(recipe, level, container))).findFirst();
    }

    public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
        return this.byType(recipeType).values().stream().flatMap(recipe -> Util.toStream(recipeType.tryMatch(recipe, level, container))).sorted(Comparator.comparing(recipe -> recipe.getResultItem().getDescriptionId())).collect(Collectors.toList());
    }

    private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> byType(RecipeType<T> recipeType) {
        return this.recipes.getOrDefault(recipeType, Collections.emptyMap());
    }

    public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> recipeType, C container, Level level) {
        Optional<T> optional = this.getRecipeFor(recipeType, container, level);
        if (optional.isPresent()) {
            return ((Recipe)optional.get()).getRemainingItems(container);
        }
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            nonNullList.set(i, container.getItem(i));
        }
        return nonNullList;
    }

    public Optional<? extends Recipe<?>> byKey(ResourceLocation resourceLocation) {
        return this.recipes.values().stream().map(map -> (Recipe)map.get(resourceLocation)).filter(Objects::nonNull).findFirst();
    }

    public Collection<Recipe<?>> getRecipes() {
        return this.recipes.values().stream().flatMap(map -> map.values().stream()).collect(Collectors.toSet());
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return this.recipes.values().stream().flatMap(map -> map.keySet().stream());
    }

    public static Recipe<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        String string = GsonHelper.getAsString(jsonObject, "type");
        return Registry.RECIPE_SERIALIZER.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'")).fromJson(resourceLocation, jsonObject);
    }

    @Environment(value=EnvType.CLIENT)
    public void replaceRecipes(Iterable<Recipe<?>> iterable) {
        this.hasErrors = false;
        HashMap map = Maps.newHashMap();
        iterable.forEach(recipe -> {
            Map map2 = map.computeIfAbsent(recipe.getType(), recipeType -> Maps.newHashMap());
            Recipe recipe2 = map2.put(recipe.getId(), recipe);
            if (recipe2 != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
            }
        });
        this.recipes = ImmutableMap.copyOf(map);
    }
}

