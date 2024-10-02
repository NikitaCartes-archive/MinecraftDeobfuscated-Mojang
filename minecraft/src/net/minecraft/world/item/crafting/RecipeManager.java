package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimplePreparableReloadListener<RecipeMap> implements RecipeAccess {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final HolderLookup.Provider registries;
	private RecipeMap recipes = RecipeMap.EMPTY;
	private Map<ResourceKey<RecipePropertySet>, RecipePropertySet> propertySets = Map.of();
	private SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes = SelectableRecipe.SingleInputSet.empty();
	private List<RecipeManager.ServerDisplayInfo> allDisplays = List.of();
	private Map<ResourceKey<Recipe<?>>, List<RecipeManager.ServerDisplayInfo>> recipeToDisplay = Map.of();

	public RecipeManager(HolderLookup.Provider provider) {
		this.registries = provider;
	}

	protected RecipeMap prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		SortedMap<ResourceLocation, Recipe<?>> sortedMap = new TreeMap();
		SimpleJsonResourceReloadListener.scanDirectory(
			resourceManager, Registries.elementsDirPath(Registries.RECIPE), this.registries.createSerializationContext(JsonOps.INSTANCE), Recipe.CODEC, sortedMap
		);
		List<RecipeHolder<?>> list = new ArrayList(sortedMap.size());
		sortedMap.forEach((resourceLocation, recipe) -> {
			ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, resourceLocation);
			RecipeHolder<?> recipeHolder = new RecipeHolder(resourceKey, recipe);
			list.add(recipeHolder);
		});
		return RecipeMap.create(list);
	}

	protected void apply(RecipeMap recipeMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.recipes = recipeMap;
		LOGGER.info("Loaded {} recipes", recipeMap.values().size());
	}

	public void finalizeRecipeLoading(FeatureFlagSet featureFlagSet) {
		List<Ingredient> list = new ArrayList();
		List<Ingredient> list2 = new ArrayList();
		List<Ingredient> list3 = new ArrayList();
		List<Ingredient> list4 = new ArrayList();
		List<Ingredient> list5 = new ArrayList();
		List<Ingredient> list6 = new ArrayList();
		List<Ingredient> list7 = new ArrayList();
		List<SelectableRecipe.SingleInputEntry<StonecutterRecipe>> list8 = new ArrayList();
		this.recipes
			.values()
			.forEach(
				recipeHolder -> {
					Recipe<?> recipe = recipeHolder.value();
					if (!recipe.isSpecial() && recipe.placementInfo().isImpossibleToPlace()) {
						LOGGER.warn("Recipe {} can't be placed due to empty ingredients and will be ignored", recipeHolder.id().location());
					} else {
						if (recipe instanceof SmithingRecipe smithingRecipe) {
							smithingRecipe.additionIngredient().ifPresent(list3::add);
							smithingRecipe.baseIngredient().ifPresent(list2::add);
							smithingRecipe.templateIngredient().ifPresent(list::add);
						}

						if (recipe instanceof AbstractCookingRecipe abstractCookingRecipe) {
							if (abstractCookingRecipe.getType() == RecipeType.SMELTING) {
								list4.add(abstractCookingRecipe.input());
							} else if (abstractCookingRecipe.getType() == RecipeType.BLASTING) {
								list5.add(abstractCookingRecipe.input());
							} else if (abstractCookingRecipe.getType() == RecipeType.SMOKING) {
								list6.add(abstractCookingRecipe.input());
							} else if (abstractCookingRecipe.getType() == RecipeType.CAMPFIRE_COOKING) {
								list7.add(abstractCookingRecipe.input());
							}
						}

						if (recipe instanceof StonecutterRecipe stonecutterRecipe
							&& isIngredientEnabled(featureFlagSet, stonecutterRecipe.input())
							&& stonecutterRecipe.resultDisplay().isEnabled(featureFlagSet)) {
							list8.add(
								new SelectableRecipe.SingleInputEntry(stonecutterRecipe.input(), new SelectableRecipe(stonecutterRecipe.resultDisplay(), Optional.of(recipeHolder)))
							);
						}
					}
				}
			);
		this.propertySets = Map.of(
			RecipePropertySet.SMITHING_ADDITION,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list3)),
			RecipePropertySet.SMITHING_BASE,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list2)),
			RecipePropertySet.SMITHING_TEMPLATE,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list)),
			RecipePropertySet.FURNACE_INPUT,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list4)),
			RecipePropertySet.BLAST_FURNACE_INPUT,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list5)),
			RecipePropertySet.SMOKER_INPUT,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list6)),
			RecipePropertySet.CAMPFIRE_INPUT,
			RecipePropertySet.create(filterDisabled(featureFlagSet, list7))
		);
		this.stonecutterRecipes = new SelectableRecipe.SingleInputSet<>(list8);
		this.allDisplays = unpackRecipeInfo(this.recipes.values(), featureFlagSet);
		this.recipeToDisplay = (Map<ResourceKey<Recipe<?>>, List<RecipeManager.ServerDisplayInfo>>)this.allDisplays
			.stream()
			.collect(Collectors.groupingBy(serverDisplayInfo -> serverDisplayInfo.parent.id(), IdentityHashMap::new, Collectors.toList()));
	}

	private static List<Ingredient> filterDisabled(FeatureFlagSet featureFlagSet, List<Ingredient> list) {
		list.removeIf(ingredient -> !isIngredientEnabled(featureFlagSet, ingredient));
		return list;
	}

	private static boolean isIngredientEnabled(FeatureFlagSet featureFlagSet, Ingredient ingredient) {
		return ingredient.items().stream().allMatch(holder -> ((Item)holder.value()).isEnabled(featureFlagSet));
	}

	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
		RecipeType<T> recipeType, I recipeInput, Level level, @Nullable ResourceKey<Recipe<?>> resourceKey
	) {
		RecipeHolder<T> recipeHolder = resourceKey != null ? this.byKeyTyped(recipeType, resourceKey) : null;
		return this.getRecipeFor(recipeType, recipeInput, level, recipeHolder);
	}

	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
		RecipeType<T> recipeType, I recipeInput, Level level, @Nullable RecipeHolder<T> recipeHolder
	) {
		return recipeHolder != null && recipeHolder.value().matches(recipeInput, level)
			? Optional.of(recipeHolder)
			: this.getRecipeFor(recipeType, recipeInput, level);
	}

	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I recipeInput, Level level) {
		return this.recipes.getRecipesFor(recipeType, recipeInput, level).findFirst();
	}

	public Optional<RecipeHolder<?>> byKey(ResourceKey<Recipe<?>> resourceKey) {
		return Optional.ofNullable(this.recipes.byKey(resourceKey));
	}

	@Nullable
	private <T extends Recipe<?>> RecipeHolder<T> byKeyTyped(RecipeType<T> recipeType, ResourceKey<Recipe<?>> resourceKey) {
		RecipeHolder<?> recipeHolder = this.recipes.byKey(resourceKey);
		return (RecipeHolder<T>)(recipeHolder != null && recipeHolder.value().getType().equals(recipeType) ? recipeHolder : null);
	}

	public Map<ResourceKey<RecipePropertySet>, RecipePropertySet> getSynchronizedItemProperties() {
		return this.propertySets;
	}

	public SelectableRecipe.SingleInputSet<StonecutterRecipe> getSynchronizedStonecutterRecipes() {
		return this.stonecutterRecipes;
	}

	@Override
	public RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourceKey) {
		return (RecipePropertySet)this.propertySets.getOrDefault(resourceKey, RecipePropertySet.EMPTY);
	}

	@Override
	public SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes() {
		return this.stonecutterRecipes;
	}

	public Collection<RecipeHolder<?>> getRecipes() {
		return this.recipes.values();
	}

	@Nullable
	public RecipeManager.ServerDisplayInfo getRecipeFromDisplay(RecipeDisplayId recipeDisplayId) {
		return (RecipeManager.ServerDisplayInfo)this.allDisplays.get(recipeDisplayId.index());
	}

	public void listDisplaysForRecipe(ResourceKey<Recipe<?>> resourceKey, Consumer<RecipeDisplayEntry> consumer) {
		((List)this.recipeToDisplay.get(resourceKey)).forEach(serverDisplayInfo -> consumer.accept(serverDisplayInfo.display));
	}

	@VisibleForTesting
	protected static RecipeHolder<?> fromJson(ResourceKey<Recipe<?>> resourceKey, JsonObject jsonObject, HolderLookup.Provider provider) {
		Recipe<?> recipe = Recipe.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), jsonObject).getOrThrow(JsonParseException::new);
		return new RecipeHolder<>(resourceKey, recipe);
	}

	public static <I extends RecipeInput, T extends Recipe<I>> RecipeManager.CachedCheck<I, T> createCheck(RecipeType<T> recipeType) {
		return new RecipeManager.CachedCheck<I, T>() {
			@Nullable
			private ResourceKey<Recipe<?>> lastRecipe;

			@Override
			public Optional<RecipeHolder<T>> getRecipeFor(I recipeInput, ServerLevel serverLevel) {
				RecipeManager recipeManager = serverLevel.recipeAccess();
				Optional<RecipeHolder<T>> optional = recipeManager.getRecipeFor(recipeType, recipeInput, serverLevel, this.lastRecipe);
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

	private static List<RecipeManager.ServerDisplayInfo> unpackRecipeInfo(Iterable<RecipeHolder<?>> iterable, FeatureFlagSet featureFlagSet) {
		List<RecipeManager.ServerDisplayInfo> list = new ArrayList();
		Object2IntMap<String> object2IntMap = new Object2IntOpenHashMap<>();

		for (RecipeHolder<?> recipeHolder : iterable) {
			Recipe<?> recipe = recipeHolder.value();
			OptionalInt optionalInt;
			if (recipe.group().isEmpty()) {
				optionalInt = OptionalInt.empty();
			} else {
				optionalInt = OptionalInt.of(object2IntMap.computeIfAbsent(recipe.group(), object -> object2IntMap.size()));
			}

			Optional<List<Ingredient>> optional;
			if (recipe.isSpecial()) {
				optional = Optional.empty();
			} else {
				optional = Optional.of(recipe.placementInfo().ingredients());
			}

			for (RecipeDisplay recipeDisplay : recipe.display()) {
				if (recipeDisplay.isEnabled(featureFlagSet)) {
					int i = list.size();
					RecipeDisplayId recipeDisplayId = new RecipeDisplayId(i);
					RecipeDisplayEntry recipeDisplayEntry = new RecipeDisplayEntry(recipeDisplayId, recipeDisplay, optionalInt, recipe.recipeBookCategory(), optional);
					list.add(new RecipeManager.ServerDisplayInfo(recipeDisplayEntry, recipeHolder));
				}
			}
		}

		return list;
	}

	public interface CachedCheck<I extends RecipeInput, T extends Recipe<I>> {
		Optional<RecipeHolder<T>> getRecipeFor(I recipeInput, ServerLevel serverLevel);
	}

	public static record ServerDisplayInfo(RecipeDisplayEntry display, RecipeHolder<?> parent) {
	}
}
