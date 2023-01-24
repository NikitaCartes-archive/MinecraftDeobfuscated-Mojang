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
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger LOGGER = LogUtils.getLogger();
	private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ImmutableMap.of();
	private Map<ResourceLocation, Recipe<?>> byName = ImmutableMap.of();
	private boolean hasErrors;

	public RecipeManager() {
		super(GSON, "recipes");
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.hasErrors = false;
		Map<RecipeType<?>, Builder<ResourceLocation, Recipe<?>>> map2 = Maps.<RecipeType<?>, Builder<ResourceLocation, Recipe<?>>>newHashMap();
		Builder<ResourceLocation, Recipe<?>> builder = ImmutableMap.builder();

		for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();

			try {
				Recipe<?> recipe = fromJson(resourceLocation, GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), "top element"));
				((Builder)map2.computeIfAbsent(recipe.getType(), recipeType -> ImmutableMap.builder())).put(resourceLocation, recipe);
				builder.put(resourceLocation, recipe);
			} catch (IllegalArgumentException | JsonParseException var10) {
				LOGGER.error("Parsing error loading recipe {}", resourceLocation, var10);
			}
		}

		this.recipes = (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>)map2.entrySet()
			.stream()
			.collect(ImmutableMap.toImmutableMap(Entry::getKey, entryx -> ((Builder)entryx.getValue()).build()));
		this.byName = builder.build();
		LOGGER.info("Loaded {} recipes", map2.size());
	}

	public boolean hadErrorsLoading() {
		return this.hasErrors;
	}

	public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
		return this.byType(recipeType).values().stream().filter(recipe -> recipe.matches(container, level)).findFirst();
	}

	public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, T>> getRecipeFor(
		RecipeType<T> recipeType, C container, Level level, @Nullable ResourceLocation resourceLocation
	) {
		Map<ResourceLocation, T> map = this.byType(recipeType);
		if (resourceLocation != null) {
			T recipe = (T)map.get(resourceLocation);
			if (recipe != null && recipe.matches(container, level)) {
				return Optional.of(Pair.of(resourceLocation, recipe));
			}
		}

		return map.entrySet()
			.stream()
			.filter(entry -> ((Recipe)entry.getValue()).matches(container, level))
			.findFirst()
			.map(entry -> Pair.of((ResourceLocation)entry.getKey(), (Recipe)entry.getValue()));
	}

	public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> recipeType) {
		return List.copyOf(this.byType(recipeType).values());
	}

	public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
		return (List<T>)this.byType(recipeType)
			.values()
			.stream()
			.filter(recipe -> recipe.matches(container, level))
			.sorted(Comparator.comparing(recipe -> recipe.getResultItem(level.registryAccess()).getDescriptionId()))
			.collect(Collectors.toList());
	}

	private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> byType(RecipeType<T> recipeType) {
		return (Map<ResourceLocation, T>)this.recipes.getOrDefault(recipeType, Collections.emptyMap());
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
		return Optional.ofNullable((Recipe)this.byName.get(resourceLocation));
	}

	public Collection<Recipe<?>> getRecipes() {
		return (Collection<Recipe<?>>)this.recipes.values().stream().flatMap(map -> map.values().stream()).collect(Collectors.toSet());
	}

	public Stream<ResourceLocation> getRecipeIds() {
		return this.recipes.values().stream().flatMap(map -> map.keySet().stream());
	}

	public static Recipe<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "type");
		return ((RecipeSerializer)BuiltInRegistries.RECIPE_SERIALIZER
				.getOptional(new ResourceLocation(string))
				.orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'")))
			.fromJson(resourceLocation, jsonObject);
	}

	public void replaceRecipes(Iterable<Recipe<?>> iterable) {
		this.hasErrors = false;
		Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = Maps.<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>newHashMap();
		Builder<ResourceLocation, Recipe<?>> builder = ImmutableMap.builder();
		iterable.forEach(recipe -> {
			Map<ResourceLocation, Recipe<?>> map2 = (Map<ResourceLocation, Recipe<?>>)map.computeIfAbsent(recipe.getType(), recipeType -> Maps.newHashMap());
			ResourceLocation resourceLocation = recipe.getId();
			Recipe<?> recipe2 = (Recipe<?>)map2.put(resourceLocation, recipe);
			builder.put(resourceLocation, recipe);
			if (recipe2 != null) {
				throw new IllegalStateException("Duplicate recipe ignored with ID " + resourceLocation);
			}
		});
		this.recipes = ImmutableMap.copyOf(map);
		this.byName = builder.build();
	}

	public static <C extends Container, T extends Recipe<C>> RecipeManager.CachedCheck<C, T> createCheck(RecipeType<T> recipeType) {
		return new RecipeManager.CachedCheck<C, T>() {
			@Nullable
			private ResourceLocation lastRecipe;

			@Override
			public Optional<T> getRecipeFor(C container, Level level) {
				RecipeManager recipeManager = level.getRecipeManager();
				Optional<Pair<ResourceLocation, T>> optional = recipeManager.getRecipeFor(recipeType, container, level, this.lastRecipe);
				if (optional.isPresent()) {
					Pair<ResourceLocation, T> pair = (Pair<ResourceLocation, T>)optional.get();
					this.lastRecipe = pair.getFirst();
					return Optional.of(pair.getSecond());
				} else {
					return Optional.empty();
				}
			}
		};
	}

	public interface CachedCheck<C extends Container, T extends Recipe<C>> {
		Optional<T> getRecipeFor(C container, Level level);
	}
}
