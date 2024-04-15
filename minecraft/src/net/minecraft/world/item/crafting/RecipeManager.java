package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger LOGGER = LogUtils.getLogger();
	private final HolderLookup.Provider registries;
	private Multimap<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.of();
	private Map<ResourceLocation, RecipeHolder<?>> byName = ImmutableMap.of();
	private boolean hasErrors;

	public RecipeManager(HolderLookup.Provider provider) {
		super(GSON, "recipes");
		this.registries = provider;
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.hasErrors = false;
		Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
		com.google.common.collect.ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder2 = ImmutableMap.builder();
		RegistryOps<JsonElement> registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);

		for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();

			try {
				Recipe<?> recipe = Recipe.CODEC.parse(registryOps, (JsonElement)entry.getValue()).getOrThrow(JsonParseException::new);
				RecipeHolder<?> recipeHolder = new RecipeHolder<>(resourceLocation, recipe);
				builder.put(recipe.getType(), recipeHolder);
				builder2.put(resourceLocation, recipeHolder);
			} catch (IllegalArgumentException | JsonParseException var12) {
				LOGGER.error("Parsing error loading recipe {}", resourceLocation, var12);
			}
		}

		this.byType = builder.build();
		this.byName = builder2.build();
		LOGGER.info("Loaded {} recipes", this.byType.size());
	}

	public boolean hadErrorsLoading() {
		return this.hasErrors;
	}

	public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
		return this.byType(recipeType).stream().filter(recipeHolder -> recipeHolder.value().matches(container, level)).findFirst();
	}

	public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(
		RecipeType<T> recipeType, C container, Level level, @Nullable ResourceLocation resourceLocation
	) {
		if (resourceLocation != null) {
			RecipeHolder<T> recipeHolder = this.byKeyTyped(recipeType, resourceLocation);
			if (recipeHolder != null && recipeHolder.value().matches(container, level)) {
				return Optional.of(recipeHolder);
			}
		}

		return this.byType(recipeType).stream().filter(recipeHolderx -> recipeHolderx.value().matches(container, level)).findFirst();
	}

	public <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getAllRecipesFor(RecipeType<T> recipeType) {
		return List.copyOf(this.byType(recipeType));
	}

	public <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
		return (List<RecipeHolder<T>>)this.byType(recipeType)
			.stream()
			.filter(recipeHolder -> recipeHolder.value().matches(container, level))
			.sorted(Comparator.comparing(recipeHolder -> recipeHolder.value().getResultItem(level.registryAccess()).getDescriptionId()))
			.collect(Collectors.toList());
	}

	private <C extends Container, T extends Recipe<C>> Collection<RecipeHolder<T>> byType(RecipeType<T> recipeType) {
		return (Collection<RecipeHolder<T>>)this.byType.get(recipeType);
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

	@Nullable
	private <T extends Recipe<?>> RecipeHolder<T> byKeyTyped(RecipeType<T> recipeType, ResourceLocation resourceLocation) {
		RecipeHolder<?> recipeHolder = (RecipeHolder<?>)this.byName.get(resourceLocation);
		return (RecipeHolder<T>)(recipeHolder != null && recipeHolder.value().getType().equals(recipeType) ? recipeHolder : null);
	}

	public Collection<RecipeHolder<?>> getOrderedRecipes() {
		return this.byType.values();
	}

	public Collection<RecipeHolder<?>> getRecipes() {
		return this.byName.values();
	}

	public Stream<ResourceLocation> getRecipeIds() {
		return this.byName.keySet().stream();
	}

	@VisibleForTesting
	protected static RecipeHolder<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject, HolderLookup.Provider provider) {
		Recipe<?> recipe = Recipe.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), jsonObject).getOrThrow(JsonParseException::new);
		return new RecipeHolder<>(resourceLocation, recipe);
	}

	public void replaceRecipes(Iterable<RecipeHolder<?>> iterable) {
		this.hasErrors = false;
		Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
		com.google.common.collect.ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder2 = ImmutableMap.builder();

		for (RecipeHolder<?> recipeHolder : iterable) {
			RecipeType<?> recipeType = recipeHolder.value().getType();
			builder.put(recipeType, recipeHolder);
			builder2.put(recipeHolder.id(), recipeHolder);
		}

		this.byType = builder.build();
		this.byName = builder2.build();
	}

	public static <C extends Container, T extends Recipe<C>> RecipeManager.CachedCheck<C, T> createCheck(RecipeType<T> recipeType) {
		return new RecipeManager.CachedCheck<C, T>() {
			@Nullable
			private ResourceLocation lastRecipe;

			@Override
			public Optional<RecipeHolder<T>> getRecipeFor(C container, Level level) {
				RecipeManager recipeManager = level.getRecipeManager();
				Optional<RecipeHolder<T>> optional = recipeManager.getRecipeFor(recipeType, container, level, this.lastRecipe);
				if (optional.isPresent()) {
					RecipeHolder<T> recipeHolder = (RecipeHolder<T>)optional.get();
					this.lastRecipe = recipeHolder.id();
					return Optional.of(recipeHolder);
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
