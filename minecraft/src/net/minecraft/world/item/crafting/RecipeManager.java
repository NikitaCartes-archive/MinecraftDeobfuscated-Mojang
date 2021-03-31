package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger LOGGER = LogManager.getLogger();
	private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ImmutableMap.of();
	private boolean hasErrors;

	public RecipeManager() {
		super(GSON, "recipes");
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.hasErrors = false;
		Map<RecipeType<?>, Builder<ResourceLocation, Recipe<?>>> map2 = Maps.<RecipeType<?>, Builder<ResourceLocation, Recipe<?>>>newHashMap();

		for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();

			try {
				Recipe<?> recipe = fromJson(resourceLocation, GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), "top element"));
				((Builder)map2.computeIfAbsent(recipe.getType(), recipeType -> ImmutableMap.builder())).put(resourceLocation, recipe);
			} catch (IllegalArgumentException | JsonParseException var9) {
				LOGGER.error("Parsing error loading recipe {}", resourceLocation, var9);
			}
		}

		this.recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>)map2.entrySet()
			.stream()
			.collect(ImmutableMap.toImmutableMap(Entry::getKey, entryx -> ((Builder)entryx.getValue()).build()));
		LOGGER.info("Loaded {} recipes", map2.size());
	}

	public boolean hadErrorsLoading() {
		return this.hasErrors;
	}

	public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
		return this.byType(recipeType).values().stream().flatMap(recipe -> Util.toStream(recipeType.tryMatch(recipe, level, container))).findFirst();
	}

	public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> recipeType) {
		return (List<T>)this.byType(recipeType).values().stream().map(recipe -> recipe).collect(Collectors.toList());
	}

	public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
		return (List<T>)this.byType(recipeType)
			.values()
			.stream()
			.flatMap(recipe -> Util.toStream(recipeType.tryMatch(recipe, level, container)))
			.sorted(Comparator.comparing(recipe -> recipe.getResultItem().getDescriptionId()))
			.collect(Collectors.toList());
	}

	private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> byType(RecipeType<T> recipeType) {
		return (Map<ResourceLocation, Recipe<C>>)this.recipes.getOrDefault(recipeType, Collections.emptyMap());
	}

	public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> recipeType, C container, Level level) {
		Optional<T> optional = this.getRecipeFor(recipeType, container, level);
		if (optional.isPresent()) {
			return ((Recipe)optional.get()).getRemainingItems(container);
		} else {
			NonNullList<ItemStack> nonNullList = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

			for (int i = 0; i < nonNullList.size(); i++) {
				nonNullList.set(i, container.getItem(i));
			}

			return nonNullList;
		}
	}

	public Optional<? extends Recipe<?>> byKey(ResourceLocation resourceLocation) {
		return this.recipes.values().stream().map(map -> (Recipe)map.get(resourceLocation)).filter(Objects::nonNull).findFirst();
	}

	public Collection<Recipe<?>> getRecipes() {
		return (Collection<Recipe<?>>)this.recipes.values().stream().flatMap(map -> map.values().stream()).collect(Collectors.toSet());
	}

	public Stream<ResourceLocation> getRecipeIds() {
		return this.recipes.values().stream().flatMap(map -> map.keySet().stream());
	}

	public static Recipe<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "type");
		return ((RecipeSerializer)Registry.RECIPE_SERIALIZER
				.getOptional(new ResourceLocation(string))
				.orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'")))
			.fromJson(resourceLocation, jsonObject);
	}

	public void replaceRecipes(Iterable<Recipe<?>> iterable) {
		this.hasErrors = false;
		Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = Maps.<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>newHashMap();
		iterable.forEach(recipe -> {
			Map<ResourceLocation, Recipe<?>> map2 = (Map<ResourceLocation, Recipe<?>>)map.computeIfAbsent(recipe.getType(), recipeType -> Maps.newHashMap());
			Recipe<?> recipe2 = (Recipe<?>)map2.put(recipe.getId(), recipe);
			if (recipe2 != null) {
				throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
			}
		});
		this.recipes = ImmutableMap.copyOf(map);
	}
}
