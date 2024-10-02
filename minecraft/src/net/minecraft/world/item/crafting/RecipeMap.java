package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class RecipeMap {
	public static final RecipeMap EMPTY = new RecipeMap(ImmutableMultimap.of(), Map.of());
	private final Multimap<RecipeType<?>, RecipeHolder<?>> byType;
	private final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

	private RecipeMap(Multimap<RecipeType<?>, RecipeHolder<?>> multimap, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> map) {
		this.byType = multimap;
		this.byKey = map;
	}

	public static RecipeMap create(Iterable<RecipeHolder<?>> iterable) {
		Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
		com.google.common.collect.ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> builder2 = ImmutableMap.builder();

		for (RecipeHolder<?> recipeHolder : iterable) {
			builder.put(recipeHolder.value().getType(), recipeHolder);
			builder2.put(recipeHolder.id(), recipeHolder);
		}

		return new RecipeMap(builder.build(), builder2.build());
	}

	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> recipeType) {
		return (Collection<RecipeHolder<T>>)this.byType.get(recipeType);
	}

	public Collection<RecipeHolder<?>> values() {
		return this.byKey.values();
	}

	@Nullable
	public RecipeHolder<?> byKey(ResourceKey<Recipe<?>> resourceKey) {
		return (RecipeHolder<?>)this.byKey.get(resourceKey);
	}

	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(RecipeType<T> recipeType, I recipeInput, Level level) {
		return recipeInput.isEmpty() ? Stream.empty() : this.byType(recipeType).stream().filter(recipeHolder -> recipeHolder.value().matches(recipeInput, level));
	}
}
