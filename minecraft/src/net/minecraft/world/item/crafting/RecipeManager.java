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
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.Util;
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
	private Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> recipes = ImmutableMap.of();
	private Map<ResourceLocation, RecipeHolder<?>> byName = ImmutableMap.of();
	private boolean hasErrors;

	public RecipeManager() {
		super(GSON, "recipes");
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.hasErrors = false;
		Map<RecipeType<?>, Builder<ResourceLocation, RecipeHolder<?>>> map2 = Maps.<RecipeType<?>, Builder<ResourceLocation, RecipeHolder<?>>>newHashMap();
		Builder<ResourceLocation, RecipeHolder<?>> builder = ImmutableMap.builder();

		for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();

			try {
				RecipeHolder<?> recipeHolder = fromJson(resourceLocation, GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), "top element"));
				((Builder)map2.computeIfAbsent(recipeHolder.value().getType(), recipeType -> ImmutableMap.builder())).put(resourceLocation, recipeHolder);
				builder.put(resourceLocation, recipeHolder);
			} catch (IllegalArgumentException | JsonParseException var10) {
				LOGGER.error("Parsing error loading recipe {}", resourceLocation, var10);
			}
		}

		this.recipes = (Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>>)map2.entrySet()
			.stream()
			.collect(ImmutableMap.toImmutableMap(Entry::getKey, entryx -> ((Builder)entryx.getValue()).build()));
		this.byName = builder.build();
		LOGGER.info("Loaded {} recipes", map2.size());
	}

	public boolean hadErrorsLoading() {
		return this.hasErrors;
	}

	public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
		return this.byType(recipeType).values().stream().filter(recipeHolder -> recipeHolder.value().matches(container, level)).findFirst();
	}

	public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, RecipeHolder<T>>> getRecipeFor(
		RecipeType<T> recipeType, C container, Level level, @Nullable ResourceLocation resourceLocation
	) {
		Map<ResourceLocation, RecipeHolder<T>> map = this.byType(recipeType);
		if (resourceLocation != null) {
			RecipeHolder<T> recipeHolder = (RecipeHolder<T>)map.get(resourceLocation);
			if (recipeHolder != null && recipeHolder.value().matches(container, level)) {
				return Optional.of(Pair.of(resourceLocation, recipeHolder));
			}
		}

		return map.entrySet()
			.stream()
			.filter(entry -> ((RecipeHolder)entry.getValue()).value().matches(container, level))
			.findFirst()
			.map(entry -> Pair.of((ResourceLocation)entry.getKey(), (RecipeHolder)entry.getValue()));
	}

	public <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getAllRecipesFor(RecipeType<T> recipeType) {
		return List.copyOf(this.byType(recipeType).values());
	}

	public <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
		return (List<RecipeHolder<T>>)this.byType(recipeType)
			.values()
			.stream()
			.filter(recipeHolder -> recipeHolder.value().matches(container, level))
			.sorted(Comparator.comparing(recipeHolder -> recipeHolder.value().getResultItem(level.registryAccess()).getDescriptionId()))
			.collect(Collectors.toList());
	}

	private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>> byType(RecipeType<T> recipeType) {
		return (Map<ResourceLocation, RecipeHolder<T>>)this.recipes.getOrDefault(recipeType, Collections.emptyMap());
	}

	public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> recipeType, C container, Level level) {
		Optional<RecipeHolder<T>> optional = this.getRecipeFor(recipeType, container, level);
		if (optional.isPresent()) {
			return ((RecipeHolder)optional.get()).value().getRemainingItems(container);
		} else {
			NonNullList<ItemStack> nonNullList = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

			for (int i = 0; i < nonNullList.size(); i++) {
				nonNullList.set(i, container.getItem(i));
			}

			return nonNullList;
		}
	}

	public Optional<RecipeHolder<?>> byKey(ResourceLocation resourceLocation) {
		return Optional.ofNullable((RecipeHolder)this.byName.get(resourceLocation));
	}

	public Collection<RecipeHolder<?>> getRecipes() {
		return (Collection<RecipeHolder<?>>)this.recipes.values().stream().flatMap(map -> map.values().stream()).collect(Collectors.toSet());
	}

	public Stream<ResourceLocation> getRecipeIds() {
		return this.recipes.values().stream().flatMap(map -> map.keySet().stream());
	}

	protected static RecipeHolder<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "type");
		Codec<? extends Recipe<?>> codec = ((RecipeSerializer)BuiltInRegistries.RECIPE_SERIALIZER
				.getOptional(new ResourceLocation(string))
				.orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'")))
			.codec();
		Recipe<?> recipe = Util.getOrThrow((DataResult<Recipe<?>>)codec.parse(JsonOps.INSTANCE, jsonObject), JsonParseException::new);
		return new RecipeHolder<>(resourceLocation, recipe);
	}

	public void replaceRecipes(Iterable<RecipeHolder<?>> iterable) {
		this.hasErrors = false;
		Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> map = Maps.<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>>newHashMap();
		Builder<ResourceLocation, RecipeHolder<?>> builder = ImmutableMap.builder();
		iterable.forEach(
			recipeHolder -> {
				Map<ResourceLocation, RecipeHolder<?>> map2 = (Map<ResourceLocation, RecipeHolder<?>>)map.computeIfAbsent(
					recipeHolder.value().getType(), recipeType -> Maps.newHashMap()
				);
				ResourceLocation resourceLocation = recipeHolder.id();
				RecipeHolder<?> recipeHolder2 = (RecipeHolder<?>)map2.put(resourceLocation, recipeHolder);
				builder.put(resourceLocation, recipeHolder);
				if (recipeHolder2 != null) {
					throw new IllegalStateException("Duplicate recipe ignored with ID " + resourceLocation);
				}
			}
		);
		this.recipes = ImmutableMap.copyOf(map);
		this.byName = builder.build();
	}

	public static <C extends Container, T extends Recipe<C>> RecipeManager.CachedCheck<C, T> createCheck(RecipeType<T> recipeType) {
		return new RecipeManager.CachedCheck<C, T>() {
			@Nullable
			private ResourceLocation lastRecipe;

			@Override
			public Optional<RecipeHolder<T>> getRecipeFor(C container, Level level) {
				RecipeManager recipeManager = level.getRecipeManager();
				Optional<Pair<ResourceLocation, RecipeHolder<T>>> optional = recipeManager.getRecipeFor(recipeType, container, level, this.lastRecipe);
				if (optional.isPresent()) {
					Pair<ResourceLocation, RecipeHolder<T>> pair = (Pair<ResourceLocation, RecipeHolder<T>>)optional.get();
					this.lastRecipe = pair.getFirst();
					return Optional.of(pair.getSecond());
				} else {
					return Optional.empty();
				}
			}
		};
	}

	public interface CachedCheck<C extends Container, T extends Recipe<C>> {
		Optional<RecipeHolder<T>> getRecipeFor(C container, Level level);
	}
}
